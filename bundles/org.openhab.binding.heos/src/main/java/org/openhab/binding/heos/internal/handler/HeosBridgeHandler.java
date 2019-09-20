/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
import org.openhab.binding.heos.internal.HeosChannelManager;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.configuration.BridgeConfiguration;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscoveryListener;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.HeosPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

/**
 * The {@link HeosSystemHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosBridgeHandler extends BaseBridgeHandler implements HeosEventListener {
    private final Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

    private static final int HEOS_PORT = 1255;

    private List<String> heosPlaylists = new ArrayList<>();
    private List<HeosPlayerDiscoveryListener> playerDiscoveryList = new ArrayList<>();
    private Map<String, String> selectedPlayer = new HashMap<>();
    private List<String[]> selectedPlayerList = new ArrayList<>();
    private HeosChannelManager channelManager = new HeosChannelManager(this);
    private HeosChannelHandlerFactory channelHandlerFactory;

    private Map<String, HeosGroupHandler> groupHandlerMap = new HashMap<>();
    private Map<String, String> hashToGidMap = new HashMap<>();

    private ScheduledFuture<?> startupFuture;

    private HeosSystem heos;

    // TODO try-to-get-rid of this one
    private boolean bridgeIsConnected = false;
    private boolean loggedIn = false;
    private boolean connectionDelay = false;
    private boolean bridgeHandlerdisposalOngoing = false;

    private BridgeConfiguration configuration;
    private Map<String, String> properties;

    public HeosBridgeHandler(Bridge thing) {
        super(thing);
        this.heos = new HeosSystem();
        channelHandlerFactory = new HeosChannelHandlerFactory(this, heos.getAPI());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        ChannelTypeUID channelTypeUID = null; // Needed to detect the player channels on the bridge
        Channel channel = this.getThing().getChannel(channelUID.getId());
        if (channel != null) {
            channelTypeUID = channel.getChannelTypeUID();
        } else {
            logger.debug("No valid channel found");
            return;
        }
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID, channelTypeUID);
        if (channelHandler != null) {
            channelHandler.handleCommand(command, this, channelUID);
        }
    }

    @Override
    public synchronized void initialize() {
        configuration = thing.getConfiguration().as(BridgeConfiguration.class);
        properties = thing.getProperties();

        scheduledStartUp();
    }

    private void scheduledStartUp() {
        startupFuture = scheduler.schedule(this::delayedInitialize, 5, TimeUnit.SECONDS);
    }

    private void delayedInitialize() {
        try {
            // TODO wrap with try-catch to request for a retry later
            logger.debug("Running scheduledStartUp job");
            connectBridge();
            bridgeHandlerdisposalOngoing = false;
            heos.startEventListener();
            heos.startHeartBeat(configuration.heartbeat);

            logger.debug("HEOS System heart beat started. Pulse time is {}s", configuration.heartbeat);
            // gets all available player and groups to ensure that the system knows
            // about the conjunction between the groupMemberHash and the GID
            triggerPlayerDiscovery();
            if (StringUtils.isNotEmpty(configuration.username) && StringUtils.isNotEmpty(configuration.password)) {
                logger.debug("Logging in to HEOS account.");
                heos.getAPI().logIn(configuration.username, configuration.password);
                updateState(CH_ID_REBOOT, OnOffType.OFF);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(thing.getStatus(), ThingStatusDetail.CONFIGURATION_ERROR, "Can't log in. Username or password not set.");
            }
        } catch (RuntimeException e) {
            logger.debug("Error occurred while connecting", e);
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Errors occurred: " + e.getMessage());
            scheduler.schedule(this::delayedInitialize, 30, TimeUnit.SECONDS);
        }
    }

    private void connectBridge() {
        loggedIn = false;

        logger.debug("Initialize Bridge '{}' with IP '{}'",  properties.get(PROP_NAME), configuration.ipAddress);
        heos.setConnectionIP(configuration.ipAddress);
        heos.setConnectionPort(HEOS_PORT);
        heos.establishConnection();
        heos.getAPI().registerForChangeEvents(this);
    }

    @Override
    public void dispose() {
        bridgeHandlerdisposalOngoing = true; // Flag to prevent the handler from being updated during disposal

        terminateStartupSequence();

        heos.getAPI().unregisterForChangeEvents(this);
        logger.debug("HEOS bridge removed from change notifications");

        logger.debug("Dispose bridge '{}'", properties.get(PROP_NAME));
        heos.closeConnection();
    }

    private void terminateStartupSequence() {
        ScheduledFuture<?> localStartupFuture = startupFuture;
        if (localStartupFuture != null && !localStartupFuture.isCancelled()) {
            localStartupFuture.cancel(true);
        }
        startupFuture = null;
    }

    /**
     * Manages adding the player channel to the bridge
     */
    @Override
    public synchronized void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        addPlayerChannel(childThing);
        logger.debug("Initialize child handler for: {}.", childThing.getUID().getId());
    }

    /**
     * Manages the removal of the player or group channels from the bridge.
     */
    @Override
    public synchronized void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Disposing child handler for: {}.", childThing.getUID().getId());
        if (bridgeHandlerdisposalOngoing) { // Checks if bridgeHandler is going to disposed (by stopping the binding or
            // openHAB for example) and prevents it from being updated which stops the
            // disposal process.
            return;
        } else if (HeosPlayerHandler.class.equals(childHandler.getClass())) {
            String channelIdentifier = "P" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingleChannel(channelIdentifier));
        } else if (HeosGroupHandler.class.equals(childHandler.getClass())){
            String channelIdentifier = "G" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingleChannel(channelIdentifier));
            // removes the handler from the groupMemberMap that handler is no longer called
            // if group is getting online
            removeGroupHandlerInformation((HeosGroupHandler) childHandler);
        }
    }

    public void resetPlayerList(ChannelUID channelUID) {
        selectedPlayerList.forEach(element -> updateState(element[1], OnOffType.OFF));
        selectedPlayerList.clear();
        updateState(channelUID, OnOffType.OFF);
    }

    /**
     * Sets the HEOS Thing offline
     *
     * @param hashValue
     */
    public void setGroupOffline(String hashValue) {
        HeosGroupHandler groupHandler = groupHandlerMap.get(hashValue);
        if (groupHandler != null) {
            groupHandler.setStatusOffline();
        }
    }

    /**
     * Sets the HEOS Thing online. Also updates the link between
     * the groubMemberHash value with the actual gid of this group     *
     */
    public void setGroupOnline(HeosGroup group) {
        hashToGidMap.put(group.getGroupMemberHash(), group.getGid());
        groupHandlerMap.forEach((hash, handler) -> {
            if (hash.equals(group.getGroupMemberHash())) {
                handler.setStatusOnline();
                addPlayerChannel(handler.getThing());
            }
        });
    }

    public void addGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.put(handler.getGroupMemberHash(), handler);
    }

    private void removeGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.remove(handler.getGroupMemberHash());
    }

    public String getActualGID(String groupHash) {
        return hashToGidMap.get(groupHash);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        // Do nothing
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        // Do nothing
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (EVENTTYPE_EVENT.equals(event)) {
            if (PLAYERS_CHANGED.equals(command)) {
                triggerPlayerDiscovery();
            } else if (GROUPS_CHANGED.equals(command)) {
                triggerPlayerDiscovery();
            } else if (CONNECTION_LOST.equals(command)) {
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                bridgeIsConnected = false;
                logger.debug("HEOS Bridge OFFLINE");
            } else if (CONNECTION_RESTORED.equals(command)) {
                connectionDelay = true;
                initialize();
            }
        }
        if (EVENTTYPE_SYSTEM.equals(event)) {
            if (SING_IN.equals(command)) {
                if (SUCCESS.equals(result)) {
                    if (!loggedIn) {
                        loggedIn = true;
                        addPlaylists();
                    }
                }
            } else if (USER_CHANGED.equals(command)) {
                if (!loggedIn) {
                    loggedIn = true;
                    addPlaylists();
                }
            }
        }
    }

    public void addPlaylists() {
        if (loggedIn) {
            heosPlaylists.clear();
            heosPlaylists = heos.getPlaylists();
        }
    }

    /**
     * Create a channel for the childThing. Depending if it is a HEOS Group
     * or a player an identification prefix is added
     *
     * @param childThing the thing the channel is created for
     */
    @SuppressWarnings("null")
    private void addPlayerChannel(Thing childThing) {
        String channelIdentifier = "";
        String pid = "";
        if (HeosPlayerHandler.class.equals(childThing.getHandler().getClass())) {
            channelIdentifier = "P" + childThing.getUID().getId();
            pid = childThing.getConfiguration().get(PROP_PID).toString();
        } else if (HeosGroupHandler.class.equals(childThing.getHandler().getClass())) {
            channelIdentifier = "G" + childThing.getUID().getId();
            HeosGroupHandler handler = (HeosGroupHandler) childThing.getHandler();
            pid = handler.getGroupID();
        }
        Map<String, String> properties = new HashMap<>(2);
        String playerName = childThing.getLabel();
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelIdentifier);
        properties.put(PROP_NAME, playerName);
        properties.put(PID, pid);

        Channel channel = ChannelBuilder.create(channelUID, "Switch").withLabel(playerName).withType(CH_TYPE_PLAYER)
                .withProperties(properties).build();
        updateThingChannels(channelManager.addSingleChannel(channel));
    }

    private void updateThingChannels(List<Channel> channelList) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    public Map<String, HeosPlayer> getNewPlayer() {
        // create a clone of the map
        return new HashMap<>(heos.getAllPlayer());
    }

    public Map<String, HeosGroup> getNewGroups() {
        return heos.getGroups();
    }

    public Map<String, HeosGroup> getRemovedGroups() {
        return heos.getGroupsRemoved();
    }

    public Map<String, HeosPlayer> getRemovedPlayer() {
        return heos.getPlayerRemoved();
    }

    /**
     * The list with the currently selected player
     *
     * @return a HashMap which the currently selected player
     */
    public Map<String, String> getSelectedPlayer() {
        selectedPlayer.clear();
        for (String[] strings : selectedPlayerList) {
            selectedPlayer.put(strings[0], strings[1]);
        }
        return selectedPlayer;
    }

    public List<String[]> getSelectedPlayerList() {
        return selectedPlayerList;
    }

    public void setSelectedPlayerList(List<String[]> selectedPlayerList) {
        this.selectedPlayerList = selectedPlayerList;
    }

    public List<String> getHeosPlaylists() {
        return heosPlaylists;
    }

    public HeosChannelHandlerFactory getChannelHandlerFactory() {
        return channelHandlerFactory;
    }

    /**
     * Register an {@link HeosPlayerDiscoveryListener} to get informed
     * if the amount of groups or players have changed
     *
     * @param listener the implementing class
     */
    public void registerPlayerDiscoverListener(HeosPlayerDiscoveryListener listener) {
        playerDiscoveryList.add(listener);
    }

    private void triggerPlayerDiscovery() {
        playerDiscoveryList.forEach(HeosPlayerDiscoveryListener::playerChanged);
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public boolean isBridgeConnected() {
        return bridgeIsConnected;
    }

    public HeosSystem getSystem() {
        return heos;
    }
}

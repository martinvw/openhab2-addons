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

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.HeosChannelHandlerFactory;
import org.openhab.binding.heos.internal.HeosChannelManager;
import org.openhab.binding.heos.internal.api.HeosFacade;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.configuration.BridgeConfiguration;
import org.openhab.binding.heos.internal.discovery.HeosPlayerDiscoveryListener;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.json.dto.HeosError;
import org.openhab.binding.heos.internal.json.dto.HeosEvent;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosBridgeHandler extends BaseBridgeHandler implements HeosEventListener {
    private final Logger logger = LoggerFactory.getLogger(HeosBridgeHandler.class);

    private static final int HEOS_PORT = 1255;

    private List<HeosPlayerDiscoveryListener> playerDiscoveryList = new CopyOnWriteArrayList<>();
    private List<String[]> selectedPlayerList = new CopyOnWriteArrayList<>();
    private HeosChannelManager channelManager = new HeosChannelManager(this);
    private HeosChannelHandlerFactory channelHandlerFactory;

    private Map<String, HeosGroupHandler> groupHandlerMap = new ConcurrentHashMap<>();
    private Map<String, String> hashToGidMap = new ConcurrentHashMap<>();

    private @Nullable ScheduledFuture<?> startupFuture;

    private HeosSystem heos;
    private @Nullable HeosFacade apiConnection;

    private boolean loggedIn = false;
    private boolean bridgeHandlerDisposalOngoing = false;

    private @NonNullByDefault({}) BridgeConfiguration configuration;

    private int failureCount;

    public HeosBridgeHandler(Bridge thing, HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        super(thing);
        heos = new HeosSystem(scheduler);
        channelHandlerFactory = new HeosChannelHandlerFactory(this, heosDynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        Channel channel = this.getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.debug("No valid channel found");
            return;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        HeosChannelHandler channelHandler = channelHandlerFactory.getChannelHandler(channelUID, this, channelTypeUID);
        if (channelHandler != null) {
            try {
                channelHandler.handleBridgeCommand(command, thing.getUID());
                failureCount = 0;
                updateStatus(ONLINE);
            } catch (IOException | ReadException e) {
                logger.debug("Failed to handle bridge command", e);
                failureCount++;

                if (failureCount > FAILURE_COUNT_LIMIT) {
                    updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Failed to handle command: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public synchronized void initialize() {
        configuration = thing.getConfiguration().as(BridgeConfiguration.class);
        startupFuture = scheduler.schedule(this::delayedInitialize, 5, TimeUnit.SECONDS);
    }

    private void delayedInitialize() {
        HeosFacade connection = null;
        try {
            logger.debug("Running scheduledStartUp job");

            connection = connectBridge();
            updateStatus(ThingStatus.ONLINE);
            updateState(CH_ID_REBOOT, OnOffType.OFF);

            logger.debug("HEOS System heart beat started. Pulse time is {}s", configuration.heartbeat);
            // gets all available player and groups to ensure that the system knows
            // about the conjunction between the groupMemberHash and the GID
            triggerPlayerDiscovery();
            String username = configuration.username;
            String password = configuration.password;
            if (username != null && !"".equals(username) && password != null && !"".equals(password)) {
                login(connection, username, password);
            } else {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Can't log in. Username or password not set.");
            }
        } catch (Telnet.ReadException | IOException | RuntimeException e) {
            logger.debug("Error occurred while connecting", e);
            if (connection != null) {
                connection.closeConnection();
            }
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Errors occurred: " + e.getMessage());
            startupFuture = scheduler.schedule(this::delayedInitialize, 30, TimeUnit.SECONDS);
        }
    }

    private HeosFacade connectBridge() throws IOException, Telnet.ReadException {
        loggedIn = false;

        logger.debug("Initialize Bridge '{}' with IP '{}'", thing.getProperties().get(PROP_NAME),
                configuration.ipAddress);
        bridgeHandlerDisposalOngoing = false;
        HeosFacade connection = heos.establishConnection(configuration.ipAddress, HEOS_PORT, configuration.heartbeat);
        connection.registerForChangeEvents(this);

        apiConnection = connection;

        return connection;
    }

    private void login(HeosFacade connection, String username, String password) throws IOException, ReadException {
        logger.debug("Logging in to HEOS account.");
        HeosResponseObject<Void> response = connection.logIn(username, password);

        if (response.result) {
            logger.debug("successfully logged-in, event is fired to handle post-login behaviour");
            return;
        }

        HeosError error = response.getError();
        logger.debug("Failed to login: {}", error);
        updateStatus(ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                error != null ? error.code.toString() : "Failed to login, no error was returned.");

    }

    @Override
    public void dispose() {
        bridgeHandlerDisposalOngoing = true; // Flag to prevent the handler from being updated during disposal

        terminateStartupSequence();

        HeosFacade localApiConnection = apiConnection;
        if (localApiConnection == null) {
            logger.debug("Not disposing bridge because of missing apiConnection");
            return;
        }

        localApiConnection.unregisterForChangeEvents(this);
        logger.debug("HEOS bridge removed from change notifications");

        logger.debug("Dispose bridge '{}'", thing.getProperties().get(PROP_NAME));
        localApiConnection.closeConnection();
    }

    private void terminateStartupSequence() {
        ScheduledFuture<?> localStartupFuture = startupFuture;
        if (localStartupFuture != null && !localStartupFuture.isCancelled()) {
            localStartupFuture.cancel(true);
        }
    }

    /**
     * Manages the removal of the player or group channels from the bridge.
     */
    @Override
    public synchronized void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Disposing child handler for: {}.", childThing.getUID().getId());
        if (bridgeHandlerDisposalOngoing) { // Checks if bridgeHandler is going to disposed (by stopping the binding or
            // openHAB for example) and prevents it from being updated which stops the
            // disposal process.
        } else if (childHandler instanceof HeosPlayerHandler) {
            String channelIdentifier = "P" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingleChannel(channelIdentifier));
        } else if (childHandler instanceof HeosGroupHandler) {
            String channelIdentifier = "G" + childThing.getUID().getId();
            updateThingChannels(channelManager.removeSingleChannel(channelIdentifier));
            // removes the handler from the groupMemberMap that handler is no longer called
            // if group is getting online
            removeGroupHandlerInformation((HeosGroupHandler) childHandler);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        scheduler.schedule(() -> addPlayerChannel(childThing, null), 5, TimeUnit.SECONDS);
    }

    void resetPlayerList(ChannelUID channelUID) {
        selectedPlayerList.forEach(element -> updateState(element[1], OnOffType.OFF));
        selectedPlayerList.clear();
        updateState(channelUID, OnOffType.OFF);
    }

    /**
     * Sets the HEOS Thing offline
     */
    @SuppressWarnings("null")
    public void setGroupOffline(String hashValue) {
        HeosGroupHandler groupHandler = groupHandlerMap.get(hashValue);
        if (groupHandler != null) {
            groupHandler.setStatusOffline();
        }
    }

    /**
     * Sets the HEOS Thing online. Also updates the link between
     * the groubMemberHash value with the actual gid of this group *
     */
    public void setGroupOnline(String groupMemberHash, String groupId) {
        hashToGidMap.put(groupMemberHash, groupId);
        groupHandlerMap.forEach((hash, handler) -> {
            if (hash.equals(groupMemberHash)) {
                handler.setStatusOnline();
                addPlayerChannel(handler.getThing(), groupId);
            }
        });
    }

    /**
     * Create a channel for the childThing. Depending if it is a HEOS Group
     * or a player an identification prefix is added
     *
     * @param childThing the thing the channel is created for
     * @param groupId
     */
    private void addPlayerChannel(Thing childThing, @Nullable String groupId) {
        try {
            String channelIdentifier = "";
            String pid = "";
            ThingHandler handler = childThing.getHandler();
            if (handler instanceof HeosPlayerHandler) {
                channelIdentifier = "P" + childThing.getUID().getId();
                pid = ((HeosPlayerHandler) handler).getId();
            } else if (handler instanceof HeosGroupHandler) {
                channelIdentifier = "G" + childThing.getUID().getId();
                if (groupId == null) {
                    pid = ((HeosGroupHandler) handler).getId();
                } else {
                    pid = groupId;
                }
            }
            Map<String, String> properties = new HashMap<>();
            String playerName = childThing.getLabel();
            playerName = playerName == null ? pid : playerName;
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelIdentifier);
            properties.put(PROP_NAME, playerName);
            properties.put(PID, pid);

            Channel channel = ChannelBuilder.create(channelUID, "Switch").withLabel(playerName).withType(CH_TYPE_PLAYER)
                    .withProperties(properties).build();
            updateThingChannels(channelManager.addSingleChannel(channel));
        } catch (HeosNotFoundException e) {
            logger.debug("Group is not yet initialized fully", e);
        }
    }

    public void addGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.put(handler.getGroupMemberHash(), handler);
    }

    private void removeGroupHandlerInformation(HeosGroupHandler handler) {
        groupHandlerMap.remove(handler.getGroupMemberHash());
    }

    public @Nullable String getActualGID(String groupHash) {
        return hashToGidMap.get(groupHash);
    }

    @Override
    public void playerStateChangeEvent(HeosEventObject eventObject) {
        // do nothing
    }

    @Override
    public <T> void playerStateChangeEvent(HeosResponseObject<T> responseObject) {
        // do nothing
    }

    @Override
    public void playerMediaChangeEvent(String pid, Media media) {
        // do nothing
    }

    @Override
    public void bridgeChangeEvent(String event, boolean success, Object command) {
        if (EVENT_TYPE_EVENT.equals(event)) {
            if (HeosEvent.PLAYERS_CHANGED.equals(command)) {
                triggerPlayerDiscovery();
            } else if (HeosEvent.GROUPS_CHANGED.equals(command)) {
                triggerPlayerDiscovery();
            } else if (CONNECTION_LOST.equals(command)) {
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                logger.debug("HEOS Bridge OFFLINE");
            } else if (CONNECTION_RESTORED.equals(command)) {
                initialize();
            }
        }
        if (EVENT_TYPE_SYSTEM.equals(event) && HeosEvent.USER_CHANGED == command) {
            if (success && !loggedIn) {
                loggedIn = true;
            }
        }
    }

    private void updateThingChannels(List<Channel> channelList) {
        ThingBuilder thingBuilder = editThing();
        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
    }

    public @Nullable Map<Integer, Player> getNewPlayers() {
        try {
            return getApiConnection().getNewPlayers();
        } catch (IOException | ReadException e) {
            return null;
        }
    }

    public Map<Integer, Player> getRemovedPlayers() {
        try {
            return getApiConnection().getRemovedPlayers();
        } catch (HeosNotConnectedException e) {
            return Collections.emptyMap();
        }
    }

    public @Nullable Map<String, Group> getNewGroups() {
        try {
            return getApiConnection().getNewGroups();
        } catch (IOException | ReadException e) {
            return null;
        }
    }

    public Map<String, Group> getRemovedGroups() {
        try {
            return getApiConnection().getRemovedGroups();
        } catch (HeosNotConnectedException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * The list with the currently selected player
     *
     * @return a HashMap which the currently selected player
     */
    public Map<String, String> getSelectedPlayer() {
        return selectedPlayerList.stream().collect(Collectors.toMap(a -> a[0], a -> a[1], (a, b) -> a));
    }

    public List<String[]> getSelectedPlayerList() {
        return selectedPlayerList;
    }

    public void setSelectedPlayerList(List<String[]> selectedPlayerList) {
        this.selectedPlayerList = selectedPlayerList;
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isBridgeConnected() {
        HeosFacade connection = apiConnection;
        return connection != null && connection.isConnected();
    }

    public HeosFacade getApiConnection() throws HeosNotConnectedException {
        HeosFacade localApiConnection = apiConnection;
        if (localApiConnection != null) {
            return localApiConnection;
        } else {
            throw new HeosNotConnectedException();
        }
    }
}

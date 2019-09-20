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

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.configuration.GroupConfiguration;
import org.openhab.binding.heos.internal.resources.HeosConstants;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;

/**
 * The {@link HeosGroupHandler} handles the actions for a HEOS group.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosGroupHandler extends HeosThingBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(HeosGroupHandler.class);

    private String gid;
    private HeosGroup heosGroup = new HeosGroup();
    private GroupConfiguration configuration;

    private boolean blockInitialization;

    public HeosGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The GID is null if there is no group online with the groupMemberHash
        // Only commands from the UNGROUP channel are passed through
        // to activate the group if it is offline
        if (gid != null || CH_ID_UNGROUP.equals(channelUID.getId())) {
            super.handleCommand(channelUID, command);
        }
    }

    /**
     * Initialize the HEOS group. Starts an extra thread to avoid blocking
     * during start up phase. Gathering all information can take longer
     * than 5 seconds which can throw an error within the OpenHab system.
     */
    @Override
    public synchronized void initialize() {
        super.initialize();

        configuration = thing.getConfiguration().as(GroupConfiguration.class);

        // Prevents that initialize() is called multiple times if group goes online
        blockInitialization = true;
        if (thing.getStatus().equals(ThingStatus.ONLINE)) {
            return;
        }
        // Generates the groupMember from the properties. Is needed to generate group after restart of OpenHab.
        heosGroup.updateGroupPlayers(configuration.members);

        // TODO this is not nice
        getApi().registerForChangeEvents(this);
        scheduledStartUp();
    }

    @Override
    protected String getId() {
        return gid;
    }

    public String getGroupMemberHash() {
        return heosGroup.getGroupMemberHash();
    }

    @Override
    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(heosGroup.getLevel());
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        // TODO this is not nice
        getApi().volumeGroup(volume.toString(), gid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (getThing().getStatus().equals(ThingStatus.UNINITIALIZED)) {
            logger.debug("Can't Handle Event. Group {} not initialized. Status is: {}", getConfig().get(PROP_NAME),
                    getThing().getStatus().toString());
            return;
        }
        if (pid.equals(gid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        if (pid.equals(gid)) {
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (HeosConstants.USER_CHANGED.equals(command)) {
            // TODO this is not nice
            updateThingChannels(channelManager.addFavoriteChannels(getApi().getFavorites()));
        }
    }

    /**
     * Sets the status of the HEOS group to OFFLINE.
     * Also sets the UNGROUP channel to OFF and the CONTROL
     * channel to PAUSE
     */
    @Override
    public void setStatusOffline() {
        // TODO this is not nice
        getApi().unregisterForChangeEvents(this);
        updateState(CH_ID_UNGROUP, OnOffType.OFF);
        updateState(CH_ID_CONTROL, PlayPauseType.PAUSE);
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void setStatusOnline() {
        if (thing.getStatus().equals(ThingStatus.OFFLINE) && !blockInitialization) {
            initialize();
        }
    }

    public HeosGroup getHeosGroup() {
        return heosGroup;
    }

    public String getGroupID() {
        return gid;
    }

    private void updateConfiguration() {
        Map<String, Object> prop = new HashMap<>();
        prop.put(PROP_NAME, heosGroup.getName());
        prop.put(PROP_GROUP_MEMBERS, heosGroup.getGroupMembersAsString());
        prop.put(PROP_GROUP_LEADER, heosGroup.getLeader());
        prop.put(PROP_GROUP_HASH, heosGroup.getGroupMemberHash());
        prop.put(PROP_GID, gid);
        Configuration conf = editConfiguration();
        conf.setProperties(prop);
        updateConfiguration(conf);
    }

    private void scheduledStartUp() {
        scheduler.schedule(() -> {
            bridge.addGroupHandlerInformation(this);
            // Checks if there is a group online with the same group member hash.
            // If not setting the group offline.
            gid = bridge.getActualGID(heosGroup.getGroupMemberHash());
            if (gid == null) {
                blockInitialization = false;
                setStatusOffline();
            } else {
                heosGroup.setGid(gid);
                heosGroup = getHeosSystem().getGroupState(heosGroup);
                getHeosSystem().addHeosGroupToOldGroupMap(heosGroup.getGroupMemberHash(), heosGroup);
                if (bridge.isLoggedin()) {
                    // TODO this is not nice
                    updateThingChannels(channelManager.addFavoriteChannels(getApi().getFavorites()));
                }
                updateConfiguration();
                updateStatus(ThingStatus.ONLINE);
                updateState(CH_ID_UNGROUP, OnOffType.ON);
                blockInitialization = false;
            }
        }, 4, TimeUnit.SECONDS);
    }
}

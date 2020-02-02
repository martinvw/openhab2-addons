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

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute.PLAYER_ID;
import static org.openhab.binding.heos.internal.json.dto.HeosEvent.GROUP_VOLUME_CHANGED;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.configuration.PlayerConfiguration;
import org.openhab.binding.heos.internal.exception.HeosNotConnectedException;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosPlayerHandler extends HeosThingBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(HeosPlayerHandler.class);

    private @NonNullByDefault({}) String pid;
    private @Nullable ScheduledFuture<?> scheduledFuture;

    public HeosPlayerHandler(Thing thing, HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        super(thing, heosDynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        HeosChannelHandler channelHandler = getHeosChannelHandler(channelUID);
        if (channelHandler != null) {
            try {
                channelHandler.handlePlayerCommand(command, getId(), thing.getUID());
                handleSuccess();
            } catch (IOException | ReadException e) {
                handleError(e);
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        PlayerConfiguration configuration = thing.getConfiguration().as(PlayerConfiguration.class);
        pid = configuration.pid;

        delayedInitialize();
    }

    private void delayedInitialize() {
        scheduledFuture = scheduler.schedule(() -> {
            try {
                refreshPlayState(pid);

                handleThingStateUpdate(getApiConnection().getPlayerInfo(pid));

                updateStatus(ThingStatus.ONLINE);
            } catch (IOException | ReadException e) {
                logger.debug("Failed to initialize, will try again", e);
                delayedInitialize();
            }
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    void refreshPlayState(String id) throws IOException, ReadException {
        super.refreshPlayState(id);

        handleThingStateUpdate(getApiConnection().getPlayerMuteState(id));
        handleThingStateUpdate(getApiConnection().getPlayerVolume(id));
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localStartupFuture = scheduledFuture;
        if (localStartupFuture != null && !localStartupFuture.isCancelled()) {
            localStartupFuture.cancel(true);
        }

        super.dispose();
    }

    @Override
    public String getId() {
        return pid;
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
    }

    @Override
    public void playerStateChangeEvent(HeosEventObject eventObject) {
        if (!pid.equals(eventObject.getAttribute(PLAYER_ID))) {
            return;
        }

        if (GROUP_VOLUME_CHANGED.equals(eventObject.command)) {
            logger.debug("Ignoring group-volume changes for players");
            return;
        }

        handleThingStateUpdate(eventObject);
    }

    @Override
    public <T> void playerStateChangeEvent(HeosResponseObject<T> responseObject) {
        if (!pid.equals(responseObject.getAttribute(PLAYER_ID))) {
            return;
        }

        handleThingStateUpdate(responseObject);
    }

    @Override
    public void playerMediaChangeEvent(String eventPid, Media media) {
        if (!pid.equals(eventPid)) {
            return;
        }

        handleThingMediaUpdate(media);
    }

    @Override
    public void setStatusOffline() {
        logger.warn("Status was set offline");
        try {
            getApiConnection().unregisterForChangeEvents(this);
        } catch (HeosNotConnectedException e) {
            logger.debug("Failed to unregister because the connection could not be fetched");
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void setStatusOnline() {
        this.initialize();
    }

    public static void propertiesFromPlayer(Map<String, ? super String> prop, Player player) {
        prop.put(PROP_NAME, player.name);
        prop.put(PROP_PID, String.valueOf(player.playerId));
        prop.put(Thing.PROPERTY_MODEL_ID, player.model);
        prop.put(Thing.PROPERTY_FIRMWARE_VERSION, player.version);
        prop.put(PROP_NETWORK, player.network);
        prop.put(PROP_IP, player.ip);
        String serialNumber = player.serial;
        if (serialNumber != null) {
            prop.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        }
    }
}

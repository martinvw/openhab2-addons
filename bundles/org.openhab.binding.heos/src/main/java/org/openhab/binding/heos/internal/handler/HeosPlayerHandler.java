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

import static org.openhab.binding.heos.internal.HeosBindingConstants.PROP_PID;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.api.HeosSystem;
import org.openhab.binding.heos.internal.configuration.PlayerConfiguration;
import org.openhab.binding.heos.internal.resources.HeosConstants;
import org.openhab.binding.heos.internal.resources.HeosPlayer;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosPlayerHandler extends HeosThingBaseHandler {

    private String pid;
    private HeosPlayer player = new HeosPlayer();

    public HeosPlayerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
        super.initialize();

        PlayerConfiguration configuration = thing.getConfiguration().as(PlayerConfiguration.class);

        pid = configuration.pid;

        // TODO this is not nice
        // Because initialization can take longer a scheduler with an extra thread is created
        scheduler.schedule(() -> {
            // TODO this is not nice
            player = getApi().getPlayerState(pid);
            if (!player.isOnline()) {
                setStatusOffline();
                return;
            }
            // Adding the favorite channel to the player
            if (bridge.isLoggedin()) {
                updateThingChannels(channelManager.addFavoriteChannels(getApi().getFavorites()));
            }

            updateStatus(ThingStatus.ONLINE);
        }, 3, TimeUnit.SECONDS);
    }

    @Override
    protected String getId() {
        return pid;
    }

    @Override
    public PercentType getNotificationSoundVolume() {
        return PercentType.valueOf(player.getLevel());
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
        getApi().setVolume(volume.toString(), pid);
    }

    @Override
    public void playerStateChangeEvent(String pid, String event, String command) {
        if (this.pid.equals(pid)) {
            handleThingStateUpdate(event, command);
        }
    }

    @Override
    public void playerMediaChangeEvent(String pid, Map<String, String> info) {
        if (this.pid.equals(pid)) {
            player.updateMediaInfo(info);
            handleThingMediaUpdate(info);
        }
    }

    @Override
    public void bridgeChangeEvent(String event, String result, String command) {
        if (HeosConstants.USER_CHANGED.equals(command)) {
            updateThingChannels(channelManager.addFavoriteChannels(getApi().getFavorites()));
        }
    }

    @Override
    public void setStatusOffline() {
        getApi().unregisterForChangeEvents(this);
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void setStatusOnline() {
        this.initialize();
    }
}

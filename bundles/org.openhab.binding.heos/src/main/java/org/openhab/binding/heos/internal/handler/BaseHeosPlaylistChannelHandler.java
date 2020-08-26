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

import static org.openhab.binding.heos.internal.HeosBindingConstants.CH_ID_PLAYLISTS;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.heos.internal.exception.HeosNotFoundException;
import org.openhab.binding.heos.internal.resources.Telnet;

/**
 * The {@link BaseHeosPlaylistChannelHandler} provides an abstraction layer for handling of playlist commands.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHeosPlaylistChannelHandler extends BaseHeosChannelHandler {
    protected final HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider;

    public BaseHeosPlaylistChannelHandler(HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider,
            HeosBridgeHandler bridge) {
        super(bridge);
        this.heosDynamicStateDescriptionProvider = heosDynamicStateDescriptionProvider;
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid, @Nullable Configuration configuration)
            throws IOException, Telnet.ReadException {
        handleCommand(command, id, uid, configuration);
    }

    @Override
    public void handleGroupCommand(Command command, @Nullable String id, ThingUID uid,
            @Nullable Configuration configuration, HeosGroupHandler heosGroupHandler)
            throws IOException, Telnet.ReadException {
        if (id == null) {
            throw new HeosNotFoundException();
        }

        handleCommand(command, id, uid, configuration);
    }

    protected abstract void handleCommand(Command command, String id, ThingUID uid,
            @Nullable Configuration configuration) throws IOException, Telnet.ReadException;

    protected void handleRefresh(ThingUID uid) throws IOException, Telnet.ReadException {
        ChannelUID channelUID = new ChannelUID(uid, CH_ID_PLAYLISTS);
        heosDynamicStateDescriptionProvider.setPlaylists(channelUID, getApi().getPlaylists());
    }

    protected String getIdentifier(ThingUID uid, String requestedPlaylist) {
        ChannelUID channelUID = new ChannelUID(uid, CH_ID_PLAYLISTS);
        return heosDynamicStateDescriptionProvider.getValueByLabel(channelUID, requestedPlaylist);
    }
}

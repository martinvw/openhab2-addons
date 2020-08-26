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

import static org.openhab.binding.heos.internal.resources.HeosConstants.PLAYLISTS_SID;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.profile.HeosPlayCommandType;
import org.openhab.binding.heos.internal.resources.AddCriteria;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelHandlerPlaylists} handles the playlist selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerPlaylists extends BaseHeosPlaylistChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(HeosChannelHandlerPlaylists.class);

    public HeosChannelHandlerPlaylists(HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider,
            HeosBridgeHandler bridge) {
        super(heosDynamicStateDescriptionProvider, bridge);
    }

    @Override
    protected void handleCommand(Command command, String id, ThingUID uid, @Nullable Configuration configuration)
            throws IOException, ReadException {
        if (command instanceof RefreshType) {
            handleRefresh(uid);
            return;
        }

        logger.debug("Handling playlist command: {}", command);

        String requestedPlaylist;
        AddCriteria criteria = AddCriteria.PLAY_NOW;
        if (command instanceof HeosPlayCommandType) {
            HeosPlayCommandType heosPlayCommandType = (HeosPlayCommandType) command;
            requestedPlaylist = heosPlayCommandType.getRequestedPlaylist();
            criteria = heosPlayCommandType.getCriteria();
        } else {
            requestedPlaylist = command.toString();
        }

        String idCommand = getIdentifier(uid, requestedPlaylist);
        getApi().addContainerToQueue(id, PLAYLISTS_SID, idCommand, criteria);
    }
}

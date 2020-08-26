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
import org.openhab.binding.heos.internal.resources.AddCriteria;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelHandlerSinglePlaylist} provides a way to start a dedicated HEOS playlist.
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerSinglePlaylist extends BaseHeosPlaylistChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(HeosChannelHandlerSinglePlaylist.class);

    public HeosChannelHandlerSinglePlaylist(HeosDynamicStateDescriptionProvider stateDescriptionProvider,
            HeosBridgeHandler bridgeHandler) {
        super(stateDescriptionProvider, bridgeHandler);
    }

    @Override
    protected void handleCommand(Command command, String id, ThingUID uid, @Nullable Configuration configuration)
            throws IOException, Telnet.ReadException {
        if (command instanceof RefreshType) {
            handleRefresh(uid);
            return;
        }

        if (configuration == null || configuration.get("playlist") == null) {
            logger.warn("Playlist configuration is missing for {}", uid);
            return;
        }

        try {
            AddCriteria criteria = AddCriteria.valueOf(command.toString());

            String idCommand = getIdentifier(uid, (String) configuration.get("playlist"));
            getApi().addContainerToQueue(id, PLAYLISTS_SID, idCommand, criteria);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to map requested action: {}", command);
        }
    }
}

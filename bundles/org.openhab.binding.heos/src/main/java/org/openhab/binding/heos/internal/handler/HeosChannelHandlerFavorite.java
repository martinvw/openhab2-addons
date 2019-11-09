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

import static org.openhab.binding.heos.internal.HeosBindingConstants.CH_ID_FAVORITES;
import static org.openhab.binding.heos.internal.resources.HeosConstants.FAVORITE_SID;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;

/**
 * The {@link HeosChannelHandlerFavorite} handles the playlist selection channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerFavorite extends BaseHeosChannelHandler {
    private final HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider;

    public HeosChannelHandlerFavorite(HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider,
            HeosBridgeHandler bridge) {
        super(bridge);
        this.heosDynamicStateDescriptionProvider = heosDynamicStateDescriptionProvider;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        handleCommand(command, id, uid);
    }

    @Override
    public void handleGroupCommand(Command command, String id, ThingUID uid, HeosGroupHandler heosGroupHandler)
            throws IOException, ReadException {
        handleCommand(command, id, uid);
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }

    private void handleCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            heosDynamicStateDescriptionProvider.setFavorites(new ChannelUID(uid, CH_ID_FAVORITES),
                    getApi().getFavorites());
            return;
        }

        getApi().playStream(id, FAVORITE_SID, command.toString());
    }
}

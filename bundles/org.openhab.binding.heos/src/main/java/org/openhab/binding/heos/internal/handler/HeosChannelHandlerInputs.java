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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosChannelHandlerInputs} handles the Input channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerInputs extends BaseHeosChannelHandler {
    protected final Logger logger = LoggerFactory.getLogger(HeosChannelHandlerInputs.class);

    private final HeosEventListener eventListener;

    public HeosChannelHandlerInputs(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        handleCommand(command, id);
    }

    @Override
    public void handleGroupCommand(Command command, String id, ThingUID uid, HeosGroupHandler heosGroupHandler)
            throws IOException, ReadException {
        handleCommand(command, id);
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }

    private void handleCommand(Command command, String id) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            @Nullable
            Media payload = getApi().getNowPlayingMedia(id).payload;
            if (payload != null) {
                eventListener.playerMediaChangeEvent(id, payload);
            }
            return;
        }

        if (bridge.getSelectedPlayer().isEmpty()) {
            getApi().playInputSource(id, command.toString());
        } else if (bridge.getSelectedPlayer().size() > 1) {
            logger.debug("Only one source can be selected for HEOS Input. Selected amount of sources: {} ",
                    bridge.getSelectedPlayer().size());
        } else {
            for (String sourcePid : bridge.getSelectedPlayer().keySet()) {
                getApi().playInputSource(id, sourcePid, command.toString());
            }
        }
        bridge.getSelectedPlayer().clear();
    }
}

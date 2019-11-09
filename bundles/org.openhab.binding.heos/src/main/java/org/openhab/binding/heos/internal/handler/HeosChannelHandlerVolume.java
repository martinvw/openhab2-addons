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
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.heos.internal.resources.HeosEventListener;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;

/**
 * The {@link HeosChannelHandlerVolume} handles the Volume channel command
 * from the implementing thing.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosChannelHandlerVolume extends BaseHeosChannelHandler {
    private final HeosEventListener eventListener;

    public HeosChannelHandlerVolume(HeosEventListener eventListener, HeosBridgeHandler bridge) {
        super(bridge);
        this.eventListener = eventListener;
    }

    @Override
    public void handlePlayerCommand(Command command, String id, ThingUID uid) throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getHeosPlayerVolume(id));
            return;
        }
        if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE == command) {
                getApi().increaseVolume(id);
            } else {
                getApi().decreaseVolume(id);
            }
        } else {
            getApi().setVolume(command.toString(), id);
        }
    }

    @Override
    public void handleGroupCommand(Command command, String id, ThingUID uid, HeosGroupHandler heosGroupHandler)
            throws IOException, ReadException {
        if (command instanceof RefreshType) {
            eventListener.playerStateChangeEvent(getApi().getHeosGroupVolume(id));
            return;
        }
        if (command instanceof IncreaseDecreaseType) {
            if (IncreaseDecreaseType.INCREASE == command) {
                getApi().increaseGroupVolume(id);
            } else {
                getApi().decreaseGroupVolume(id);
            }
        } else {
            getApi().volumeGroup(command.toString(), id);
        }
    }

    @Override
    public void handleBridgeCommand(Command command, ThingUID uid) {
        // not used on bridge
    }
}

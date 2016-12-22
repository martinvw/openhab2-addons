/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;

public enum PacketType {
    HOMEDUINO_ACK(null), // TODO map this properly
    HOMEDUINO_ERROR(null), // TODO map this properly

    SWITCH1(Switch1Message.class),
    SWITCH2(Switch2Message.class),
    SWITCH4(Switch4Message.class),
    DIMMER1(Dimmer1Message.class),
    PIR1(Pir1Message.class),
    SHUTTER3(Shutter3Message.class),

    UNKNOWN(null);

    private final Class<? extends RFXComHomeduinoMessage> messageClazz;

    PacketType(Class<? extends RFXComHomeduinoMessage> clazz) {
        this.messageClazz = clazz;
    }

    public Class<? extends RFXComHomeduinoMessage> getMessageClass() throws RFXComNotImpException {
        if (messageClazz == null) {
            throw new RFXComNotImpException(this.name() + "is not implemented");
        }
        return messageClazz;
    }
}
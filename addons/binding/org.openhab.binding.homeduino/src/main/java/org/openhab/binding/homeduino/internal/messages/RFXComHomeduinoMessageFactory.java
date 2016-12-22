/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.internal.exceptions.RFXComException;
import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class RFXComHomeduinoMessageFactory {
    private static final Map<Class<? extends HomeduinoProtocol>, PacketType> MAP = Collections
            .unmodifiableMap(new HashMap<Class<? extends HomeduinoProtocol>, PacketType>() {
                {
                    put(Dimmer1Message.Protocol.class, PacketType.DIMMER1);
                    put(Pir1Message.Protocol.class, PacketType.PIR1);
                    put(Shutter3Message.Protocol.class, PacketType.SHUTTER3);
                    put(Switch1Message.Protocol.class, PacketType.SWITCH1);
                    put(Switch2Message.Protocol.class, PacketType.SWITCH2);
                    put(Switch4Message.Protocol.class, PacketType.SWITCH4);
                }
            });

    static RFXComMessage createMessage(HomeduinoProtocol.Result result) throws RFXComNotImpException, RFXComException {

        PacketType packetType = getPacketType(result);

        try {
            Class<? extends RFXComMessage> clazz = packetType.getMessageClass();
            try {
                Constructor<? extends RFXComMessage> c = clazz.getConstructor(HomeduinoProtocol.Result.class);
                return c.newInstance(result);
            } catch (NoSuchMethodException e) {
                Constructor<? extends RFXComMessage> c = clazz.getConstructor();
                return c.newInstance();
            }
        } catch (Exception e) {
            throw new RFXComException(e);
        }
    }

    private static PacketType getPacketType(HomeduinoProtocol.Result result) {
        return MAP.get(result.getProtocol().getClass());
    }
}

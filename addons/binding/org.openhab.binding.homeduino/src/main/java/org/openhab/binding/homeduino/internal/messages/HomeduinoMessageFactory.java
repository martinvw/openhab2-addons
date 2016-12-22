/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.internal.exceptions.RFXComException;
import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class HomeduinoMessageFactory {
    public static HomeduinoMessage createMessage(byte[] packet) throws RFXComNotImpException, RFXComException {
        PacketTypeHomeduino packetTypeHomeduino = getPacketType(Arrays.copyOfRange(packet, 0, 3));

        try {
            Class<? extends HomeduinoMessage> clazz = packetTypeHomeduino.getMessageClass();
            try {
                Constructor<? extends HomeduinoMessage> c = clazz.getConstructor(byte[].class);
                return c.newInstance(packet);
            } catch (NoSuchMethodException e) {
                Constructor<? extends HomeduinoMessage> c = clazz.getConstructor();
                return c.newInstance();
            }
        } catch (Exception e) {
            throw new RFXComException(e);
        }
    }

    private static PacketTypeHomeduino getPacketType(byte[] copyOfRange) {
        return HomeduinoBaseMessage.valueOfString(new String(copyOfRange));
    }
}

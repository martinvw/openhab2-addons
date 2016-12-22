/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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

public class RFXComMessageFactory {
    public static RFXComHomeduinoMessage createMessage(PacketType packetType) throws RFXComException, RFXComNotImpException {

        try {
            Class<? extends RFXComHomeduinoMessage> clazz = packetType.getMessageClass();
            Constructor<? extends RFXComHomeduinoMessage> c = clazz.getConstructor();
            return c.newInstance();
        } catch (RFXComNotImpException e) {
            throw e;
        } catch (Exception e) {
            throw new RFXComException(e);
        }
    }

    public static PacketType convertPacketType(String packetType) throws IllegalArgumentException {
        for (PacketType p : PacketType.values()) {
            if (p.toString().replace("_", "").equals(packetType.replace("_", ""))) {
                return p;
            }
        }

        throw new IllegalArgumentException("Unknown packet type " + packetType);
    }

}

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

import static org.openhab.binding.homeduino.internal.messages.ResponseType.HOMEDUINO_RF_EVENT;

public class HomeduinoMessageFactory {
    public static HomeduinoMessage createMessage(byte[] packet) throws RFXComNotImpException, RFXComException {
        ResponseType responseType = getResponseType(Arrays.copyOfRange(packet, 0, 3));

        try {
            Class<? extends HomeduinoMessage> clazz = responseType.getMessageClass();

            if (responseType == HOMEDUINO_RF_EVENT) {
                Constructor<? extends HomeduinoMessage> c = clazz.getConstructor(byte[].class);
                return c.newInstance(packet);
            } else {
                return clazz.newInstance();
            }
        } catch (Exception e) {
            throw new RFXComException(e);
        }
    }

    private static ResponseType getResponseType(byte[] copyOfRange) {
        return ResponseType.valueOfString(new String(copyOfRange));
    }
}

/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class HomeduinoBaseMessage implements HomeduinoMessage {
    private static final Map<String, PacketTypeHomeduino> MAP = Collections.unmodifiableMap(new HashMap<String, PacketTypeHomeduino>() {
        {
            put("rea", PacketTypeHomeduino.HOMEDUINO_READY);
            put("ACK", PacketTypeHomeduino.HOMEDUINO_ACK);
            put("ERR", PacketTypeHomeduino.HOMEDUINO_ERROR);
            put("RF ", PacketTypeHomeduino.HOMEDUINO_RF_EVENT);
        }
    });

    static PacketTypeHomeduino valueOfString(String value) {
        PacketTypeHomeduino p = PacketTypeHomeduino.UNKNOWN;
        if (MAP.containsKey(value)) {
            p = MAP.get(value);
        }

        return p;
    }
}

/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

public enum PacketTypeHomeduino {
    HOMEDUINO_READY(HomeduinoReadyMessage.class),
    HOMEDUINO_ACK(HomeduinoAcknowledgementMessage.class),
    HOMEDUINO_ERROR(HomeduinoErrorMessage.class),
    HOMEDUINO_RF_EVENT(HomeduinoEventMessage.class),
    UNKNOWN(null);

    private final Class<? extends HomeduinoMessage> messageClazz;

    PacketTypeHomeduino(Class<? extends HomeduinoMessage> clazz) {
        this.messageClazz = clazz;
    }

    public Class<? extends HomeduinoMessage> getMessageClass() {
        return messageClazz;
    }
}

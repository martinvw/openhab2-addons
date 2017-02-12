/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

public enum ResponseType {
    HOMEDUINO_READY(HomeduinoReadyMessage.class, "rea"),
    HOMEDUINO_ACK(HomeduinoAcknowledgementMessage.class, "ACK"),
    HOMEDUINO_ERROR(HomeduinoErrorMessage.class, "ERR"),
    HOMEDUINO_RF_EVENT(HomeduinoEventMessage.class, "RF ");

    private final Class<? extends HomeduinoMessage> messageClazz;
    private final String messagePrefix;

    ResponseType(Class<? extends HomeduinoMessage> clazz, String messagePrefix) {
        this.messageClazz = clazz;
        this.messagePrefix = messagePrefix;
    }

    public Class<? extends HomeduinoMessage> getMessageClass() {
        return messageClazz;
    }

    static ResponseType valueOfString(String messagePrefix) {
        for (ResponseType packetType : values()) {
            if (packetType.messagePrefix.equals(messagePrefix)) {
                return packetType;
            }
        }

        throw new IllegalStateException("Invalid response received");
    }
}

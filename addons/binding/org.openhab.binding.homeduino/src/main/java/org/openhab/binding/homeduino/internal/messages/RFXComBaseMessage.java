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

/**
 * Base class for RFXCOM data classes. All other data classes should extend this class.
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class RFXComBaseMessage implements RFXComMessage {

    public final static String ID_DELIMITER = ".";

    private PacketType packetType = PacketType.UNKNOWN;
    public byte subType = 0;
    private byte seqNbr = 0;
    private byte id1 = 0;
    private byte id2 = 0;

    public RFXComBaseMessage() {

    }

    public RFXComBaseMessage(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public void encodeMessage(byte[] data) {

    }

    @Override
    public String toString() {
        String str = "";

        str += ", Packet type = " + packetType;
        str += ", Seq number = " + (short) (seqNbr & 0xFF);

        return str;
    }

    @Override
    public String getDeviceId() {
        return id1 + ID_DELIMITER + id2;
    }

    @Override
    public PacketType getPacketType() {
        return this.packetType;
    }

    @Override
    public Object convertSubType() throws RFXComException {
        return convertSubType(String.valueOf(this.subType));
    }

}

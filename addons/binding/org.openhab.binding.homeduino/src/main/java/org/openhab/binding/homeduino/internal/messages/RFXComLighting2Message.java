/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;

import java.util.Arrays;
import java.util.List;

/**
 * RFXCOM data class for lighting2 message.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComLighting2Message extends RFXComBaseMessage {
    public enum Commands {
        OFF(0),
        ON(1),
        SET_LEVEL(2),
        GROUP_OFF(3),
        GROUP_ON(4),

        UNKNOWN(255);

        Commands(int command) {
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.COMMAND, RFXComValueSelector.DIMMING_LEVEL,
            RFXComValueSelector.CONTACT);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.COMMAND, RFXComValueSelector.DIMMING_LEVEL);

    int sensorId = 0;
    byte unitCode = 0;
    public Commands command = Commands.UNKNOWN;
    byte signalLevel = 0;

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command;
        byte dimmingLevel = 0;
        str += ", Dim level = " + dimmingLevel;
        str += ", Signal level = " + signalLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

    }

    @Override
    public byte[] decodeMessage() {
return null;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {
    return null;
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {

    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {
        return null;
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }

}

/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;

import java.math.BigDecimal;

abstract public class RFXComHomeduinoMessage implements RFXComMessage {
    private HomeduinoProtocol.Result result;
    Command command;

    RFXComHomeduinoMessage() {
        // deliberately empty
        this.command = new Command();
    }

    RFXComHomeduinoMessage(HomeduinoProtocol.Result result) {
        this.result = result;
    }

    @Override
    public void encodeMessage(byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] decodeMessage() {
        throw new UnsupportedOperationException();
    }

    public String decodeToHomeduinoMessage(int transmitterPin) {
        return getProtocol().decode(command, transmitterPin);
    }

    abstract HomeduinoProtocol getProtocol();

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {
        if (valueSelector == RFXComValueSelector.DIMMING_LEVEL) {
            return getPercentTypeFromDimLevel(result.getDimlevel());
        } else if (valueSelector == RFXComValueSelector.COMMAND) {
            return result.getState() == 0 ? OnOffType.OFF : OnOffType.ON;
        } else if (valueSelector == RFXComValueSelector.CONTACT) {
            return result.getState() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        } else if (valueSelector == RFXComValueSelector.SHUTTER) {
            if (result.getState() == 1) {
                return UpDownType.UP;
            } else if (result.getState() == 3) {
                return UpDownType.DOWN;
            } else {
                return UnDefType.UNDEF;
            }
        }

        throw new RFXComException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());
    }


    /**
     * Convert a 0-15 scale value to a percent type.
     *
     * @param value
     *            percent type to convert
     * @return converted value 0-15
     */
    public static PercentType getPercentTypeFromDimLevel(int value) {
        value = Math.min(value, 15);

        return new PercentType(BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(15), 0, BigDecimal.ROUND_UP).intValue());
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {
        if (!getSupportedOutputValueSelectors().contains(valueSelector)) {
            throw new RFXComException("Can't convert " + type + " to " + valueSelector);
        }

        command.convertFromState(valueSelector, type);
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {
        return null;
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        command.setSubType(subType);
    }

    @Override
    public String getDeviceId() throws RFXComException {
        return result.getId() + "." + result.getUnit();
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        command.setDeviceId(deviceId);
    }

    @Override
    public Object convertSubType() throws RFXComException {
        return getPacketType();
    }
}

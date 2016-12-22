/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;

public class Command {
    private int sensorId;
    private String unitCode;
    private boolean group;

    private RFXComValueSelector valueSelector;
    private Type command;

    void setDeviceId(String deviceId) {
        String[] parts = deviceId.split("\\.");
        sensorId = Integer.parseInt(parts[0]);
        unitCode = parts[1];
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    void setSubType(Object subType) {
        // we don't care about sub types
    }

    public int getSensorId() {
        return sensorId;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public int getUnitCodeAsInt() {
        return Integer.parseInt(unitCode);
    }

    public boolean isGroup() {
        return group;
    }

    public RFXComValueSelector getValueSelector() {
        return valueSelector;
    }

    public Type getCommand() {
        return command;
    }

    void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {
        this.valueSelector = valueSelector;
        this.command = type;
    }
}

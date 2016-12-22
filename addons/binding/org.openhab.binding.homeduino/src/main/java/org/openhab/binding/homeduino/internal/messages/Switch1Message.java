/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;

import java.util.Arrays;
import java.util.List;

// TODO add file / class level docblocks!!!
public class Switch1Message extends RFXComHomeduinoMessage implements RFXComMessage {
    public Switch1Message() {
        // deliberately empty
    }

    public Switch1Message(HomeduinoProtocol.Result result) {
        super(result);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SWITCH1;
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() {
        return Arrays.asList(RFXComValueSelector.COMMAND, RFXComValueSelector.CONTACT);
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() {
        return Arrays.asList(RFXComValueSelector.COMMAND);
    }

    @Override
    HomeduinoProtocol getProtocol() {
        return new Protocol();
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        super.setDeviceId(deviceId);

        if ("0".equals(command.getUnitCode())) {
            command.setGroup(true);
        }
    }

    public static final class Protocol extends HomeduinoCoCo2 {
        private static final int PULSE_COUNT = 132;

        public Protocol() {
            super(PULSE_COUNT);
        }
    }
}

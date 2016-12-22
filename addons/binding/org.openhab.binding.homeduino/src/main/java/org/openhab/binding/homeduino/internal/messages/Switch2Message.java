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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Switch2Message extends RFXComHomeduinoMessage implements RFXComMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Switch2Message.class);
    private static final int[] PULSE_LENGTHS = { 306, 957, 9808 };
    private static final int PULSE_COUNT = 50;

    public Switch2Message(HomeduinoProtocol.Result result) {
        super(result);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SWITCH2;
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

    public static final class Protocol extends HomeduinoProtocol {
        public Protocol() {
            super(PULSE_COUNT, PULSE_LENGTHS);
        }

        @Override
        public Result process(String pulses) {
            pulses = pulses.replace("02", "");
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < pulses.length(); i += 4) {
                String pulse = pulses.substring(i, i + 4);
                char c;
                if ("0101".equals(pulse)) {
                    c = '1';
                } else {
                    c = '0';
                }

                output.append(c);
            }

            int unit = Integer.parseInt(output.substring(0, 4).toString(), 2);
            int id = Integer.parseInt(output.substring(5, 9).toString(), 2);
            int state = 1 - Integer.parseInt(output.substring(11).toString(), 2);

            LOGGER.warn("state: " + state); // could also indicate presence
            LOGGER.warn("Id: " + id);
            LOGGER.warn("Unitcode: " + unit);

            return new Result(id, unit, state, false, null);
        }
    }
}

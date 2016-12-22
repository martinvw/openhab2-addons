/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.RFXComValueSelector;

import java.util.List;

import static java.util.Collections.singletonList;

public class Shutter3Message extends RFXComHomeduinoMessage implements RFXComMessage {
    private static final int PULSE_COUNT = 82;
    private static final int[] PULSE_LENGTHS = { 366, 736, 1600, 5204, 10896 };

    public Shutter3Message(HomeduinoProtocol.Result result) {
        super(result);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.SHUTTER3;
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() {
        return singletonList(RFXComValueSelector.COMMAND);
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() {
        return singletonList(RFXComValueSelector.SHUTTER);
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
            pulses = pulses.replace("32", "").replace("14", "").replace("04", "");
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < pulses.length(); i += 2) {
                String pulse = pulses.substring(i, i + 2);
                char c;
                if ("01".equals(pulse)) {
                    c = '0';
                } else {
                    c = '1';
                }

                output.append(c);
            }

            int id = Integer.parseInt(output.substring(0, 29), 2);
            int channel = Integer.parseInt(output.substring(29, 32), 2);
            int state = Integer.parseInt(output.substring(33, 36), 2);

            return new Result(id, channel, state, false, null);
        }

    }
}

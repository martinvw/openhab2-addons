/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages.homeduino;

import static org.openhab.binding.homeduino.RFXComValueSelector.HUMIDITY;
import static org.openhab.binding.homeduino.RFXComValueSelector.LOW_BATTERY;
import static org.openhab.binding.homeduino.RFXComValueSelector.TEMPERATURE;

import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.messages.PacketType;
import org.openhab.binding.homeduino.internal.messages.RFXComMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Weather1Message extends RFXComHomeduinoMessage implements RFXComMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Weather1Message.class);

    public Weather1Message() {
        // deliberately empty
    }

    public Weather1Message(Result result) {
        super(result);
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.WEATHER1;
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() {
        return Arrays.asList(TEMPERATURE, HUMIDITY, LOW_BATTERY);
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() {
        return Collections.emptyList();
    }

    @Override
    HomeduinoProtocol getProtocol() {
        return new Protocol();
    }

    public static final class Protocol extends HomeduinoProtocol {
        private static final String POSTFIX = "03";

        private static final int[] PULSE_LENGTHS = {456, 1990, 3940, 9236};
        private static final int PULSE_COUNT = 74;

        private static Map<String, Character> PULSES_TO_BINARY_MAPPING = initializePulseBinaryMapping();

        public Protocol() {
            super(PULSE_COUNT, PULSE_LENGTHS);
        }

        private static Map<String, Character> initializePulseBinaryMapping() {
            Map<String, Character> map = new HashMap<>();
            map.put("01", '0');
            map.put("02", '1');
            return map;
        }

        @Override
        public Result process(String pulses) {
            pulses = pulses.replace(POSTFIX, "");
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < pulses.length(); i += 2) {
                String pulse = pulses.substring(i, i + 2);
                output.append(map(PULSES_TO_BINARY_MAPPING, pulse));
            }

            int id = Integer.parseInt(output.substring(4, 12), 2);
            int channel = Integer.parseInt(output.substring(14, 16), 2) + 1;
            double temperature = (double) Integer.parseInt(output.substring(16, 28), 2) / 10;
            int humidity = Integer.parseInt(output.substring(28, 36), 2);
            boolean lowBattery = Character.getNumericValue(output.charAt(12)) == 0;

            return new Result.Builder(getClass(), id, channel)
                    .withTemperature(temperature)
                    .withHumidity(humidity)
                    .withLowBattery(lowBattery)
                    .build();
        }

        @Override
        public String decode(Command command, int transmitterPin) {
            throw new IllegalArgumentException("Cannot send weather via openHAB");
        }

    }
}

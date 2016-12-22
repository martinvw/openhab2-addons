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
import org.eclipse.smarthome.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

abstract class HomeduinoCoCo2 extends HomeduinoProtocol {
    private static final String MESSAGE_PREFIX = "RF send ";

    private static final Logger logger = LoggerFactory.getLogger(HomeduinoCoCo2.class);

    private static final String PREFIX = "02";
    private static final String POSTFIX = "03";

    private static int[] PULSE_LENGTHS = { 260, 1300, 2700, 10400 };
    private static Map<String, Character> PULSES_TO_BINARY_MAPPING = initializePulseBinaryMapping();
    private static Map<Character, String> BINARY_TO_PULSE_MAPPING = inverse(PULSES_TO_BINARY_MAPPING);

    private int pulseCount;

    HomeduinoCoCo2(int pulseCount) {
        super(pulseCount, PULSE_LENGTHS);
        this.pulseCount = pulseCount;
    }

    private static Map<String, Character> initializePulseBinaryMapping() {
        Map<String, Character> map = new HashMap<>();
        map.put("0100", '1');
        map.put("0001", '0');
        map.put("0000", 'N');
        return map;
    }

    private static Map<Character, String> inverse(Map<String, Character> input) {
        Map<Character, String> result = new HashMap<>();
        for (Map.Entry<String, Character> entry : input.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    @Override
    public String decode(Command command, int transmitterPi) {
        // first convert it to a binary string
        StringBuilder binary = new StringBuilder(MESSAGE_PREFIX).append(transmitterPi).append(" 3 ");

        prettyPrintBuckets(binary, PULSE_LENGTHS).append(PREFIX);
        convert(binary, printBinaryWithWidth(command.getSensorId(), 26), BINARY_TO_PULSE_MAPPING);
        convert(binary, command.isGroup() ? "1" : "0", BINARY_TO_PULSE_MAPPING);
        convert(binary, commandToBinaryState(command.getCommand()), BINARY_TO_PULSE_MAPPING);
        convert(binary, printBinaryWithWidth(command.getUnitCodeAsInt() - 1, 4), BINARY_TO_PULSE_MAPPING);

        return binary.append(POSTFIX).toString();
    }

    private StringBuilder prettyPrintBuckets(StringBuilder sb, int[] pulseLenghts) {
        for (int i = 0; i < 8; i++) {
            if (i < pulseLenghts.length) {
                sb.append(pulseLenghts[i]);
            } else {
                sb.append(0);
            }
            sb.append(' ');
        }

        // handy for chaining
        return sb;
    }

    private void convert(StringBuilder sb, String input, Map<Character, String> mapping) {
        for (char c : input.toCharArray()) {
            sb.append(mapping.get(c));
        }
    }

    @Override
    public Result process(String pulses) {
        System.out.println(pulses);
        pulses = pulses.replace(PREFIX, "").replace(POSTFIX, "");

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < pulses.length(); i += 4) {
            String pulse = pulses.substring(i, i + 4);
            output.append(PULSES_TO_BINARY_MAPPING.get(pulse));
        }

        int id = Integer.parseInt(output.substring(0, 26), 2);
        boolean all = Character.getNumericValue(output.charAt(26)) == 1;
        char stateChar = output.charAt(27);
        Integer state = null;
        if (stateChar != 'N') {
            state = Character.getNumericValue(stateChar);
        }
        int unit;
        if (all) {
            unit = 0;
        } else {
            unit = Integer.parseInt(output.substring(28, 32), 2) + 1;
        }

        Integer dimlevel = null;
        if (pulseCount > 132) {
            dimlevel = Integer.parseInt(output.substring(32), 2);
        }

        return new Result(id, unit, state, all, dimlevel);
    }

    static String commandToBinaryState(Type type) {
        if (type == OnOffType.ON || type == OpenClosedType.OPEN) {
            return "1";
        } else if (type == OnOffType.OFF || type == OpenClosedType.CLOSED) {
            return "0";
        } else {
            return "N";
        }
    }

    static String printBinaryWithWidth(int number, int width) {
        return String.format("%" + width + "s", Integer.toBinaryString(number)).replace(' ', '0');
    }

    @Override
    public Result process(Pulses pulses) {
        return process(pulses.getPulses());
    }
}

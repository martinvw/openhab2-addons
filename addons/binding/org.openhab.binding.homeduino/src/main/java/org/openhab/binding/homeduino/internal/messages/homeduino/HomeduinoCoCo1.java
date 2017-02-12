/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages.homeduino;

import java.util.HashMap;
import java.util.Map;

public abstract class HomeduinoCoCo1 extends HomeduinoProtocol {
    private static final String POSTFIX = "02";

    private static int[] PULSE_LENGTHS = {358, 1095, 11244};
    private static int PULSE_COUNT = 50;

    private static Map<String, Character> PULSES_TO_BINARY_MAPPING = initializePulseBinaryMapping();
    private static Map<Character, String> BINARY_TO_PULSE_MAPPING = inverse(PULSES_TO_BINARY_MAPPING);

    public HomeduinoCoCo1() {
        super(PULSE_COUNT, PULSE_LENGTHS);
    }

    private static Map<String, Character> initializePulseBinaryMapping() {
        Map<String, Character> map = new HashMap<>();
        map.put("0110", '0');
        map.put("0101", '1');
        return map;
    }

    @Override
    public Result process(String pulses) {
        pulses = pulses.replace(POSTFIX, "");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < pulses.length(); i += 4) {
            String pulse = pulses.substring(i, i + 4);
            output.append(map(PULSES_TO_BINARY_MAPPING, pulse));
        }

        int unit = Integer.parseInt(output.substring(0, 5), 2);
        int id = Integer.parseInt(output.substring(5, 10), 2);
        int state = 1 - Integer.parseInt(output.substring(11), 2);

        return new Result.Builder(this.getClass(), id, unit).withState(state).build();
    }

    @Override
    public String decode(Command command, int transmitterPin) {
        StringBuilder binary = getMessageStart(transmitterPin, PULSE_LENGTHS);

        convert(binary, printBinaryWithWidth(command.getUnitCodeAsInt(), 5), BINARY_TO_PULSE_MAPPING);
        convert(binary, printBinaryWithWidth(command.getSensorId(), 5), BINARY_TO_PULSE_MAPPING);
        binary.append(BINARY_TO_PULSE_MAPPING.get('0'));
        convert(binary, inverse(commandToBinaryState(command.getCommand())), BINARY_TO_PULSE_MAPPING);

        return binary.append(POSTFIX).toString();
    }
}

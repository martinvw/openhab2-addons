/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

abstract class HomeduinoCoCo1 extends HomeduinoProtocol {
    private static int[] PULSE_LENGTHS = { 358, 1095, 11244 };
    private static int PULSE_COUNT = 50;

    HomeduinoCoCo1() {
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

        int unit = Integer.parseInt(output.substring(0, 5), 2);
        int id = Integer.parseInt(output.substring(5, 10), 2);
        int state = 1 - Integer.parseInt(output.substring(11), 2);

        return new Result(id, unit, state, false, null);
    }
}

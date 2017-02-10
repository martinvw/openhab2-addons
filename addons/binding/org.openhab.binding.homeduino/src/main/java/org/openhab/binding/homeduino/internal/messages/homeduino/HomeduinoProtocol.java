/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages.homeduino;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Type;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HomeduinoProtocol {
    static final String MESSAGE_PREFIX = "RF send ";

    private int pulseCount;
    private int[] pulseLengths;

    public HomeduinoProtocol(int pulseCount, int[] pulseLengths) {
        this.pulseCount = pulseCount;
        this.pulseLengths = pulseLengths;
    }

    public static Pulses prepareAndFixCompressedPulses(byte[] data) {
        Pulses result = prepareCompressedPulses(data);
        return fixPulses(result);
    }

    public String decode(Command command, int transmitterPin) {
        // TODO Auto-generated method stub
        return null;
    }

    static Map<Character, String> inverse(Map<String, Character> input) {
        Map<Character, String> result = new HashMap<>();
        for (Map.Entry<String, Character> entry : input.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    StringBuilder getMessageStart(int transmitterPi, int[] pulseLengths) {
        StringBuilder append = new StringBuilder(MESSAGE_PREFIX).append(transmitterPi).append(" 3 ");
        return prettyPrintBuckets(append, pulseLengths);
    }

    void convert(StringBuilder sb, String input, Map<Character, String> mapping) {
        for (char c : input.toCharArray()) {
            sb.append(mapping.get(c));
        }
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

    /**
     * See: https://github.com/pimatic/rfcontroljs/blob/master/src/controller.coffee
     *
     * @param data
     * @return
     */
    private static Pulses prepareCompressedPulses(byte[] data) {
        Pattern p = Pattern.compile(".*? (([0-9]+ ){8})(([0-7][0-7])+)$");
        Matcher m = p.matcher(new String(data, StandardCharsets.US_ASCII));

        if (!m.matches()) {
            return null;
        }

        String[] pulseLengthsString = m.group(1).replace(" 0", "").split(" ");
        int numberOfDifferentPulseLenghts = pulseLengthsString.length;
        String pulses = m.group(3);

        int[] pulseLengths = new int[numberOfDifferentPulseLenghts];

        int k = 0;
        for (String str : pulseLengthsString) {
            pulseLengths[k++] = Integer.parseInt(str);
        }

        int[] originalPulseLengths = pulseLengths.clone();

        Arrays.sort(pulseLengths);
        int[] pulseLengthMap = new int[numberOfDifferentPulseLenghts];

        // construct a map which points from the old location to the new location
        for (int i = 0; i < pulseLengths.length; i++) {
            int originalPulseLength = originalPulseLengths[i];
            // find it in the new map and store a reference from old to new location
            for (int j = 0; j < pulseLengths.length; j++) {
                if (originalPulseLength == pulseLengths[j]) {
                    pulseLengthMap[i] = j;
                }
            }
        }

        // map all the pulses from old to new
        StringBuilder mappedPulses = new StringBuilder(pulses.length());
        for (int i = 0; i < pulses.length(); i++) {
            String pulseString = pulses.substring(i, i + 1);
            int pulse = Integer.parseInt(pulseString);
            mappedPulses.append(pulseLengthMap[pulse]);
        }

        return new Pulses(pulseLengths, mappedPulses.toString());
    }

    /**
     * See: https://github.com/pimatic/rfcontroljs/blob/master/src/controller.coffee
     *
     * @param pulses
     * @return
     */
    private static Pulses fixPulses(Pulses pulses) {
        if (pulses.pulseLengths.length <= 3) {
            return pulses;
        }

        for (int i = 1; i < pulses.pulseLengths.length; i++) {
            if (pulses.pulseLengths[i - 1] * 1.8 < pulses.pulseLengths[i]) {
                continue;
            }

            // we are going to merge pulse i-1 & i, the correct average can be calculated
            // by averaging over the occurrences.
            int newPulseLenght = calculateNewPulseLength(pulses, i - 1, i);

            // we will remove i-1 & i from the list and replace it by newPulseLenght
            int[] newPulseLengths = Arrays.copyOfRange(pulses.pulseLengths, 0, pulses.pulseLengths.length - 1);
            newPulseLengths[i - 1] = newPulseLenght;
            for (int j = i + 1; j < pulses.pulseLengths.length; j++) {
                newPulseLengths[j - 1] = pulses.pulseLengths[j];
            }

            // all elements which refer to i or up should be shifted one back
            String newPulses = pulses.pulses;
            for (int j = i; j < pulses.pulseLengths.length; j++) {
                newPulses = newPulses.replaceAll("" + j, "" + (j - 1));
            }
            return new Pulses(newPulseLengths, newPulses);
        }

        return pulses;
    }

    private static int calculateNewPulseLength(Pulses pulses, int pulse1, int pulse2) {
        int pulseLength1 = pulses.pulseLengths[pulse1];
        int pulseLength2 = pulses.pulseLengths[pulse2];

        int totalPulseStringLength = pulses.pulses.length();
        int pulseCount1 = (totalPulseStringLength - pulses.pulses.replaceAll("" + pulse1, "").length()) / 2;
        int pulseCount2 = (totalPulseStringLength - pulses.pulses.replaceAll("" + pulse2, "").length()) / 2;

        return ((pulseLength1 * pulseCount1) + (pulseLength2 * pulseCount2)) / (pulseCount1 + pulseCount2);
    }

    public boolean matches(Pulses pulses) {
        if (this.pulseCount != pulses.pulseCount) {
            return false;
        }

        return matchTimings(pulses.pulseLengths, this.pulseLengths);
    }

    private static boolean matchTimings(int[] sourcePulseLengths, int[] targetPulseLengths) {
        if (sourcePulseLengths.length != targetPulseLengths.length) {
            return false;
        }

        int length = sourcePulseLengths.length;
        for (int i = 0; i < length; i++) {
            int delta = sourcePulseLengths[i] - targetPulseLengths[i];
            double deviation = (double) delta / sourcePulseLengths[i];
            if (deviation > 0.15) {
                return false;
            }
        }
        return true;
    }

    abstract public Result process(String pulses);

    public Result process(Pulses pulses) {
        return process(pulses.pulses);
    }

    protected String inverse(String s) {
        if ("1".equals(s)) return "0";
        return "1";
    }

    public static class Pulses {
        private final int[] pulseLengths;
        private final String pulses;
        private final int pulseCount;

        Pulses(int[] pulseLengths, String pulses) {
            this.pulseLengths = pulseLengths;
            this.pulses = pulses;
            this.pulseCount = pulses.length();
        }

        String getPulses() {
            return pulses;
        }
    }

    public class Result {
        private int id;
        private boolean all;
        private Integer state;
        private int unit;
        private Integer dimlevel;

        Result(int id, int unit, Integer state, boolean all, Integer dimlevel) {
            this.id = id;
            this.unit = unit;
            this.state = state;
            this.all = all;
            this.dimlevel = dimlevel;
        }

        public HomeduinoProtocol getProtocol() {
            return HomeduinoProtocol.this;
        }

        public int getId() {
            return id;
        }

        public boolean getAll() {
            return all;
        }

        public Integer getState() {
            return state;
        }

        public int getUnit() {
            return unit;
        }

        public Integer getDimlevel() {
            return dimlevel;
        }
    }
}

/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.internal.exceptions.RFXComException;
import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeduinoEventMessage extends HomeduinoBaseMessage {
    private final byte[] data;

    private Logger logger = LoggerFactory.getLogger(HomeduinoEventMessage.class);

    private final static List<HomeduinoProtocol> SUPPORTED_PROTOCOLS = initializeProtocols();

    private static List<HomeduinoProtocol> initializeProtocols() {
        List<HomeduinoProtocol> result = new ArrayList<>();
        result.add(new Switch1Message.Protocol());
        result.add(new Switch2Message.Protocol());
        result.add(new Switch4Message.Protocol());
        result.add(new Dimmer1Message.Protocol());
        result.add(new Pir1Message.Protocol());
        result.add(new Shutter3Message.Protocol());
        return result;
    }

    public HomeduinoEventMessage(byte[] data) {
        this.data = data;
    }

    public static String decodeMessage(RFXComMessage message) {
        if (message instanceof RFXComLighting2Message) {
            RFXComLighting2Message lightingMessage = (RFXComLighting2Message) message;

            // first convert it to a binary string
            String binary = printBinaryWithWidth(lightingMessage.sensorId, 26);
            // all
            RFXComLighting2Message.Commands command = lightingMessage.command;
            binary += (command == RFXComLighting2Message.Commands.GROUP_OFF || command == RFXComLighting2Message.Commands.GROUP_ON) ? "1" : "0";
            binary += commandToBinaryState(command);
            binary += printBinaryWithWidth(lightingMessage.unitCode, 4);
            if (command == RFXComLighting2Message.Commands.SET_LEVEL) {
                binary += printBinaryWithWidth(lightingMessage.signalLevel, 4);
            }

            // make the output pin variable
            StringBuilder output = new StringBuilder("RF send 4 3 284 2800 1352 10760 0 0 0 0 01");
            for (int i = 0; i < binary.length(); i++) {
                char c = binary.charAt(i);
                String pulse;
                if (c == '1') {
                    pulse = "0200";
                } else if (c == 'N') {
                    pulse = "0000";
                } else {
                    pulse = "0002";
                }

                output.append(pulse);
            }
            output.append("03");
            return output.toString();
        }
        return null;
    }

    private static String commandToBinaryState(RFXComLighting2Message.Commands command) {
        if (command == RFXComLighting2Message.Commands.ON || command == RFXComLighting2Message.Commands.GROUP_ON) {
            return "1";
        } else if (command == RFXComLighting2Message.Commands.OFF || command == RFXComLighting2Message.Commands.GROUP_OFF) {
            return "0";
        } else {
            return "N";
        }
    }

    private static String printBinaryWithWidth(int number, int width) {
        return String.format("%" + width + "s", Integer.toBinaryString(number)).replace(' ', '0');
    }

    public String getDeviceId(HomeduinoProtocol.Result result) throws RFXComException {
        return result.getId() + "." + result.getUnit();
    }

    public List<RFXComMessage> getInterpretations() throws RFXComNotImpException, RFXComException {
        List<RFXComMessage> list = new ArrayList<>();

        // the result is a compressed set of timings (from rfcontrol https://github.com/pimatic/RFControl)
        // the first 8 numbers are buckets which refer to pulse lengths,
        // all the other values refer back to these buckets.

        // the strategy we use here is based on the strategy described for rfcontroljs
        String value = new String(data, StandardCharsets.US_ASCII);
        Pattern p = Pattern.compile(".*? (([0-9]+ ){8})(([0-7][0-7])+)$");
        Matcher m = p.matcher(value);

        if (m.matches()) {
            HomeduinoProtocol.Pulses pulses = HomeduinoProtocol.prepareAndFixCompressedPulses(data);

            for (HomeduinoProtocol protocol : SUPPORTED_PROTOCOLS) {
                if (protocol.matches(pulses)) {
                    list.add(RFXComHomeduinoMessageFactory.createMessage(protocol.process(pulses)));
                }
            }
        } else {
            logger.warn("Panic: could not parse message");
        }

        return list;
    }

    @Override
    public PacketType getPacketType() throws RFXComException {
        return null;
    }
}

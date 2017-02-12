/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.openhab.binding.homeduino.internal.exceptions.InvalidInputForProtocol;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;
import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;
import org.openhab.binding.homeduino.internal.messages.homeduino.Dimmer1Message;
import org.openhab.binding.homeduino.internal.messages.homeduino.HomeduinoProtocol;
import org.openhab.binding.homeduino.internal.messages.homeduino.Pir1Message;
import org.openhab.binding.homeduino.internal.messages.homeduino.Shutter3Message;
import org.openhab.binding.homeduino.internal.messages.homeduino.Switch1Message;
import org.openhab.binding.homeduino.internal.messages.homeduino.Switch2Message;
import org.openhab.binding.homeduino.internal.messages.homeduino.Switch4Message;
import org.openhab.binding.homeduino.internal.messages.homeduino.Weather1Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeduinoEventMessage implements HomeduinoMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeduinoEventMessage.class);

    private final byte[] data;

    private final static List<HomeduinoProtocol> SUPPORTED_PROTOCOLS = initializeProtocols();

    private static List<HomeduinoProtocol> initializeProtocols() {
        List<HomeduinoProtocol> result = new ArrayList<>();
        result.add(new Switch1Message.Protocol());
        result.add(new Switch2Message.Protocol());
        result.add(new Switch4Message.Protocol());
        result.add(new Dimmer1Message.Protocol());
        result.add(new Pir1Message.Protocol());
        result.add(new Shutter3Message.Protocol());
        result.add(new Weather1Message.Protocol());
        return result;
    }

    public HomeduinoEventMessage(byte[] data) {
        this.data = data;
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
                    try {
                        list.add(RFXComHomeduinoMessageFactory.createMessage(protocol.process(pulses)));
                    } catch (InvalidInputForProtocol e){
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        } else {
            LOGGER.warn("Panic: could not parse message");
        }

        return list;
    }

    @Override
    public PacketType getPacketType() throws RFXComException {
        return PacketType.HOMEDUINO_RF_EVENT;
    }
}

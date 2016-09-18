package org.openhab.binding.rfxcom.internal.messages.homeduino;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.PacketType;
import org.openhab.binding.rfxcom.internal.messages.RFXComLighting2Message;
import org.openhab.binding.rfxcom.internal.messages.RFXComLighting2Message.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoDimmer1;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoPir1;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoProtocol;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoProtocol.Result;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoShutter3;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoSwitch1;
import org.openhab.binding.rfxcom.internal.messages.homeduino.protocols.HomeduinoSwitch2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeduinoEventMessage extends HomeduinoBaseMessage {
    private final byte[] data;

    private Logger logger = LoggerFactory.getLogger(HomeduinoEventMessage.class);

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays
            .asList(RFXComValueSelector.COMMAND, RFXComValueSelector.CONTACT);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.COMMAND);

    private final static List<HomeduinoProtocol> SUPPORTED_PROTOCOLS = initializeProtocols();

    private static List<HomeduinoProtocol> initializeProtocols() {
        List<HomeduinoProtocol> result = new ArrayList<>();
        result.add(new HomeduinoSwitch1());
        result.add(new HomeduinoSwitch2());
        result.add(new HomeduinoDimmer1());
        result.add(new HomeduinoPir1());
        result.add(new HomeduinoShutter3());
        return result;
    }

    public HomeduinoEventMessage(byte[] data) {
        super(PacketType.HOMEDUINO_RF_EVENT);
        this.data = data;
    }

    public byte[] decodeMessage() {
        // convert
        return null;
    }

    public static String decodeMessage(RFXComMessage message) {
        if (message instanceof RFXComLighting2Message) {
            RFXComLighting2Message lightingMessage = (RFXComLighting2Message) message;

            // first convert it to a binary string
            String binary = printBinaryWithWidth(lightingMessage.sensorId, 26);
            // all
            Commands command = lightingMessage.command;
            binary += (command == Commands.GROUP_OFF || command == Commands.GROUP_ON) ? "1" : "0";
            binary += commandToBinaryState(command);
            binary += printBinaryWithWidth(lightingMessage.unitCode, 4);
            if (command == Commands.SET_LEVEL) {
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

    private static String commandToBinaryState(Commands command) {
        if (command == Commands.ON || command == Commands.GROUP_ON) {
            return "1";
        } else if (command == Commands.OFF || command == Commands.GROUP_OFF) {
            return "0";
        } else {
            return "N";
        }
    }

    private static String printBinaryWithWidth(int number, int width) {
        return String.format("%" + width + "s", Integer.toBinaryString(number)).replace(' ', '0');
    }

    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }

    public String getDeviceId(Result result) throws RFXComException {
        return result.getId() + "." + result.getUnit();
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.LIGHTING2;
    }

    public List<RFXComMessage> getInterpretations() {
        List<RFXComMessage> list = new ArrayList<>();

        // the result is a compressed set of timings (from rfcontrol https://github.com/pimatic/RFControl)
        // the first 8 numbers are buckets which refer to pulse lengths,
        // all the other values refer back to these buckets.

        // the strategy we use here is based on the strategy described for rfcontroljs
        String value = new String(data, StandardCharsets.US_ASCII);
        Pattern p = Pattern.compile(".*? (([0-9]+ ){8})(([0-7][0-7])+)$");
        Matcher m = p.matcher(value);

        System.out.println(value);

        if (m.matches()) {
            HomeduinoProtocol.Pulses pulses = HomeduinoProtocol.prepareAndFixCompressedPulses(data);

            for (HomeduinoProtocol protocol : SUPPORTED_PROTOCOLS) {
                if (protocol.matches(pulses)) {
                    list.add(new RFXComHomeduinoMessage(protocol.process(pulses)));
                }
            }
        } else {
            logger.warn("Panic: could not parse message");
        }

        return list;
    }
}

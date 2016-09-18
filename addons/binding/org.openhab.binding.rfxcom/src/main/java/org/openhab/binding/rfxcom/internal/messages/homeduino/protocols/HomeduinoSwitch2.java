package org.openhab.binding.rfxcom.internal.messages.homeduino.protocols;

import static org.openhab.binding.rfxcom.RFXComValueSelector.*;

import java.util.Arrays;
import java.util.List;

import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.messages.PacketType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class HomeduinoSwitch2 extends HomeduinoProtocol  {
	private Logger logger = LoggerFactory.getLogger(HomeduinoSwitch2.class);
	private static int[] PULSE_LENGTHS = { 306, 957, 9808 };
    private static int PULSE_COUNT = 50;

    public HomeduinoSwitch2() {
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

		logger.warn("state: " + state); // could also indicate presence
        logger.warn("Id: " + id);
        logger.warn("Unitcode: " + unit);
		
        return new Result(id, unit, state, false, null);
    }
	
    @Override
    public PacketType getPacketType() {
        return PacketType.HOMEDUINO_SWITCH2;
    }

    @Override
    protected List<RFXComValueSelector> getSupportedInputValueSelectors() {
        return Arrays.asList(COMMAND, CONTACT);
    }
}

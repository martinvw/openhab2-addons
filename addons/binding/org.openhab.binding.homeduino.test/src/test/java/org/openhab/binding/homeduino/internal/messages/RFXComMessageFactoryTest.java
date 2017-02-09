package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.messages.homeduino.RFXComHomeduinoMessage;

import static org.junit.Assert.assertEquals;

public class RFXComMessageFactoryTest {
    @Test
    public void testOutgoingMessageSwitch1() throws Exception {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(PacketType.SWITCH1);
        msg.setDeviceId("9390234.1");
        msg.convertFromState(RFXComValueSelector.COMMAND, OnOffType.ON);

        assertEquals("RF send 1 3 260 1300 2700 10400 0 0 0 0 020001000101000001000100010100010001000100000101000001000101000001000100010100000100010100010000010100000100010100000100010001000103",
                msg.decodeToHomeduinoMessage(1));
    }

    @Test
    public void testOutgoingMessageSwitch2() throws Exception {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(PacketType.SWITCH2);
        msg.setDeviceId("25.16");
        msg.convertFromState(RFXComValueSelector.COMMAND, OnOffType.ON);

        assertEquals("RF send 1 3 306 957 9808 0 0 0 0 0 01010101011001100101010101100110011001100101011002",
                msg.decodeToHomeduinoMessage(1));
    }
}

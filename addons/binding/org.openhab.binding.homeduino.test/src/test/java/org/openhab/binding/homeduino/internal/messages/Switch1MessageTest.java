package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.messages.homeduino.RFXComHomeduinoMessage;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Switch1MessageTest {
    private static final String PULSES = "260 1300 2700 10400 0 0 0 0 ";
    private static final String ACTUAL_DATA = "020001000101000001000100010100010001000100000101000001000101000001000100010100000100010100010000010100000100010100000100010001000103";
    private static final String RF_EVENT_SWITCH1 = "RF receive " + PULSES +ACTUAL_DATA;

    @Test
    public void testOutgoingMessage() throws Exception {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(PacketType.SWITCH1);
        msg.setDeviceId("9390234.1");
        msg.convertFromState(RFXComValueSelector.COMMAND, OnOffType.ON);

        assertEquals("RF send 1 3 " + PULSES + ACTUAL_DATA,
                msg.decodeToHomeduinoMessage(1));
    }

    @Test
    public void testIncomingMessage() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_SWITCH1.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals(PacketType.SWITCH1, event.getPacketType());
        Assert.assertEquals("9390234.1", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.COMMAND), OnOffType.ON);
    }
}

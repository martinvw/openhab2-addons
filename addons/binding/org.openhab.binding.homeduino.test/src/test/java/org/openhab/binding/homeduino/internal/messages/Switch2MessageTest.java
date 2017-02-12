package org.openhab.binding.homeduino.internal.messages;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.messages.homeduino.RFXComHomeduinoMessage;

import java.nio.charset.StandardCharsets;

public class Switch2MessageTest {
    private static final String PULSES = "306 957 9808 0 0 0 0 0 ";
    private static final String ACTUAL_DATA = "01010101011001100101010101100110011001100101011002";
    private static final String RF_EVENT_SWITCH2 = "RF receive " + PULSES + ACTUAL_DATA;

    @Test
    public void testOutgoingMessage() throws Exception {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(PacketType.SWITCH2);
        msg.setDeviceId("25.16");
        msg.convertFromState(RFXComValueSelector.COMMAND, OnOffType.ON);

        assertEquals("RF send 1 3 " + PULSES + ACTUAL_DATA,
                msg.decodeToHomeduinoMessage(1));
    }

    @Test
    public void testIncomingMessage() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_SWITCH2.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals(PacketType.SWITCH2, event.getPacketType());
        Assert.assertEquals("25.16", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.COMMAND), OnOffType.ON);
    }

    @Test
    public void testIncomingStrangeMessage() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage("RF receive 312 944 9740 0 0 0 0 0 01100101011001010101011001100110011001101010101002".getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;

        // no matches, but also no exceptions...
        assertEquals(0, rfEvent.getInterpretations().size());
    }
}

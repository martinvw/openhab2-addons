package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.messages.homeduino.RFXComHomeduinoMessage;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Switch4MessageTest {
    private static final String PULSES = "358 1095 11244 0 0 0 0 0 ";
    private static final String ACTUAL_DATA = "01010110010101100110011001100110010101100110011002";
    private static final String RF_EVENT_SWITCH4 = "RF receive " + PULSES + ACTUAL_DATA;

    @Test
    public void testOutgoingMessage() throws Exception {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(PacketType.SWITCH4);
        msg.setDeviceId("2.20");
        msg.convertFromState(RFXComValueSelector.COMMAND, OnOffType.ON);

        assertEquals("RF send 1 3 " + PULSES + ACTUAL_DATA,
                msg.decodeToHomeduinoMessage(1));
    }

    @Test
    public void testIncomingMessage() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_SWITCH4.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(1);

        Assert.assertEquals(PacketType.SWITCH4, event.getPacketType());
        Assert.assertEquals("2.20", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.COMMAND), OnOffType.ON);
    }
}

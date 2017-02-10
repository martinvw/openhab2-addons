package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.messages.homeduino.RFXComHomeduinoMessage;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Shutter3MessageTest {
    private static final String PULSES = "366 736 1600 5204 10896 0 0 0 ";
    private static final String ACTUAL_DATA = "3210010110010101010110011010011010010110100101011001011001100101101010010110100104";
    private static final String RF_EVENT_SHUTTER3 = "RF receive " + PULSES + ACTUAL_DATA;

    @Test
    public void testOutgoingMessageSwitch2() throws Exception {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(PacketType.SHUTTER3);
        msg.setDeviceId("302736933.1");
        msg.convertFromState(RFXComValueSelector.SHUTTER, UpDownType.UP);

        assertEquals("RF send 1 3 " + PULSES + ACTUAL_DATA,
                msg.decodeToHomeduinoMessage(1));
    }

    @Test
    public void testHomeduinoMessageSwitch2() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_SHUTTER3.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals(PacketType.SHUTTER3, event.getPacketType());
        Assert.assertEquals("302736933.1", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.SHUTTER), UpDownType.UP);
    }
}

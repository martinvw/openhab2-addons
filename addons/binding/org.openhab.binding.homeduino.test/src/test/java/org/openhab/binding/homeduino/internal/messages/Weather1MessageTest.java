package org.openhab.binding.homeduino.internal.messages;

import static org.openhab.binding.homeduino.RFXComValueSelector.HUMIDITY;
import static org.openhab.binding.homeduino.RFXComValueSelector.LOW_BATTERY;
import static org.openhab.binding.homeduino.RFXComValueSelector.TEMPERATURE;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;

import java.nio.charset.StandardCharsets;

public class Weather1MessageTest {
    private static final String PULSES = "504 1936 3888 9188 0 0 0 0 ";
    private static final String ACTUAL_DATA = "01020102010101020201010102010201010101010202010201010102010102020102010203";
    private static final String RF_EVENT = "RF receive " + PULSES + ACTUAL_DATA;

    @Test
    public void testIncomingMessage() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals(PacketType.WEATHER1, event.getPacketType());
        Assert.assertEquals("24.3", event.getDeviceId());
        Assert.assertEquals(20.9, convertState(event, TEMPERATURE), 0.001);
        Assert.assertEquals(53, convertState(event, HUMIDITY), 0.1);
        Assert.assertEquals(OnOffType.OFF, event.convertToState(LOW_BATTERY));
    }

    private double convertState(RFXComMessage event, RFXComValueSelector valueSelector) throws RFXComException {
        return ((DecimalType)(event.convertToState(valueSelector))).toBigDecimal().doubleValue();
    }
}

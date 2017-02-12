package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;
import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;
import org.openhab.binding.homeduino.internal.messages.homeduino.HomeduinoProtocol;
import org.openhab.binding.homeduino.internal.messages.homeduino.RFXComHomeduinoMessage;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.homeduino.internal.messages.PacketType.SHUTTER3;

public class Shutter3MessageTest {
    private static final String PULSES = "366 736 1600 5204 10896 0 0 0 ";

    private static final String ACTUAL_DATA_DOWN = "3201010110101010100110010101010101100110101010010101100110010101100101101010100104";
    private static final String RF_RECEIVE = "RF receive ";
    private static final String RF_EVENTS_SHUTTER_DOWN = RF_RECEIVE + PULSES + ACTUAL_DATA_DOWN;

    private static final String ACTUAL_DATA_UP = "3201010110101010100110010101010101100110101010010101100110010101100101011001010114";
    private static final String RF_EVENTS_SHUTTER_UP = RF_RECEIVE + PULSES + ACTUAL_DATA_UP;

    private static final String ACTUAL_DATA_UP_2 = "3201010110101010100110010101010101100110101010010101100110010101100101011001010114";
    private static final String RF_EVENTS_SHUTTER_UP_2 = RF_RECEIVE + PULSES + ACTUAL_DATA_UP_2;

    private static final String ACTUAL_DATA_STOP = "3210011010011010100110010101010101100110101001101001100110010101100110011001100114";
    private static final String RF_EVENTS_SHUTTER_STOP = RF_RECEIVE + PULSES + ACTUAL_DATA_STOP;

    private static final String TEST = "RF receive 5076 1616 328 660 10504 0 0 0 0123232332323232322332232323232323322332323232232323322332232323322323233223232334";

    @Test
    public void testIncomingMessageDown() throws Exception {
        testIncomingMessage(RF_EVENTS_SHUTTER_DOWN,
                SHUTTER3,
                RFXComValueSelector.SHUTTER,
                UpDownType.DOWN,
                "65542026.1", ACTUAL_DATA_DOWN, UpDownType.DOWN);
    }

    @Test
    public void testIncomingMessageUp() throws Exception {
        testIncomingMessage(RF_EVENTS_SHUTTER_UP,
                SHUTTER3,
                RFXComValueSelector.SHUTTER,
                UpDownType.UP,
                "65542026.1", ACTUAL_DATA_UP, UpDownType.UP);
    }

    @Test
    public void testIncomingMessageUp2() throws Exception {
        testIncomingMessage(RF_EVENTS_SHUTTER_UP_2,
                SHUTTER3,
                RFXComValueSelector.SHUTTER,
                UpDownType.UP,
                "65542026.1", ACTUAL_DATA_UP_2, UpDownType.UP);
    }

    @Test
    public void testIncomingMessageStop() throws Exception {
        testIncomingMessage(RF_EVENTS_SHUTTER_STOP,
                SHUTTER3,
                RFXComValueSelector.SHUTTER,
                UnDefType.UNDEF,
                "384309098.1", ACTUAL_DATA_STOP, StopMoveType.STOP);
    }

    @Ignore("Used to test some example messages from the forum thread")
    @Test
    public void testIncomingMessageTEST() throws Exception {
        testIncomingMessage(TEST,
                SHUTTER3,
                RFXComValueSelector.SHUTTER,
                UpDownType.UP,
                "65542026.1", ACTUAL_DATA_STOP, UpDownType.UP);
    }

    private void testIncomingMessage(String incomingMessage, PacketType expectedEvent, RFXComValueSelector valueSelector,
                                     Type type, String deviceId, String expectedData, Type command)
            throws RFXComNotImpException, RFXComException {
        System.out.println(HomeduinoProtocol.prepareAndFixCompressedPulses(
                incomingMessage.getBytes()));

        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(incomingMessage.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals(expectedEvent, event.getPacketType());
        Assert.assertEquals(deviceId, event.getDeviceId());
        Assert.assertEquals(type, event.convertToState(valueSelector));

        System.out.println(StopMoveType.STOP + " " + convertEventToNewMessage(event, StopMoveType.STOP).decodeToHomeduinoMessage(1));
        System.out.println(UpDownType.UP + " " + convertEventToNewMessage(event, UpDownType.UP).decodeToHomeduinoMessage(1));
        System.out.println(UpDownType.DOWN + " " + convertEventToNewMessage(event, UpDownType.DOWN).decodeToHomeduinoMessage(1));

        assertEquals("RF send 1 3 " + PULSES + expectedData.substring(0, 74),
                convertEventToNewMessage(event, command).decodeToHomeduinoMessage(1).substring(0, 116));
    }

    private RFXComHomeduinoMessage convertEventToNewMessage(RFXComMessage event, Type command) throws RFXComException, RFXComNotImpException {
        RFXComHomeduinoMessage msg = RFXComMessageFactory.createMessage(SHUTTER3);
        msg.setDeviceId(event.getDeviceId());
        State state = event.convertToState(RFXComValueSelector.SHUTTER);
        msg.convertFromState(RFXComValueSelector.SHUTTER, command);
        return msg;
    }
}

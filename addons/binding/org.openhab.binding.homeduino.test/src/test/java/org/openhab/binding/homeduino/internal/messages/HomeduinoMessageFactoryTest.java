/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.homeduino.RFXComValueSelector;

import java.nio.charset.StandardCharsets;

public class HomeduinoMessageFactoryTest {
    private static final String RF_EVENT = "RF receive 284 2800 1352 10760 0 0 0 0 010002020000020002000200020200020000020200000200020200000200020002020002000200020002000200020002000200000202000002000200020002000203";
    private static final String RF_EVENT_DIM = "RF receive 284 2800 1352 10760 0 0 0 0 0100020002020000020002020000020002000202000200020002000200000202000200020000020002000200020002020002000002000200000002000200020002020002000200020003";
    private static final String RF_EVENT_DOORBEL = "RF receive 352 1052 10864 0 0 0 0 0 01100101010101010110011001100101011001100110011002";
    private static final String RF_EVENT_OLD_COCO = "RF receive 295 1180 11210 0 0 0 0 0 01010110010101100110011001100110010101100110011002";
    private static final String RF_EVENTS_SHUTTER_DOWN = "RF receive 5096 1712 364 732 10956 0 0 0 0123232332323232322332232323232323322332323232232323322332232323322323323232322324";
    private static final String RF_EVENTS_SHUTTER_UP = "RF receive 5100 1708 364 732 10896 0 0 0 0123232332323232322332232323232323322332323232232323322332232323322323233232323224";
    private static final String RF_EVENTS_SHUTTER_UP_2 = "RF receive 5268 1716 320 740 464 10448 0 0 0123232332323232342332232323232323342334343434232323342334232323342323233223232335";
    private static final String RF_EVENTS_SHUTTER_STOP = "RF receive 5284 1696 728 368 10456 0 0 0 0123322323322323233223323232323232233223232332232332233223323232233223322332233224";

    // RFControl test cases
    private static final String RF_EVENT_PIR1 = "RF receive 358 1095 11244 0 0 0 0 0 01100101011001100110011001100110011001010110011002";
    private static final String RF_EVENT_PIR1_2 = "RF receive 508 1524 15568 0 0 0 0 0 01100110011001100110011001100110011001100110011002";
    private static final String RF_EVENT_SWITCH1 = "RF receive 268 1282 2632 10168 0 0 0 0 020001000101000001000100010100010001000100000101000001000101000001000100010100000100010100010000010100000100010100000100010001000103";

    private static final String ACKNOWLEDGEMENT = "ACK";
    private static final String ERROR = "ERR";
    private static final String READY = "ready";

    @Test
    public void testHomeduinoMessage() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory.createMessage(RF_EVENT.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals(event.getDeviceId(), "17638398.0");
        Assert.assertEquals(event.convertToState(RFXComValueSelector.COMMAND), OnOffType.OFF);
    }

    @Test
    public void testHomeduinoMessageDim() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_DIM.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("9565958.1", event.getDeviceId());
        Assert.assertEquals(new PercentType(100), event.convertToState(RFXComValueSelector.DIMMING_LEVEL));
    }

    @Test
    public void testHomeduinoMessageDoorbell() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_DOORBEL.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("14.4", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.CONTACT), OpenClosedType.OPEN);
    }

    @Test
    public void testHomeduinoMessageOldCoCo() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_OLD_COCO.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("2.20", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.COMMAND), OnOffType.ON);
    }
	
	@Test
    public void testHomeduinoMessagePir1() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENT_PIR1.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(2);

        Assert.assertEquals(PacketType.PIR1, event.getPacketType());
        Assert.assertEquals("1.8", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.CONTACT), OpenClosedType.OPEN);
    }

	@Test
    public void testHomeduinoMessageSwitch1() throws Exception {
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

    @Test
    public void testHomeduinoMessageShutterUp() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENTS_SHUTTER_UP.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("65542026.1", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.SHUTTER), UpDownType.UP);
    }

    @Test
    public void testHomeduinoMessageShutterUp2() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENTS_SHUTTER_UP_2.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("65542026.1", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.SHUTTER), UpDownType.UP);
    }

    @Test
    public void testHomeduinoMessageShutterDown() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENTS_SHUTTER_DOWN.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("65542026.1", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.SHUTTER), UpDownType.DOWN);
    }

    @Test
    public void testHomeduinoMessageShutterStop() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory
                .createMessage(RF_EVENTS_SHUTTER_STOP.getBytes(StandardCharsets.US_ASCII));
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoEventMessage);

        HomeduinoEventMessage rfEvent = (HomeduinoEventMessage) result;
        RFXComMessage event = rfEvent.getInterpretations().get(0);

        Assert.assertEquals("384309098.1", event.getDeviceId());
        Assert.assertEquals(event.convertToState(RFXComValueSelector.SHUTTER), UnDefType.UNDEF);
    }

    @Test
    public void testHomeduinoReady() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory.createMessage(READY.getBytes());
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoMessage);
    }

    @Test
    public void testHomeduinoAcknowledgement() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory.createMessage(ACKNOWLEDGEMENT.getBytes());
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoMessage);
    }

    @Test
    public void testHomeduinoError() throws Exception {
        HomeduinoMessage result = HomeduinoMessageFactory.createMessage(ERROR.getBytes());
        Assert.assertNotEquals(result, null);
        Assert.assertTrue(result instanceof HomeduinoMessage);
    }
}

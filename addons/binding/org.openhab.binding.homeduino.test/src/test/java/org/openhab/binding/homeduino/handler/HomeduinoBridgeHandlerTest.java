/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.homeduino.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.homeduino.internal.messages.RFXComMessage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.openhab.binding.homeduino.RFXComValueSelector.COMMAND;
import static org.openhab.binding.homeduino.internal.messages.PacketType.SWITCH1;

public class HomeduinoBridgeHandlerTest {
    private static final String RF_EVENT = "RF receive 280 2804 1364 10776 0 0 0 0 010002020000020002000200020200020000020200000200020200000200020002020002000200020002000200020002000200000200020002000200020002000203";

    private HomeduinoBridgeHandler subject = new HomeduinoBridgeHandler(null);
    private HomeduinoConnectorMock connector;

    @Before
    public void setUp() {
        connector = new HomeduinoConnectorMock();
    }

    @Test
    @Ignore("The HomeduinoBridgeHandler cannot be instanstiated with null, this throws java.lang.IllegalArgumentException: The argument 'thing' must not be null.")
    public void testConnect() throws Exception {
        constructSubject(defaultProperties(false));

        subject.connect();
        subject.dispose();

        assertThat(connector.isConnectCalled(), is(true));

        List<String> receivedMessages = connector.getSentMessages();
        assertThat(receivedMessages.size(), is(0));
    }

    @Test
    @Ignore("The HomeduinoBridgeHandler cannot be instanstiated with null, this throws java.lang.IllegalArgumentException: The argument 'thing' must not be null.")
    public void testConnectWithReceiver() throws Exception {
        constructSubject(defaultProperties(true));

        subject.connect();

        connector.mockReceiveMessage("ready");

        subject.dispose();

        assertThat(connector.isConnectCalled(), is(true));

        List<String> receivedMessages = connector.getSentMessages();
        assertThat(receivedMessages.size(), is(1));
        assertThat(receivedMessages.get(0), is("RF receive 1"));
    }

    @Test
    @Ignore("The HomeduinoBridgeHandler cannot be instanstiated with null, this throws java.lang.IllegalArgumentException: The argument 'thing' must not be null.")
    public void testReceiveMessage() throws Exception {
        RFXComHandlerMock listenerMock = new RFXComHandlerMock();

        constructSubject(defaultProperties(true));

        subject.connect();
        subject.registerDeviceStatusListener(listenerMock);

        connector.mockReceiveMessage(RF_EVENT);

        RFXComMessage receivedMessage = listenerMock.getCaptureMessage();
        assertThat(receivedMessage.getDeviceId(), is("17638398.1"));
        assertThat(receivedMessage.getPacketType(), is(SWITCH1));
        assertThat(receivedMessage.convertToState(COMMAND), is((State) OFF));
    }

    private RFXComBridgeConfiguration defaultProperties(boolean withReceiver) {
        RFXComBridgeConfiguration configuration = new RFXComBridgeConfiguration();
        configuration.serialPort = "test";
        configuration.baudrate = BigDecimal.ONE;

        if (withReceiver) {
            configuration.receiverPin = BigDecimal.ONE;
        }
        return configuration;
    }

    private void constructSubject(RFXComBridgeConfiguration configuration) {
        subject = new HomeduinoBridgeHandler(new BridgeStub());
        subject.setConnector(connector);
        subject.setConfiguration(configuration);
        ThingHandlerCallback callback = new ThingHandlerCallbackStub();
        subject.setCallback(callback);
    }

    private static class ThingHandlerCallbackStub implements ThingHandlerCallback {

        @Override
        public void stateUpdated(ChannelUID channelUID, State state) {

        }

        @Override
        public void postCommand(ChannelUID channelUID, Command command) {

        }

        @Override
        public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {

        }

        @Override
        public void thingUpdated(Thing thing) {

        }

        @Override
        public void configurationUpdated(Thing thing) {

        }

        @Override
        public void channelTriggered(Thing thing, ChannelUID channelUID, String string) {

        }

        @Override
        public void migrateThingType(Thing thing, ThingTypeUID thingTypeUID, Configuration configuration) {

        }
    }

    private static class BridgeStub implements Bridge {

        @Override
        public List<Thing> getThings() {
            return null;
        }

        @Override
        public String getLabel() {
            return null;
        }

        @Override
        public void setLabel(String s) {

        }

        @Override
        public List<Channel> getChannels() {
            return null;
        }

        @Override
        public Channel getChannel(String s) {
            return null;
        }

        @Override
        public ThingStatus getStatus() {
            return null;
        }

        @Override
        public ThingStatusInfo getStatusInfo() {
            return null;
        }

        @Override
        public void setStatusInfo(ThingStatusInfo thingStatusInfo) {

        }

        @Override
        public void setHandler(ThingHandler thingHandler) {

        }

        @Override
        public BridgeHandler getHandler() {
            return null;
        }

        @Override
        public ThingUID getBridgeUID() {
            return null;
        }

        @Override
        public void setBridgeUID(ThingUID thingUID) {

        }

        @Override
        public Configuration getConfiguration() {
            return null;
        }

        @Override
        public ThingUID getUID() {
            return null;
        }

        @Override
        public ThingTypeUID getThingTypeUID() {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return null;
        }

        @Override
        public String setProperty(String s, String s1) {
            return null;
        }

        @Override
        public void setProperties(Map<String, String> map) {

        }

        @Override
        public String getLocation() {
            return null;
        }

        @Override
        public void setLocation(String s) {

        }
    }

}

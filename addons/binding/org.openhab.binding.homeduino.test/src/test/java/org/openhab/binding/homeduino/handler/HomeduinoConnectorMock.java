/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.handler;

import org.openhab.binding.homeduino.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.homeduino.internal.connector.RFXComConnectorInterface;
import org.openhab.binding.homeduino.internal.connector.RFXComEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeduinoConnectorMock implements RFXComConnectorInterface {
    private boolean connectCalled;

    private List<String> sentMessages = new ArrayList<String>();
    private List<String> receivedMessages = new ArrayList<String>();

    private static List<RFXComEventListener> _listeners = new ArrayList<RFXComEventListener>();

    @Override
    public void connect(RFXComBridgeConfiguration rfxComBridgeConfiguration) throws Exception {
        this.connectCalled = true;
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendMessage(String message) {
        sentMessages.add(message);
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }

    public void mockReceiveMessage(String message) {
        byte[] msg = message.getBytes(StandardCharsets.US_ASCII);

        Iterator<RFXComEventListener> iterator = _listeners.iterator();

        while (iterator.hasNext()) {
            iterator.next().packetReceived(msg);
        }
    }

    @Override
    public synchronized void addEventListener(RFXComEventListener rfxComEventListener) {
        if (!_listeners.contains(rfxComEventListener)) {
            _listeners.add(rfxComEventListener);
        }
    }

    @Override
    public synchronized void removeEventListener(RFXComEventListener listener) {
        _listeners.remove(listener);
    }

    public boolean isConnectCalled() {
        return connectCalled;
    }
}

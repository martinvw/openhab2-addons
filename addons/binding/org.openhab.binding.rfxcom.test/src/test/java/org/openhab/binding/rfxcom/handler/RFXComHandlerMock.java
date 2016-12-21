/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.handler;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;

class RFXComHandlerMock extends RFXComHandler {
    private RFXComMessage captureMessage;

    RFXComHandlerMock() {
        super(null);
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RFXComMessage message) {
        captureMessage = message;
    }

    public RFXComMessage getCaptureMessage() {
        return captureMessage;
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homeduino.internal.messages.HomeduinoMessage;

/**
 * The {@link DeviceMessageListener} is notified when a message is received.
 *
 * @author Pauli Anttila - Initial contribution of code for RFXCom-binding
 * @author Martin van Wingerden - adapted for usage for the Homeduino-binding
 */
public interface DeviceMessageListener {

    /**
     * This method is called whenever the message is received from the bridge.
     *
     * @param bridge
     *            The Homeduino bridge where message is received.
     * @param message
     *            The message which received.
     */
    void onDeviceMessageReceived(ThingUID bridge, HomeduinoMessage message);
}

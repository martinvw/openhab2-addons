/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.messages;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.homeduino.RFXComValueSelector;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;

import java.util.List;

/**
 * This interface defines interface which every message class should implement.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RFXComMessage {

    /**
     * Procedure for encode raw data.
     *
     * @param data
     *            Raw data.
     */
    void encodeMessage(byte[] data);

    /**
     * Procedure for converting RFXCOM value to Openhab state.
     *
     * @param valueSelector
     *
     * @return Openhab state.
     */
    State convertToState(RFXComValueSelector valueSelector) throws RFXComException;

    /**
     * Procedure for converting Openhab state to RFXCOM object.
     *
     */
    void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException;

    /**
     * Procedure to get device id.
     *
     * @return device Id.
     */
    String getDeviceId() throws RFXComException;

    /**
     * Procedure to set device id.
     *
     */
    void setDeviceId(String deviceId) throws RFXComException;

    /**
     * Procedure to get packet type.
     *
     * @return packet Type
     */
    PacketType getPacketType() throws RFXComException;

    /**
     * Procedure for get supported value selector list for input values.
     *
     * @return List of supported value selectors.
     */
    List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException;

    /**
     * Procedure for get supported value selector list for output values.
     *
     * @return List of supported value selectors.
     */
    List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException;

}

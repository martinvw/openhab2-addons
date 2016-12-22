/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Arrays;

final class HomeduinoStreamReader extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeduinoStreamReader.class);

    private final HomeduinoSerialConnector connector;
    private final InputStream in;
    private boolean interrupted = false;

    HomeduinoStreamReader(HomeduinoSerialConnector connector, InputStream in) {
        this.connector = connector;
        this.in = in;
    }

    @Override
    public void interrupt() {
        super.interrupt();

        interrupted = true;
        try {
            in.close();
        } catch (IOException ignore) {
        } // quietly close
    }

    @Override
    public void run() {
        final int dataBufferMaxLen = Short.MAX_VALUE;

        byte[] dataBuffer = new byte[dataBufferMaxLen];

        int index = 0;
        boolean start_found = false;

        LOGGER.debug("Data listener started");

        try {

            byte[] tmpData = new byte[20];
            int len = -1;

            while ((len = in.read(tmpData)) > 0 && !interrupted) {

                byte[] logData = Arrays.copyOf(tmpData, len);
                LOGGER.debug("Received data (len={}): {}", len, DatatypeConverter.printHexBinary(logData));

                for (int i = 0; i < len; i++) {

                    if (index > dataBufferMaxLen) {
                        // too many bytes received, try to find new start
                        start_found = false;
                    }

                    if (!start_found && ((tmpData[i] >= 97 && tmpData[i] <= 122) // a-z
                            || (tmpData[i] >= 65 && tmpData[i] <= 90) // A-Z
                    )) {

                        start_found = true;
                        index = 0;
                        dataBuffer[index++] = tmpData[i];

                    } else if (start_found) {

                        // line feed & carriage return
                        if (tmpData[i] == 13) {
                            // ignore the line feed
                        } else if (tmpData[i] == 10) {
                            // whole message received, send an event
                            byte[] msg = new byte[index];

                            System.arraycopy(dataBuffer, 0, msg, 0, index);

                            connector.sendMsgToListeners(msg);

                            // find new start
                            start_found = false;
                        } else {
                            dataBuffer[index++] = tmpData[i];
                        }
                    }
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted via InterruptedIOException");
        } catch (IOException e) {
            LOGGER.error("Reading from serial port failed", e);
            connector.sendErrorToListeners(e.getMessage());
        }

        LOGGER.debug("Data listener stopped");
    }
}

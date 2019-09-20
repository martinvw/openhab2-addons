/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.heos.internal.resources;

import static org.openhab.binding.heos.internal.resources.HeosConstants.FAIL;

import java.io.IOException;
import java.util.List;

import org.openhab.binding.heos.internal.api.HeosEventController;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosSendCommand} is responsible to send a command
 * to the HEOS bridge
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosSendCommand {
    private final Logger logger = LoggerFactory.getLogger(HeosSendCommand.class);

    private final HeosResponseDecoder decoder;
    private final HeosEventController eventController;

    private Telnet client;

    public HeosSendCommand(Telnet client, HeosResponseDecoder decoder, HeosEventController eventController) {
        this.client = client;
        this.decoder = decoder;
        this.eventController = eventController;
    }

    public synchronized boolean send(String command) throws ReadException, IOException {
        if (!client.isConnected()) {
            logger.debug("Not connected");
            return false;
        }
        int sendTryCounter = 0;

        if (executeSendCommand(command)) {
            while (sendTryCounter < 1) {
                if (decoder.getSendResult().equals(FAIL)) {
                    executeSendCommand(command);
                    ++sendTryCounter;
                }
                if (decoder.isCommandUnderProgress()) {
                    while (decoder.isCommandUnderProgress()) {
                        try {
                            logger.warn("Sleeping inside Thread {}", Thread.currentThread().getName());
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            logger.debug("Interrupted Exception - Message: {}", e.getMessage());
                        }
                        List<String> readResultList = client.readLine(15000);

                        for (String s : readResultList) {
                            decoder.getHeosJsonParser().parseResult(s);
                            eventController.handleEvent(0); // Important don't remove it. Costs you some live time... ;)
                        }
                    }
                } else {
                    return true;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method shall only be used if no response from network
     * is expected. Else the read buffer is not cleared
     *
     * @param command
     * @return true if send was successful
     */
    public boolean sendWithoutResponse(String command) {
        try {
            return client.send(command);
        } catch (IOException e) {
            logger.debug("IO Excecption - Message: {}", e.getMessage());
            return false;
        }
    }

    /*
     * It seems to be that sometime a command is still
     * in the reading line without being read out. This
     * shall be prevented with an Map which reads until no
     * End of line is detected.
     */
    private boolean executeSendCommand(String command) throws ReadException, IOException {
        boolean sendSuccess = client.send(command);
        if (sendSuccess) {
            List<String> readResultList = client.readLine();

            for (String s : readResultList) {
                decoder.getHeosJsonParser().parseResult(s);
                eventController.handleEvent(0);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean setTelnetClient(Telnet client) {
        this.client = client;

        logger.debug("Set client: {}, connection={}", client, client.isConnected());

        return true;
    }

    public boolean isConnectionAlive() {
        return client.isConnectionAlive();
    }
}

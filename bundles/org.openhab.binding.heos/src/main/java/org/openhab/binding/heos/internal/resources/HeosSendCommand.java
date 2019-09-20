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

import org.openhab.binding.heos.internal.api.HeosEventController;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.openhab.binding.heos.internal.resources.HeosConstants.FAIL;

/**
 * The {@link HeosSendCommand} is responsible to send a command
 * to the HEOS bridge
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosSendCommand {
    private final Logger logger = LoggerFactory.getLogger(HeosSendCommand.class);

    private final Telnet client;
    private final HeosResponseDecoder decoder;
    private final HeosEventController eventController;

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
                        List<String> readResultList = client.readLine(15000); // FIXME can this timeout be decreased?

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

    public boolean isConnectionAlive() {
        return client.isConnectionAlive();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void stopInputListener(String registerChangeEventOFF) {
        logger.debug("Stopping HEOS event line listener");
        client.stopInputListener();

        if (client.isConnected()) {
            logger.debug("HEOS event line is still open closing it....");
            try {
                client.send(registerChangeEventOFF);
            } catch (IOException e) {
                logger.error("Failure during closing connection to HEOS with message: {}", e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (client.isConnected()) {
            return;
        }

        try {
            logger.debug("Disconnecting HEOS command line");
            client.disconnect();
        } catch (IOException e) {
            logger.error("Failure during closing connection to HEOS with message: {}", e.getMessage());
        }

        logger.debug("Connection to HEOS system closed");
    }

    public void startInputListener(String command) {
        try {
            send(command);
            client.startInputListener();
        } catch (IOException | ReadException e) {
            logger.debug("Failed to start input listener");
        }
    }
}

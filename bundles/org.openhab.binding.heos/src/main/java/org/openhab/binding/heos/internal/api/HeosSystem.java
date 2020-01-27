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
package org.openhab.binding.heos.internal.api;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.json.HeosJsonParser;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Group;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.HeosSendCommand;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link HeosSystem} is handling the main commands, which are
 * sent and received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosSystem {
    private final Logger logger = LoggerFactory.getLogger(HeosSystem.class);

    private static final int START_DELAY_SEC = 30;
    private static final long LAST_EVENT_THRESHOLD = TimeUnit.MINUTES.toMillis(30);

    private final ScheduledExecutorService scheduler;

    private final HeosEventController eventController = new HeosEventController(this);

    private final Telnet eventLine = new Telnet();
    private final HeosSendCommand eventSendCommand = new HeosSendCommand(eventLine);

    private final Telnet commandLine = new Telnet();
    private final HeosSendCommand sendCommand = new HeosSendCommand(commandLine);

    private final Map<Integer, Player> playerMapNew = new HashMap<>();
    private final Map<Integer, Player> playerMapOld = new HashMap<>();
    private Map<Integer, Player> removedPlayerMap = new HashMap<>();

    private final Map<String, Group> groupMapNew = new HashMap<>();
    private final Map<String, Group> groupMapOld = new HashMap<>();
    private Map<String, Group> removedGroupMap = new HashMap<>();

    private final HeosJsonParser parser = new HeosJsonParser();
    private final PropertyChangeListener eventProcessor = evt -> {
        try {
            eventController.handleEvent(parser.parseEvent((String) evt.getNewValue()));
        } catch (JsonSyntaxException e) {
            logger.debug("Failed processing event JSON", e);
        }
    };

    private @Nullable ScheduledFuture<?> keepAliveJob;
    private @Nullable ScheduledFuture<?> reconnectJob;

    public HeosSystem(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Establishes the connection to the HEOS-Network if IP and Port is
     * set. The caller has to handle the retry to establish the connection
     * if the method returns {@code false}.
     *
     * @param connectionIP
     * @param connectionPort
     * @param heartbeat
     * @return {@code true} if connection is established else returns {@code false}
     */
    public HeosFacade establishConnection(String connectionIP, int connectionPort, int heartbeat)
            throws IOException, ReadException {
        if (commandLine.connect(connectionIP, connectionPort)) {
            logger.debug("HEOS command line connected at IP {} @ port {}", connectionIP, connectionPort);
            send(HeosCommands.registerChangeEventOff());
        }

        if (eventLine.connect(connectionIP, connectionPort)) {
            logger.debug("HEOS event line connected at IP {} @ port {}", connectionIP, connectionPort);
            eventSendCommand.send(HeosCommands.registerChangeEventOff(), Void.class);
        }

        startHeartBeat(heartbeat);
        startEventListener();

        return new HeosFacade(this, eventController);
    }

    boolean isConnected() {
        return sendCommand.isConnected() && eventSendCommand.isConnected();
    }

    /**
     * Starts the HEOS Heart Beat. This held the connection open even
     * if no data is transmitted. If the connection to the HEOS system
     * is lost, the method reconnects to the HEOS system by calling the
     * {@code establishConnection()} method. If the connection is lost or
     * reconnect the method fires a bridgeEvent via the {@code HeosEvenController.class}
     */
    void startHeartBeat(int heartbeatPulse) {
        keepAliveJob = scheduler.scheduleWithFixedDelay(new KeepAliveRunnable(), START_DELAY_SEC, heartbeatPulse,
                TimeUnit.SECONDS);
    }

    synchronized void startEventListener() throws IOException, ReadException {
        logger.debug("HEOS System Event Listener is starting....");
        eventSendCommand.startInputListener(HeosCommands.registerChangeEventOn());

        logger.debug("HEOS System Event Listener successfully started");
        eventLine.getReadResultListener().addPropertyChangeListener(eventProcessor);
    }

    void closeConnection() {
        logger.debug("Shutting down HEOS Heart Beat");
        ScheduledFuture<?> job = keepAliveJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        cancelReconnectJob();

        eventLine.getReadResultListener().removePropertyChangeListener(eventProcessor);
        eventSendCommand.stopInputListener(HeosCommands.registerChangeEventOff());
        eventSendCommand.disconnect();
        sendCommand.disconnect();
    }

    private void cancelReconnectJob() {
        ScheduledFuture<?> localReconnectJob = HeosSystem.this.reconnectJob;
        if (localReconnectJob != null && !localReconnectJob.isCancelled()) {
            localReconnectJob.cancel(false);
        }
    }

    HeosResponseObject<Void> send(String command) throws IOException, ReadException {
        return send(command, Void.class);
    }

    synchronized <T> HeosResponseObject<T> send(String command, Class<T> clazz) throws IOException, ReadException {
        return sendCommand.send(command, clazz);
    }

    /**
     * This method returns a {@code Map<String pid, HeosPlayer heosPlayer>} with
     * all player found on the network after an connection to the system is
     * established via a bridge.
     *
     * @return a HashMap with all HEOS Player in the network
     * @throws ReadException
     * @throws IOException
     */
    synchronized Map<Integer, Player> getAllPlayer() throws IOException, ReadException {
        playerMapNew.clear();

        HeosResponseObject<Player[]> response = send(HeosCommands.getPlayers(), Player[].class);
        Player[] players = response.payload;
        if (players == null) {
            throw new IOException("Received no valid payload");
        }

        for (Player player : players) {
            playerMapNew.put(player.playerId, player);
            removedPlayerMap = comparePlayerMaps(playerMapNew, playerMapOld);
            playerMapOld.clear();
            playerMapOld.putAll(playerMapNew);
        }
        return playerMapNew;
    }

    /**
     * This method searches for all groups which are on the HEOS network
     * and returns a {@code Map<String gid, HeosGroup heosGroup>}.
     * Before calling this method a connection via a bridge has to be
     * established
     *
     * @return a HashMap with all HEOS groups
     * @throws ReadException
     * @throws IOException
     */
    synchronized Map<String, Group> getGroups() throws IOException, ReadException {
        groupMapNew.clear();
        removedGroupMap.clear();
        HeosResponseObject<Group[]> response = send(HeosCommands.getGroups(), Group[].class);
        Group[] groups = response.payload;
        if (groups == null) {
            throw new IOException("Received no valid payload");
        }

        if (groups.length == 0) {
            removedGroupMap = compareGroupMaps(groupMapNew, groupMapOld);
            groupMapOld.clear();
            return groupMapNew;
        }

        for (Group group : groups) {
            logger.debug("Found: Group {} with {} Players", group.name, group.players.size());
            groupMapNew.put(HeosGroup.calculateGroupMemberHash(group), group);
            removedGroupMap = compareGroupMaps(groupMapNew, groupMapOld);
            groupMapOld.clear(); // clear the old map so that only the currently available groups are added in the next
            // step.
            groupMapOld.putAll(groupMapNew);
        }
        return groupMapNew;
    }

    private Map<String, Group> compareGroupMaps(Map<String, Group> mapNew, Map<String, Group> mapOld) {
        Map<String, Group> removedItems = new HashMap<>();
        for (String key : mapOld.keySet()) {
            if (!mapNew.containsKey(key)) {
                removedItems.put(key, mapOld.get(key));
            }
        }
        return removedItems;
    }

    private Map<Integer, Player> comparePlayerMaps(Map<Integer, Player> mapNew, Map<Integer, Player> mapOld) {
        Map<Integer, Player> removedItems = new HashMap<>();
        for (Integer key : mapOld.keySet()) {
            if (!mapNew.containsKey(key)) {
                removedItems.put(key, mapOld.get(key));
            }
        }
        return removedItems;
    }

    /**
     * Be used to fill the map which contains old Groups at startup
     * with existing HEOS groups.
     */
    void addHeosGroupToOldGroupMap(String hashValue, Group heosGroup) {
        groupMapOld.put(hashValue, heosGroup);
    }

    Map<String, Group> getGroupsRemoved() {
        return removedGroupMap;
    }

    Map<Integer, Player> getPlayerRemoved() {
        return removedPlayerMap;
    }

    /**
     * A class which provides a runnable for the HEOS Heart Beat
     *
     * @author Johannes Einig
     */
    private class KeepAliveRunnable implements Runnable {

        @Override
        public void run() {
            try {
                if (sendCommand.isHostReachable()) {
                    long timeSinceLastEvent = System.currentTimeMillis() - eventController.getLastEventTime();
                    logger.debug("Time since latest event: {} s", timeSinceLastEvent / 1000);

                    logger.debug("Sending HEOS Heart Beat");
                    HeosResponseObject<Void> response = send(HeosCommands.heartbeat());
                    if (timeSinceLastEvent < LAST_EVENT_THRESHOLD && response.result) {
                        return;
                    }
                }
                logger.debug("Connection to HEOS Network lost!");

                // catches a failure during a heart beat send message if connection was
                // getting lost between last Heart Beat but Bridge is online again and not
                // detected by isHostReachable()
            } catch (ReadException | IOException e) {
                logger.debug("Failure during HEOS Heart Beat command with message: {}", e.getMessage());
            }
            restartConnection();
        }

        private void restartConnection() {
            closeConnection();
            eventController.connectionToSystemLost();
            reconnectJob = scheduler.scheduleWithFixedDelay(this::reconnect, 1, 5, TimeUnit.SECONDS);
        }

        private void reconnect() {
            logger.debug("Trying to reconnect to HEOS Network...");
            if (!sendCommand.isHostReachable()) {
                return;
            }

            cancelReconnectJob();
            logger.debug("Reconnecting to Bridge");
            scheduler.schedule(eventController::systemReachable, 15, TimeUnit.SECONDS);
        }
    }
}

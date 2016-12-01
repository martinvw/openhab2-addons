/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.handler;

import static org.openhab.binding.onkyo.OnkyoBindingConstants.*;

import java.io.IOException;
import java.util.EventObject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.onkyo.internal.OnkyoConnection;
import org.openhab.binding.onkyo.internal.OnkyoEventListener;
import org.openhab.binding.onkyo.internal.eiscp.EiscpCommand;
import org.openhab.binding.onkyo.internal.eiscp.EiscpCommandRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnkyoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Frank - Initial contribution
 */
public class OnkyoHandler extends UpnpAudioSinkHandler implements OnkyoEventListener {

    private Logger logger = LoggerFactory.getLogger(OnkyoHandler.class);

    private OnkyoConnection connection;
    private ScheduledFuture<?> statusCheckerFuture;
    private int currentInput = -1;
    private PercentType volume;

    private final int NET_USB_ID = 43;

    public OnkyoHandler(Thing thing, UpnpIOService upnpIOService, AudioHTTPServer audioHTTPServer, String callbackUrl) {
        super(thing, upnpIOService, audioHTTPServer, callbackUrl);

    }

    /**
     * Initialize the state of the receiver.
     */
    @Override
    public void initialize() {
        logger.debug("Initializing handler for Onkyo Receiver");
        if (this.getConfig().get(HOST_PARAMETER) != null) {
            String host = (String) this.getConfig().get(HOST_PARAMETER);
            Integer port = 60128;
            Object portObj = this.getConfig().get(TCP_PORT_PARAMETER);
            if (portObj != null) {
                if (portObj instanceof Number) {
                    port = ((Number) portObj).intValue();
                } else if (portObj instanceof String) {
                    port = Integer.parseInt(portObj.toString());
                }
            }

            connection = new OnkyoConnection(host, port);
            connection.addEventListener(this);

            logger.debug("Connected to Onkyo Receiver @{}", connection.getConnectionName());

            // Start the status checker
            Runnable statusChecker = new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.debug("Checking status of  Onkyo Receiver @{}", connection.getConnectionName());
                        checkStatus();
                    } catch (LinkageError e) {
                        logger.warn("Failed to check the status for  Onkyo Receiver @{}. Cause: {}",
                                connection.getConnectionName(), e.getMessage());
                    } catch (Exception ex) {
                        logger.warn("Exception in update Status Thread Onkyo Receiver @{}. Cause: {}",
                                connection.getConnectionName(), ex.getMessage());

                    }
                }
            };
            statusCheckerFuture = scheduler.scheduleWithFixedDelay(statusChecker, 1, 10, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to receiver. IP address not set.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (statusCheckerFuture != null) {
            statusCheckerFuture.cancel(true);
        }
        if (connection != null) {
            connection.removeEventListener(this);
            connection.closeConnection();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand for channel {}: {}", channelUID.getId(), command.toString());
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.POWER_ON);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.POWER_OFF);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.POWER_QUERY);
                }
                break;
            case CHANNEL_MUTE:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.MUTE);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.UNMUTE);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUME:
                handleVolume(command);
                break;
            case CHANNEL_INPUT:
                if (command instanceof DecimalType) {
                    selectInput(((DecimalType) command).intValue());
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.SOURCE_QUERY);
                }
                break;
            case CHANNEL_CONTROL:
                if (command instanceof PlayPauseType) {
                    if (command.equals(PlayPauseType.PLAY)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_PLAY);
                    } else if (command.equals(PlayPauseType.PAUSE)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_PAUSE);
                    }
                } else if (command instanceof NextPreviousType) {
                    if (command.equals(NextPreviousType.NEXT)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_TRACKUP);
                    } else if (command.equals(NextPreviousType.PREVIOUS)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_TRACKDWN);
                    }
                } else if (command instanceof RewindFastforwardType) {
                    if (command.equals(RewindFastforwardType.REWIND)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_REW);
                    } else if (command.equals(RewindFastforwardType.FASTFORWARD)) {
                        sendCommand(EiscpCommandRef.NETUSB_OP_FF);
                    }
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_PLAY_STATUS_QUERY);
                }
                break;
            case CHANNEL_PLAY_URI:
                handlePlayUri(command);
                break;
            case CHANNEL_LISTENMODE:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommandRef.LISTEN_MODE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.LISTEN_MODE_QUERY);
                }
                break;
            case CHANNEL_ARTIST:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_ARTIST_QUERY);
                }
                break;
            case CHANNEL_ALBUM:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_ALBUM_QUERY);
                }
                break;
            case CHANNEL_TITLE:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_TITLE_QUERY);
                }
                break;
            case CHANNEL_CURRENTPLAYINGTIME:
                if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.NETUSB_SONG_ELAPSEDTIME_QUERY);
                }
                break;
            case CHANNEL_POWERZONE2:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.ZONE2_POWER_ON);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.ZONE2_POWER_SBY);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_POWER_QUERY);
                }
                break;
            case CHANNEL_MUTEZONE2:
                if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.ZONE2_MUTE);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.ZONE2_UNMUTE);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_MUTE_QUERY);
                }
                break;
            case CHANNEL_VOLUMEZONE2:
                if (command instanceof PercentType) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_SET, command);
                } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_UP);
                } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_DOWN);
                } else if (command.equals(OnOffType.OFF)) {
                    sendCommand(EiscpCommandRef.ZONE2_MUTE);
                } else if (command.equals(OnOffType.ON)) {
                    sendCommand(EiscpCommandRef.ZONE2_UNMUTE);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_VOLUME_QUERY);
                }
                break;
            case CHANNEL_INPUTZONE2:
                if (command instanceof DecimalType) {
                    sendCommand(EiscpCommandRef.ZONE2_SOURCE_SET, command);
                } else if (command.equals(RefreshType.REFRESH)) {
                    sendCommand(EiscpCommandRef.ZONE2_SOURCE_QUERY);
                }
                break;

            default:
                logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                break;
        }
    }

    @Override
    public void statusUpdateReceived(EventObject event, String ip, String data) {
        logger.debug("Received status update from Onkyo Receiver @{}: data={}", connection.getConnectionName(), data);

        updateStatus(ThingStatus.ONLINE);

        try {
            EiscpCommand receivedCommand = null;
            for (EiscpCommand candidate : EiscpCommand.values()) {
                String deviceCmd = candidate.getCommand();
                if (data.startsWith(deviceCmd)) {
                    receivedCommand = candidate;
                    break;
                }
            }

            if (receivedCommand != null) {
                switch (receivedCommand.getCommandRef()) {
                    case POWER_OFF:
                        updateState(CHANNEL_POWER, OnOffType.OFF);
                        break;
                    case POWER_ON:
                        updateState(CHANNEL_POWER, OnOffType.ON);
                        break;
                    case MUTE:
                        updateState(CHANNEL_MUTE, OnOffType.ON);
                        break;
                    case UNMUTE:
                        updateState(CHANNEL_MUTE, OnOffType.OFF);
                        break;
                    case VOLUME_SET:
                        volume = new PercentType(Integer.parseInt(data.substring(3, 5), 16));
                        updateState(CHANNEL_VOLUME, volume);
                        break;
                    case SOURCE_SET:
                        int input = Integer.parseInt(data.substring(3, 5), 16);
                        updateState(CHANNEL_INPUT, new DecimalType(input));
                        onInputChanged(input);
                        break;
                    case NETUSB_SONG_ARTIST_QUERY:
                        updateState(CHANNEL_ARTIST, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_SONG_ALBUM_QUERY:
                        updateState(CHANNEL_ALBUM, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_SONG_TITLE_QUERY:
                        updateState(CHANNEL_TITLE, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_SONG_ELAPSEDTIME_QUERY:
                        updateState(CHANNEL_CURRENTPLAYINGTIME, new StringType(data.substring(3, data.length())));
                        break;
                    case NETUSB_PLAY_STATUS_QUERY:
                        updateNetUsbPlayStatus(data.charAt(3));
                        break;
                    case LISTEN_MODE_SET:
                        String listenModeStr = data.substring(3, 5);
                        // update only when listen mode is supported
                        if (listenModeStr != "N/") {
                            int listenMode = Integer.parseInt(listenModeStr, 16);
                            updateState(CHANNEL_LISTENMODE, new DecimalType(listenMode));
                        }
                        break;
                    case ZONE2_POWER_SBY:
                        updateState(CHANNEL_POWERZONE2, OnOffType.OFF);
                        break;
                    case ZONE2_POWER_ON:
                        updateState(CHANNEL_POWERZONE2, OnOffType.ON);
                        break;
                    case ZONE2_MUTE:
                        updateState(CHANNEL_MUTEZONE2, OnOffType.ON);
                        break;
                    case ZONE2_UNMUTE:
                        updateState(CHANNEL_MUTEZONE2, OnOffType.OFF);
                        break;
                    case ZONE2_VOLUME_SET:
                        updateState(CHANNEL_VOLUMEZONE2, new PercentType(Integer.parseInt(data.substring(3, 5), 16)));
                        break;
                    case ZONE2_SOURCE_SET:
                        int inputZone2 = Integer.parseInt(data.substring(3, 5), 16);
                        updateState(CHANNEL_INPUTZONE2, new DecimalType(inputZone2));
                        break;
                    default:
                        logger.debug("Received unhandled status update from Onkyo Receiver @{}: data={}",
                                connection.getConnectionName(), data);

                }
            } else {
                logger.debug("Received unknown status update from Onkyo Receiver @{}: data={}",
                        connection.getConnectionName(), data);
            }
        } catch (Exception ex) {
            logger.error("Exception in statusUpdateReceived for Onkyo Receiver @{}. Cause: {}, data received: {}",
                    connection.getConnectionName(), ex.getMessage(), data);
        }
    }

    private void selectInput(int inputId) {
        sendCommand(EiscpCommandRef.SOURCE_SET, new DecimalType(inputId));
        currentInput = inputId;
    }

    private void onInputChanged(int newInput) {
        currentInput = newInput;
        if (newInput != NET_USB_ID) {
            updateState(CHANNEL_ARTIST, UnDefType.UNDEF);
            updateState(CHANNEL_ALBUM, UnDefType.UNDEF);
            updateState(CHANNEL_TITLE, UnDefType.UNDEF);
            updateState(CHANNEL_CURRENTPLAYINGTIME, UnDefType.UNDEF);
        }

    }

    private void updateNetUsbPlayStatus(char c) {
        switch (c) {
            case 'P':
                updateState(CHANNEL_CONTROL, PlayPauseType.PLAY);
                break;
            case 'p':
            case 'S':
                updateState(CHANNEL_CONTROL, PlayPauseType.PAUSE);
                break;
            case 'F':
                updateState(CHANNEL_CONTROL, RewindFastforwardType.FASTFORWARD);
                break;
            case 'R':
                updateState(CHANNEL_CONTROL, RewindFastforwardType.REWIND);
                break;

        }
    }

    private void sendCommand(EiscpCommandRef commandRef) {
        if (connection != null) {
            EiscpCommand deviceCommand = EiscpCommand.getCommandByCommandRef(commandRef.getCommand());
            connection.send(deviceCommand.getCommand());
        } else {
            logger.debug("Connect send command to onkyo receiver since the onkyo binding is not initialized");
        }
    }

    private void sendCommand(EiscpCommandRef commandRef, Command command) {
        if (connection != null) {
            EiscpCommand deviceCommand = EiscpCommand.getCommandByCommandRef(commandRef.getCommand());

            String cmdTemplate = deviceCommand.getCommand();
            String deviceCmd = null;

            if (command instanceof OnOffType) {
                deviceCmd = String.format(cmdTemplate, command == OnOffType.ON ? 1 : 0);

            } else if (command instanceof StringType) {
                deviceCmd = String.format(cmdTemplate, command);

            } else if (command instanceof DecimalType) {
                deviceCmd = String.format(cmdTemplate, ((DecimalType) command).intValue());

            } else if (command instanceof PercentType) {
                deviceCmd = String.format(cmdTemplate, ((DecimalType) command).intValue());
            }

            connection.send(deviceCmd);
        } else {
            logger.debug("Connect send command to onkyo receiver since the onkyo binding is not initialized");
        }
    }

    /**
     * Check the status of the AVR. Return true if the AVR is online, else return false.
     *
     * @return
     */
    private void checkStatus() {

        sendCommand(EiscpCommandRef.POWER_QUERY);
        sendCommand(EiscpCommandRef.VOLUME_QUERY);
        sendCommand(EiscpCommandRef.SOURCE_QUERY);
        sendCommand(EiscpCommandRef.MUTE_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_POWER_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_VOLUME_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_SOURCE_QUERY);
        sendCommand(EiscpCommandRef.ZONE2_MUTE_QUERY);

        sendCommand(EiscpCommandRef.LISTEN_MODE_QUERY);

        if (connection != null && connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void handleVolume(final Command command) {
        if (command instanceof PercentType) {
            sendCommand(EiscpCommandRef.VOLUME_SET, command);
        } else if (command.equals(IncreaseDecreaseType.INCREASE)) {
            sendCommand(EiscpCommandRef.VOLUME_UP);
        } else if (command.equals(IncreaseDecreaseType.DECREASE)) {
            sendCommand(EiscpCommandRef.VOLUME_DOWN);
        } else if (command.equals(OnOffType.OFF)) {
            sendCommand(EiscpCommandRef.MUTE);
        } else if (command.equals(OnOffType.ON)) {
            sendCommand(EiscpCommandRef.UNMUTE);
        } else if (command.equals(RefreshType.REFRESH)) {
            sendCommand(EiscpCommandRef.VOLUME_QUERY);
        }
    }

    @Override
    public PercentType getVolume() throws IOException {
        return volume;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        handleVolume(volume);
    }

}

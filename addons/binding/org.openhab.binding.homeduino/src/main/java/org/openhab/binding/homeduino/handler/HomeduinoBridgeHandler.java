package org.openhab.binding.homeduino.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.homeduino.internal.DeviceMessageListener;
import org.openhab.binding.homeduino.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.homeduino.internal.connector.HomeduinoSerialConnector;
import org.openhab.binding.homeduino.internal.connector.RFXComConnectorInterface;
import org.openhab.binding.homeduino.internal.connector.RFXComEventListener;
import org.openhab.binding.homeduino.internal.exceptions.RFXComException;
import org.openhab.binding.homeduino.internal.exceptions.RFXComNotImpException;
import org.openhab.binding.homeduino.internal.messages.HomeduinoEventMessage;
import org.openhab.binding.homeduino.internal.messages.HomeduinoMessage;
import org.openhab.binding.homeduino.internal.messages.HomeduinoMessageFactory;
import org.openhab.binding.homeduino.internal.messages.HomeduinoReadyMessage;
import org.openhab.binding.homeduino.internal.messages.HomeduinoResponseMessage;
import org.openhab.binding.homeduino.internal.messages.RFXComHomeduinoMessage;
import org.openhab.binding.homeduino.internal.messages.RFXComMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HomeduinoBridgeHandler extends BaseBridgeHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeduinoBridgeHandler.class);
    private static final int TIMEOUT = 5000;

    private static HomeduinoResponseMessage responseMessage;
    private final RFXComEventListener homeduinoEventListener = new HomeduinoMessageListener();

    private final Object notifierObject = new Object();

    private RFXComEventListener eventListener;
    private List<DeviceMessageListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private RFXComConnectorInterface connector;
    private RFXComBridgeConfiguration configuration;
    private ScheduledFuture<?> connectorTask;

    public HomeduinoBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        LOGGER.debug("Initializing Homeduino bridge handler");
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(RFXComBridgeConfiguration.class);

        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    LOGGER.debug("Checking Homeduino transceiver connection, thing status = {}", thing.getStatus());
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        connect();
                    }
                }
            }, 0, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        LOGGER.debug("Handler disposed.");

        for (DeviceMessageListener deviceStatusListener : deviceStatusListeners) {
            unregisterDeviceStatusListener(deviceStatusListener);
        }

        if (connector != null) {
            connector.removeEventListener(eventListener);
            connector.disconnect();
        }

        if (connectorTask != null && !connectorTask.isCancelled()) {
            connectorTask.cancel(true);
            connectorTask = null;
        }

        super.dispose();
    }

    protected void setMessageListener(RFXComEventListener eventListener) {
        this.eventListener = eventListener;
    }

    protected RFXComEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        LOGGER.debug("Bridge commands not supported.");
    }

    public boolean registerDeviceStatusListener(DeviceMessageListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.contains(deviceStatusListener) ? false
                : deviceStatusListeners.add(deviceStatusListener);
    }

    public boolean unregisterDeviceStatusListener(DeviceMessageListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    private List<DeviceMessageListener> getDeviceStatusListeners() {
        return deviceStatusListeners;
    }

    private static synchronized HomeduinoResponseMessage getResponseMessage() {
        return responseMessage;
    }

    private static synchronized void setResponseMessage(HomeduinoResponseMessage respMessage) {
        responseMessage = respMessage;
    }

    void connect() {
        if (configuration.serialPort == null) {
            return;
        }

        LOGGER.debug("Connecting to Homeduino transceiver");

        try {
            if (connector == null) {
                connector = new HomeduinoSerialConnector();
            }

            connector.disconnect();
            connector.connect(configuration);
            connector.addEventListener(homeduinoEventListener);

            Thread.sleep(200);

            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            e.printStackTrace();

            LOGGER.error("Connection to RFXCOM transceiver failed: {}", e.getMessage());
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("Error occured when trying to load native library for OS '{}' version '{}', processor '{}'",
                    System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), e);
        }
    }

    void sendMessage(RFXComHomeduinoMessage msg) throws RFXComException {
        setResponseMessage(null);

        try {
            connector.sendMessage(msg.decodeToHomeduinoMessage(configuration.transmitterPin.intValue()));
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            throw new RFXComException("Error while sending message", e);
        }
        try {
            HomeduinoResponseMessage resp;
            synchronized (notifierObject) {
                notifierObject.wait(TIMEOUT);
                resp = getResponseMessage();
            }

            if (resp != null) {
                switch (resp.getPacketType()) {
                    case HOMEDUINO_ACK:
                        LOGGER.debug("Command successfully transmitted, 'ACK' received");
                        break;

                    case HOMEDUINO_ERROR:
                        LOGGER.error("Command transmit failed, 'ERR' received");
                        break;
                }
            } else {
                LOGGER.warn("No response received from transceiver");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

        } catch (InterruptedException ie) {
            LOGGER.error("No acknowledge received from Homeduino controller, TIMEOUT {}ms ", TIMEOUT);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public void setConnector(RFXComConnectorInterface connector) {
        this.connector = connector;
    }

    public void setConfiguration(RFXComBridgeConfiguration configuration) {
        this.configuration = configuration;
    }

    private class HomeduinoMessageListener implements RFXComEventListener {

        @Override
        public void packetReceived(byte[] packet) {
            try {
                HomeduinoMessage message = HomeduinoMessageFactory.createMessage(packet);
                LOGGER.debug("Message received: {}", message);

                if (message instanceof HomeduinoReadyMessage) {
                    if (configuration.receiverPin != null) {
                        String configMessage = "RF receive " + configuration.receiverPin;
                        HomeduinoBridgeHandler.this.connector.sendMessage(configMessage);
                    }
                } else if (message instanceof HomeduinoResponseMessage) {
                    HomeduinoResponseMessage resp = (HomeduinoResponseMessage) message;

                    LOGGER.debug("Response received: {}", message.toString());
                    setResponseMessage(resp);
                    synchronized (notifierObject) {
                        notifierObject.notify();
                    }

                } else if (message instanceof HomeduinoEventMessage) {
                    HomeduinoEventMessage event = (HomeduinoEventMessage) message;
                    List<RFXComMessage> messages = event.getInterpretations();

                    for (RFXComMessage interprentedMsg : messages) {
                        for (DeviceMessageListener deviceStatusListener : getDeviceStatusListeners()) {
                            try {
                                deviceStatusListener.onDeviceMessageReceived(getThing().getUID(), interprentedMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                LOGGER.error("An exception occurred while calling the DeviceStatusListener", e);
                            }
                        }
                    }
                } else {
                    // todo handle this situation
                }
            } catch (RFXComNotImpException e) {
                LOGGER.debug("Message not supported, data: {}", DatatypeConverter.printHexBinary(packet));
            } catch (RFXComException e) {
                LOGGER.error("Error occured during packet receiving, data: {}",
                        DatatypeConverter.printHexBinary(packet), e.getMessage());
            } catch (IOException e) {
                LOGGER.error("Error occured during packet processing", e.getMessage());
            }

            updateStatus(ThingStatus.ONLINE);
        }

        @Override
        public void errorOccured(String error) {
            LOGGER.error("Error occured: {}", error);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }
}

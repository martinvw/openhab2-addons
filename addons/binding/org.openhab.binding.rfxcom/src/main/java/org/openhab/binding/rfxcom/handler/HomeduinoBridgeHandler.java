package org.openhab.binding.rfxcom.handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.rfxcom.internal.DeviceMessageListener;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.rfxcom.internal.connector.HomeduinoConnectorInterface;
import org.openhab.binding.rfxcom.internal.connector.HomeduinoSerialConnector;
import org.openhab.binding.rfxcom.internal.connector.RFXComEventListener;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComNotImpException;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.binding.rfxcom.internal.messages.homeduino.HomeduinoEventMessage;
import org.openhab.binding.rfxcom.internal.messages.homeduino.HomeduinoMessage;
import org.openhab.binding.rfxcom.internal.messages.homeduino.HomeduinoMessageFactory;
import org.openhab.binding.rfxcom.internal.messages.homeduino.HomeduinoReadyMessage;
import org.openhab.binding.rfxcom.internal.messages.homeduino.HomeduinoResponseMessage;
import org.openhab.binding.rfxcom.internal.messages.homeduino.RFXComHomeduinoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeduinoBridgeHandler extends BaseRFXComBridgeHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeduinoBridgeHandler.class);
    private static final int TIMEOUT = 5000;

    private HomeduinoConnectorInterface connector;
    private RFXComBridgeConfiguration configuration;

    private final RFXComEventListener homeduinoEventListener = new HomeduinoMessageListener();
    private final Object notifierObject = new Object();

    private static HomeduinoResponseMessage responseMessage;
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

        if (connector != null) {
            connector.removeEventListener(homeduinoEventListener);
            connector.disconnect();
        }

        if (connectorTask != null && !connectorTask.isCancelled()) {
            connectorTask.cancel(true);
            connectorTask = null;
        }

        super.dispose();
    }

    private static synchronized HomeduinoResponseMessage getResponseMessage() {
        return responseMessage;
    }

    private static synchronized void setResponseMessage(HomeduinoResponseMessage respMessage) {
        responseMessage = respMessage;
    }

    @Override
    protected void connect() {
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

    @Override
    public void sendMessage(RFXComMessage inputMessage) throws RFXComException {
        if (!(inputMessage instanceof RFXComHomeduinoMessage)) {
            throw new IllegalArgumentException("Homeduino bridge can only send Homeduino messages");
        }
        RFXComHomeduinoMessage msg = (RFXComHomeduinoMessage) inputMessage;

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

    public void setConnector(HomeduinoConnectorInterface connector) {
        this.connector = connector;
    }

    public void setConfiguration(RFXComBridgeConfiguration configuration) {
        this.configuration = configuration;
    }
}

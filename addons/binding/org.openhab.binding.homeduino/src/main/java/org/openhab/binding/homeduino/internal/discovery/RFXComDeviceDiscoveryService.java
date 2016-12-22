/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.homeduino.RFXComBindingConstants;
import org.openhab.binding.homeduino.handler.HomeduinoBridgeHandler;
import org.openhab.binding.homeduino.internal.DeviceMessageListener;
import org.openhab.binding.homeduino.internal.messages.RFXComBaseMessage;
import org.openhab.binding.homeduino.internal.messages.RFXComMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * The {@link RFXComDeviceDiscoveryService} class is used to discover RFXCOM
 * devices that send messages to RFXCOM bridge.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComDeviceDiscoveryService extends AbstractDiscoveryService implements DeviceMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(RFXComDeviceDiscoveryService.class);

    private HomeduinoBridgeHandler bridgeHandler;

    public RFXComDeviceDiscoveryService(HomeduinoBridgeHandler rfxcomBridgeHandler) {
        super(null, 1, false);
        this.bridgeHandler = rfxcomBridgeHandler;
    }

    public void activate() {
        bridgeHandler.registerDeviceStatusListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterDeviceStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RFXComBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        // this can be ignored here as we discover devices from received messages
    }

    @Override
    public void onDeviceMessageReceived(ThingUID bridge, RFXComMessage message) {
        logger.trace("Received: bridge: {} message: {}", bridge, message);

        try {
            if (message instanceof RFXComMessage) {
                String id = message.getDeviceId();
                ThingTypeUID uid = RFXComBindingConstants.packetTypeThingMap.get(message.getPacketType());
                ThingUID thingUID = new ThingUID(uid, bridge, id.replace(RFXComBaseMessage.ID_DELIMITER, "_"));
                if (thingUID != null) {
                    logger.trace("Adding new RFXCOM {} with id '{}' to smarthome inbox", thingUID, id);
                    String subType = message.convertSubType().toString();
                    String label = message.getPacketType() + "-" + id;
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                            .withProperty(RFXComBindingConstants.DEVICE_ID, id)
                            .withProperty(RFXComBindingConstants.SUB_TYPE, subType).withBridge(bridge).build();
                    thingDiscovered(discoveryResult);
                }
            } else {
                logger.warn("An unsupported device, was talking");
            }
        } catch (Exception e) {
            logger.debug("Error occured during device discovery", e);
        }
    }
}

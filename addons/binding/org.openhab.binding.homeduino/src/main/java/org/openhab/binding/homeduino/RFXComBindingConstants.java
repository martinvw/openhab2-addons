/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homeduino;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.homeduino.internal.messages.PacketType;

import java.util.Map;
import java.util.Set;

/**
 * The {@link RFXComBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - build an ancestor of this code for the RFXCom binding
 * @author Martin van Wingerden - Initial contribution of the homeduino binding
 */
public class RFXComBindingConstants {
    private static final String BINDING_ID = "homeduino";

    public static final String DEVICE_ID = "deviceId";
    public static final String SUB_TYPE = "subType";

    private static final String BRIDGE_TYPE_HOMEDUINO = "Homeduino";

    public final static ThingTypeUID BRIDGE_HOMEDUINO = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_HOMEDUINO);

    /**
     * Presents all supported Bridge types by RFXCOM binding.
     */
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(BRIDGE_HOMEDUINO);

    // List of all Channel ids
    public final static String CHANNEL_SHUTTER = "shutter";
    public final static String CHANNEL_COMMAND = "command";
    public final static String CHANNEL_MOOD = "mood";
    public final static String CHANNEL_SIGNAL_LEVEL = "signalLevel";
    public final static String CHANNEL_DIMMING_LEVEL = "dimmingLevel";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_HUMIDITY_STATUS = "humidityStatus";
    public final static String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public final static String CHANNEL_LOW_BATTERY = "lowBattery";
    public final static String CHANNEL_PRESSURE = "pressure";
    public final static String CHANNEL_FORECAST = "forecast";
    public final static String CHANNEL_RAIN_RATE = "rainRate";
    public final static String CHANNEL_RAIN_TOTAL = "rainTotal";
    public final static String CHANNEL_WIND_DIRECTION = "windDirection";
    public final static String CHANNEL_WIND_SPEED = "windSpeed";
    public final static String CHANNEL_GUST = "gust";
    public final static String CHANNEL_CHILL_FACTOR = "chillFactor";
    public final static String CHANNEL_INSTANT_POWER = "instantPower";
    public final static String CHANNEL_TOTAL_USAGE = "totalUsage";
    public final static String CHANNEL_INSTANT_AMPS = "instantAmp";
    public final static String CHANNEL_TOTAL_AMP_HOUR = "totalAmpHour";
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_MOTION = "motion";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_VOLTAGE = "voltage";
    public final static String CHANNEL_SET_POINT = "setpoint";

    // List of all Thing Type UIDs
    private static final ThingTypeUID THING_TYPE_SWITCH1 = new ThingTypeUID(BINDING_ID, "switch1");
    private static final ThingTypeUID THING_TYPE_SWITCH2 = new ThingTypeUID(BINDING_ID, "switch2");
    private static final ThingTypeUID THING_TYPE_SWITCH4 = new ThingTypeUID(BINDING_ID, "switch4");
    private static final ThingTypeUID THING_TYPE_DIMMER1 = new ThingTypeUID(BINDING_ID, "dommer1");
    private static final ThingTypeUID THING_TYPE_PIR1 = new ThingTypeUID(BINDING_ID, "pir1");
    private static final ThingTypeUID THING_TYPE_SHUTTER3 = new ThingTypeUID(BINDING_ID, "shutter3");

    /**
     * Presents all supported Thing types by RFXCOM binding.
     */
    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(
            THING_TYPE_SWITCH1,
            THING_TYPE_SWITCH2, THING_TYPE_SWITCH4, THING_TYPE_DIMMER1, THING_TYPE_PIR1,
            THING_TYPE_SHUTTER3
    );
    /**
     * Map RFXCOM packet types to RFXCOM Thing types and vice versa.
     */
    public final static Map<PacketType, ThingTypeUID> packetTypeThingMap = ImmutableMap
            .<PacketType, ThingTypeUID>builder()
            .put(PacketType.SWITCH1, RFXComBindingConstants.THING_TYPE_SWITCH1)
            .put(PacketType.SWITCH2, RFXComBindingConstants.THING_TYPE_SWITCH2)
            .put(PacketType.SWITCH4, RFXComBindingConstants.THING_TYPE_SWITCH4)
            .put(PacketType.DIMMER1, RFXComBindingConstants.THING_TYPE_DIMMER1)
            .put(PacketType.PIR1, RFXComBindingConstants.THING_TYPE_PIR1)
            .put(PacketType.SHUTTER3, RFXComBindingConstants.THING_TYPE_SHUTTER3).build();
}

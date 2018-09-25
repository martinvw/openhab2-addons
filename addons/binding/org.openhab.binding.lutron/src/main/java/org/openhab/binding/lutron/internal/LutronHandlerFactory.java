/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal;

import static org.openhab.binding.lutron.LutronBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lutron.handler.CcoHandler;
import org.openhab.binding.lutron.handler.DimmerHandler;
import org.openhab.binding.lutron.handler.IPBridgeHandler;
import org.openhab.binding.lutron.handler.KeypadHandler;
import org.openhab.binding.lutron.handler.MaintainedCcoHandler;
import org.openhab.binding.lutron.handler.OccupancySensorHandler;
import org.openhab.binding.lutron.handler.PicoKeypadHandler;
import org.openhab.binding.lutron.handler.PulsedCcoHandler;
import org.openhab.binding.lutron.handler.SwitchHandler;
import org.openhab.binding.lutron.handler.TabletopKeypadHandler;
import org.openhab.binding.lutron.handler.VcrxHandler;
import org.openhab.binding.lutron.handler.VirtualKeypadHandler;
import org.openhab.binding.lutron.internal.grxprg.GrafikEyeHandler;
import org.openhab.binding.lutron.internal.grxprg.PrgBridgeHandler;
import org.openhab.binding.lutron.internal.grxprg.PrgConstants;
import org.openhab.binding.lutron.internal.radiora.RadioRAConstants;
import org.openhab.binding.lutron.internal.radiora.handler.PhantomButtonHandler;
import org.openhab.binding.lutron.internal.radiora.handler.RS232Handler;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link LutronHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Allan Tong - Initial contribution
 */
public class LutronHandlerFactory extends BaseThingHandlerFactory {

    // Used by LutronDeviceDiscoveryService to discover these types
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPES_UIDS = ImmutableSet.of(THING_TYPE_DIMMER,
            THING_TYPE_SWITCH, THING_TYPE_OCCUPANCYSENSOR, THING_TYPE_KEYPAD, THING_TYPE_TTKEYPAD, THING_TYPE_PICO,
            THING_TYPE_VIRTUALKEYPAD, THING_TYPE_VCRX, THING_TYPE_CCO_PULSED, THING_TYPE_CCO_MAINTAINED);

    // Other types that can be initiated but not discovered
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_IPBRIDGE,
            PrgConstants.THING_TYPE_PRGBRIDGE, PrgConstants.THING_TYPE_GRAFIKEYE, RadioRAConstants.THING_TYPE_RS232,
            RadioRAConstants.THING_TYPE_DIMMER, RadioRAConstants.THING_TYPE_SWITCH, RadioRAConstants.THING_TYPE_PHANTOM,
            THING_TYPE_CCO);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)
                || DISCOVERABLE_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_IPBRIDGE)) {
            return new IPBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            return new DimmerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new SwitchHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CCO)) {
            return new CcoHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CCO_PULSED)) {
            return new PulsedCcoHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_CCO_MAINTAINED)) {
            return new MaintainedCcoHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OCCUPANCYSENSOR)) {
            return new OccupancySensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_KEYPAD)) {
            return new KeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_TTKEYPAD)) {
            return new TabletopKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PICO)) {
            return new PicoKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VIRTUALKEYPAD)) {
            return new VirtualKeypadHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_VCRX)) {
            return new VcrxHandler(thing);
        } else if (thingTypeUID.equals(PrgConstants.THING_TYPE_PRGBRIDGE)) {
            return new PrgBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(PrgConstants.THING_TYPE_GRAFIKEYE)) {
            return new GrafikEyeHandler(thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_RS232)) {
            return new RS232Handler((Bridge) thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_DIMMER)) {
            return new org.openhab.binding.lutron.internal.radiora.handler.DimmerHandler(thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_SWITCH)) {
            return new org.openhab.binding.lutron.internal.radiora.handler.SwitchHandler(thing);
        } else if (thingTypeUID.equals(RadioRAConstants.THING_TYPE_PHANTOM)) {
            return new PhantomButtonHandler(thing);
        }

        return null;
    }
}

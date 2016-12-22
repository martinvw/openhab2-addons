# Homeduino Binding

This binding integrates large number of sensors and actuators from several different manufactures through

RFXCOM transceivers support RF 433 Mhz protocols like: 
* HomeEasy 
* Oregon etc.


## Supported Things

This binding supports the Homeduino for accessing different sensors and actuators.

This binding currently supports following packet types:

* Blinds1

## Discovery


```
Bridge rfxcom:bridge:usb0 [ serialPort="/dev/tty.usbserial-06VVEG1Y" ] {
    Thing lighting2 100001_1 [deviceId="100001.1", subType="AC"]
}
```

## Channels

This binding currently supports following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| batterylevel | Number | Battery level. |
| command | Switch | Command channel. |
| contact | Contact | Contact channel. |
| dimminglevel | Dimmer | Dimming level channel. |
| humidity | Number | Relative humidity level in percentages. |
| humiditystatus | String | Current humidity status. |
| instantamp | Number | Instant current in Amperes. |
| instantpower | Number | Instant power consumption in Watts. |
| status | String | Status channel. |
| setpoint | Number | Requested temperature. |
| mood | Number | Mood channel. |
| motion | Switch | Motion detection sensor state. |
| rainrate | Number | Rain fall rate in millimeters per hour. |
| raintotal | Number | Total rain in millimeters. |
| shutter | Rollershutter | Shutter channel. |
| signallevel | Number | Received signal strength level. |
| temperature | Number | Current temperature in degree Celsius. |
| totalusage | Number | Used energy in Watt hours. |
| totalamphour | Number | Used "energy" in ampere-hours. |
| winddirection | Number | Wind direction in degrees. |
| windspeed | Number | Average wind speed in meters per second. |

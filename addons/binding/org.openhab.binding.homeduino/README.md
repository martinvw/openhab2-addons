# Homeduino Binding

This binding integrates large number of sensors and actuators from several different manufactures through

RFXCOM transceivers support RF 433 Mhz org.openhab.binding.homeduino.internal.protocols like: 
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

## Installation instructions - Binding
* stop openhab2 by entering  `sudo systemctl stop openhab2`.
* copy the .jar file to the addons folder ( `/usr/share/openhab2/addons/` if you installed via apt-get method)
* start openhab2 by entering  `sudo systemctl start openhab2`.
* check openhab.log file for errors such as:
```
[ERROR] [org.openhab.binding.homeduino       ] - FrameworkEvent ERROR - org.openhab.binding.homeduino
org.osgi.framework.BundleException: Could not resolve module: org.openhab.binding.homeduino [225]
  Unresolved requirement: Import-Package: gnu.io
```
if you see these errors then you need to  install gnu.io library which can be installed by entering the karaf console (`ssh openhab@localhost -p 8101` user:openhab password:habopen - in default configuration karaf console is only available locally) and entering following command: `feature:install openhab-transport-serial` (This will install nrjavaserial, which packages rxtx (which implements gnu.io) and automatically unpacks the suitable binaries for your platform). Restart openhab2 by entering  `sudo systemctl restart openhab2`.
* you should now see `Homeduino 433.92MHz Tranciever` Thing in your Inbox in PaperUI and be able to add it by clicking the + button. If its status is OFFLINE check the openhab.log file again and look for errors:
```
[ERROR] [duino.handler.HomeduinoBridgeHandler] - Connection to RFXCOM transceiver failed: null 
[DEBUG] [duino.handler.HomeduinoBridgeHandler] - Checking Homeduino transceiver connection, thing status = OFFLINE
```
If they are present, this means that openhab can't communicate with your homeduino and you need to add user openhab to the dialout group by entering: `sudo adduser openhab dialout` and restart openhab2 by entering  `sudo systemctl restart openhab2`.
* now all that is left is to configure the Homeduino 433.92MHz Tranciever Thing in the PaperUI:
    Serial port should be entered like `/dev/ttyUSB0` (check your /dev/ folder to determine the correct folder). if you use more than one USB device in your setup please see https://github.com/openhab/openhab1-addons/wiki/symlinks on how to handle serial port changes. If you have more than one USB device (e.g. a Zwave dongle and an RFXCOM dongle, the USB name will change every time you reboot.
    
    If you connected the data pin of receiver to Arduino pin D2 please select 0 as Homeduino receiverPin
    If you connected  the data pin of transmitter to Arduino pin D4 please select 4 as Homeduino transmitterPin
    (for help on how to connect the receiver and sender see:  https://usercontent.pimatic.org/1069e29b978a3453c2cde7bbd0f9aae6ceaccaf3/687474703a2f2f7777772e796f7573637265656e2e64652f777a696a79666a6534382e6a7067 and https://forum.pimatic.org/topic/202/4-homeduino-433-mhz-sending-receiving-and-even-more).

## Installation instructions - Things and item linking
Once you are up and running simply press a button on your remote and a new Thing should be discovered and should appear in you Inbox. All that is left to do is to click the + button to add the Thing and link it to an Item.

Items and links can be created via PaperUI interface or manually by adding them in the .items file

* To create items and links via PaperUi simply click on the channel for Thing under Configuration > Things  in PaperUI and select Item to link to channel or select `+ Create new item ...` and click Link, to create a new item and link it to thing. 

* To manually add the item add a new item in the .item file:`Switch TestSwitch2 {channel="homeduino:switch2:32466789:3_8:command"}` 
    where `homeduino:switch2:32466789:3_8:command` is command channel you can find and select in the Things view in paperUI.

    EXAMPLE view for Thing `SWITCH2-3.8` under Configuration > Things  in PaperUI:
```
Thing: SWITCH2-3.8

Homeduino Switch2 Actuator

A Switch2 device.

Status: ONLINE
Channels
Command

homeduino:switch2:32466789:3_8:command

Command channel


Contact

homeduino:switch2:32466789:3_8:contact

Contact channel
```

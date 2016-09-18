package org.openhab.binding.rfxcom.internal.messages;

public enum PacketType {
    INTERFACE_CONTROL(0),
    INTERFACE_MESSAGE(1),
    TRANSMITTER_MESSAGE(2),
    UNDECODED_RF_MESSAGE(3),

    LIGHTING1(16),
    LIGHTING2(17),
    LIGHTING3(18),
    LIGHTING4(19),
    LIGHTING5(20),
    LIGHTING6(21),
    CHIME(22),
    FAN(23),
    CURTAIN1(24),
    BLINDS1(25),
    RFY(26),
    SECURITY1(32),
    CAMERA1(40),
    REMOTE_CONTROL(48),
    THERMOSTAT1(64),
    THERMOSTAT2(65),
    THERMOSTAT3(66),
    BBQ1(78),
    TEMPERATURE_RAIN(79),
    TEMPERATURE(80),
    HUMIDITY(81),
    TEMPERATURE_HUMIDITY(82),
    BAROMETRIC(83),
    TEMPERATURE_HUMIDITY_BAROMETRIC(84),
    RAIN(85),
    WIND(86),
    UV(87),
    DATE_TIME(88),
    CURRENT(89),
    ENERGY(90),
    CURRENT_ENERGY(91),
    POWER(92),
    WEIGHT(93),
    GAS(94),
    WATER(95),
    RFXSENSOR(112),
    RFXMETER(113),
    FS20(114),
    IO_LINES(128),

    HOMEDUINO_READY(192),
    HOMEDUINO_ACK(193),
    HOMEDUINO_ERROR(194),
    HOMEDUINO_RF_EVENT(195),
    HOMEDUINO_SWITCH1(195),
    HOMEDUINO_SWITCH2(195),
    HOMEDUINO_SWITCH4(195),
    HOMEDUINO_DIMMER1(195),

    HOMEDUINO_PIR1(195),
    HOMEDUINO_SHUTTER3(195),

    UNKNOWN(255),;

    private final int packetType;

    PacketType(int packetType) {
        this.packetType = packetType;
    }

    PacketType(byte packetType) {
        this.packetType = packetType;
    }

    public byte toByte() {
        return (byte) packetType;
    }

}
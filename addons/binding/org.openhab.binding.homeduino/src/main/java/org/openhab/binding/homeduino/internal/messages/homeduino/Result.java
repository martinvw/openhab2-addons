package org.openhab.binding.homeduino.internal.messages.homeduino;

public class Result {
    private final int id;
    private final int unit;
    private final Class<? extends HomeduinoProtocol> protocol;
    private final boolean all;

    private final Integer state;
    private final Integer dimLevel;
    private final Double temperature;
    private final Integer humidity;
    private final boolean lowBattery;

    private Result(Builder builder) {
        this.id = builder.id;
        this.unit = builder.unit;
        this.protocol = builder.protocol;
        this.state = builder.state;
        this.all = builder.all;
        this.dimLevel = builder.dimLevel;
        this.temperature = builder.temperature;
        this.humidity = builder.humidity;
        this.lowBattery = builder.lowBattery;
    }

    public Class<? extends HomeduinoProtocol> getProtocol() {
        return protocol;
    }

    public int getId() {
        return id;
    }

    public boolean isAll() {
        return all;
    }

    public Integer getState() {
        return state;
    }

    public int getUnit() {
        return unit;
    }

    public Integer getDimLevel() {
        return dimLevel;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public boolean isLowBattery() {
        return lowBattery;
    }

    public static class Builder {
        private final int id;
        private final int unit;
        private Class<? extends HomeduinoProtocol> protocol;
        private boolean all;
        private Integer state;
        private Integer dimLevel;
        private Double temperature;
        public Integer humidity;
        private boolean lowBattery;

        public Builder(Class<? extends HomeduinoProtocol> protocol, int id, int unit) {
            this.protocol = protocol;
            this.id = id;
            this.unit = unit;
        }

        public Builder withAll(boolean all) {
            this.all = all;
            return this;
        }

        public Builder withState(Integer state) {
            this.state = state;
            return this;
        }

        public Builder withDimLevel(Integer dimlevel) {
            this.dimLevel = dimlevel;
            return this;
        }

        public Builder withTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder withHumidity(int humidity) {
            this.humidity = humidity;
            return this;
        }

        public Builder withLowBattery(boolean lowBattery) {
            this.lowBattery = lowBattery;
            return this;
        }

        public Result build() {
            return new Result(this);
        }
    }
}

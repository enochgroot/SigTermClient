package com.sigterm.module;

public class Setting {
    public final String name;
    public double value;
    public final double min, max, step;
    public final String suffix;

    public Setting(String name, double value, double min, double max, double step, String suffix) {
        this.name = name; this.value = value;
        this.min = min; this.max = max; this.step = step;
        this.suffix = suffix;
    }

    public void increment() { value = Math.min(max, value + step); }
    public void decrement() { value = Math.max(min, value - step); }

    public String display() {
        if (step >= 1.0) return (int) value + suffix;
        return String.format("%.1f", value) + suffix;
    }
}

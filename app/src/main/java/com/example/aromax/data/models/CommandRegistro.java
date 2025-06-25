package com.example.aromax.data.models;

public class CommandRegistro {
    private long timestamp;
    private int minutesTotal;
    private int secondsInterval;

    public CommandRegistro(int minutesTotal, int secondsInterval) {
        this(System.currentTimeMillis(), minutesTotal, secondsInterval);
    }

    public CommandRegistro(long timestamp, int minutesTotal, int secondsInterval) {
        this.timestamp = timestamp;
        this.minutesTotal = minutesTotal;
        this.secondsInterval = secondsInterval;
    }

    // Getters
    public long getTimestamp() {
        return timestamp;
    }

    public int getMinutesTotal() {
        return minutesTotal;
    }

    public int getSecondsInterval() {
        return secondsInterval;
    }
}
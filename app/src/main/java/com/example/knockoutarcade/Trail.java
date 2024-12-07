package com.example.knockoutarcade;

public class Trail {
    private float x, y; // Posizione della traccia
    private long timeCreated; // Tempo di creazione

    public Trail(float x, float y, long timeCreated) {
        this.x = x;
        this.y = y;
        this.timeCreated = timeCreated;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public long getTimeCreated() {
        return timeCreated;
    }
}

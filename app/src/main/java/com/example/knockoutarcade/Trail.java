package com.example.knockoutarcade;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trail {
    private List<Point> trailList; // Lista dei punti del trail
    private Set<Point> coloredCells; // Celle già colorate
    private int cellWidth; // Larghezza della cella
    private int cellHeight; // Altezza della cella
    private List<float[]> trail = new ArrayList<>(); // Traccia del percorso
    private Set<String> completedCells = new HashSet<>(); // Celle completate
    private long lastTrailPointTime = 0; // Tempo dell'ultimo punto del trail
    private static final int TRAIL_INTERVAL = 150; // Intervallo tra i punti del trail (ms)
    private static final int GRID_SIZE = 20; // Dimensione della griglia
    private Set<String> uniqueTrailPoints = new HashSet<>();
    private static final int TRAIL_SIZE = 20; // Dimensione del trail
    private int counterPoints = 0;
    private float x, y;  // Posizione del player
    private int frameWidth, frameHeight;  // Dimensioni di ogni frame

    public Trail(int cellWidth, int cellHeight) {
        this.trailList = new ArrayList<>();
        this.coloredCells = new HashSet<>();
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }







}
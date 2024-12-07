package com.example.knockoutarcade;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Bot {
    private boolean[][] walkableMap;
    int x, y; // Posizione corrente del bot
    int speed = 2; // Velocità del bot (puoi adattarla)
    private int targetX, targetY; // Target (es. posizione del giocatore)
    private Bot.Direction currentDirection = Bot.Direction.NONE;

    public Bot(boolean[][] walkableMap, int startX, int startY) {
        this.walkableMap = walkableMap;
        this.x = startX;
        this.y = startY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setTarget(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void moveBotTowardsTarget() {
        // Allineamento alla griglia (solo se necessario)
        if (alignToGrid()) {
            Log.d("BotMovement", "Aligning to grid at (" + x + ", " + y + ")");
            return; // Finché non è allineato, non calcola una nuova direzione
        }

        // Calcola la direzione se allineato a un incrocio
        if (isAlignedWithIntersection()) {
            calculateAndSetDirection();
            Log.d("BotMovement", "New direction: " + currentDirection);
        }

        // Muoviti nella direzione corrente
        if (!moveInCurrentDirection()) {
            Log.d("BotMovement", "Failed to move in current direction, recalculating...");
            calculateAndSetDirection();
        }
    }

    private void calculateAndSetDirection() {
        int dx = targetX - x;
        int dy = targetY - y;

        // Decidi la direzione basandoti sulla distanza
        if (Math.abs(dx) > Math.abs(dy)) {
            // Muoviti orizzontalmente se possibile
            if (dx > 0 && canMoveTo(x + 10, y)) {
                currentDirection = Direction.RIGHT;
            } else if (dx < 0 && canMoveTo(x - 10, y)) {
                currentDirection = Direction.LEFT;
            }
        } else {
            // Muoviti verticalmente se possibile
            if (dy > 0 && canMoveTo(x, y + 10)) {
                currentDirection = Direction.DOWN;
            } else if (dy < 0 && canMoveTo(x, y - 10)) {
                currentDirection = Direction.UP;
            }
        }

        // Se nessuna direzione è valida, fermati
        if (currentDirection == Direction.NONE) {
            Log.d("BotMovement", "No valid direction found, bot is idle.");
        }
    }

    private boolean moveInCurrentDirection() {
        switch (currentDirection) {
            case RIGHT:
                if (canMoveTo(x + 10, y)) {
                    x += 10;
                    return true;
                }
                break;
            case LEFT:
                if (canMoveTo(x - 10, y)) {
                    x -= 10;
                    return true;
                }
                break;
            case DOWN:
                if (canMoveTo(x, y + 10)) {
                    y += 10;
                    return true;
                }
                break;
            case UP:
                if (canMoveTo(x, y - 10)) {
                    y -= 10;
                    return true;
                }
                break;
            case NONE:
                Log.d("BotMovement", "No direction set, bot is idle.");
                break;
        }

        return false; // Fallimento nel movimento
    }

    private boolean alignToGrid() {
        boolean aligned = false;
        if (x % 10 != 0) {
            x = (x / 10) * 10; // Allinea alla griglia orizzontale
            aligned = true;
        }
        if (y % 10 != 0) {
            y = (y / 10) * 10; // Allinea alla griglia verticale
            aligned = true;
        }
        return aligned;
    }

    private boolean isAlignedWithIntersection() {
        return x % 10 == 0 && y % 10 == 0;
    }

    public boolean canMoveTo(int x, int y) {
        if (x < 0 || y < 0 || x >= walkableMap.length || y >= walkableMap[0].length) {
            return false;
        }
        return walkableMap[x][y];
    }

    enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT
    }
}

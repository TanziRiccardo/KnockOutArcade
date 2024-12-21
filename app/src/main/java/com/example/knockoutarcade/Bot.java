package com.example.knockoutarcade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
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
    private Bitmap spriteSheetBot;  // Sprite sheet con i frame
    private int frameWidth, frameHeight;  // Dimensioni di ogni frame
    private int currentFrame = 0;  // Frame corrente
    private int totalFrames;  // Numero totale di frame
    private long lastFrameChangeTime = 0;  // Tempo dell'ultimo frame
    private int frameLengthInMillis = 100;  // Durata di ogni frame (in millisecondi)
    private Rect frameRect;  // Rettangolo del frame corrente
    private static final int COLUMNS = 2;  // Numero di colonne nello spritesheet
    private static final int ROWS = 2;  // Numero di righe nello spritesheet
    float x, y; // Posizione corrente del bot
    int speed = 2; // Velocità del bot (puoi adattarla)
    private int targetX, targetY; // Target (es. posizione del giocatore)
    private Bot.Direction currentDirection = Bot.Direction.NONE;
    private float botX;
    private float botY;
    private int moveCounter = 0;
    // Aggiungiamo il tipo di bot
    public enum BotType {
        LUIGI,
        TOAD,
        FIRE
    }

    private BotType botType;  // Tipo di bot
    private List<Bot> allBots; // Riferimento alla lista di tutti i bot
    private int screenWidth;
    private int screenHeight;
    private int movementProgress = 0; // Progressione del movimento
    private final int MOVE_STEP = 10; // Passi più piccoli per simulare un movimento lento

    public Bot(Context context,int resourceId, boolean[][] walkableMap, float startX, float startY, BotType botType, int screenWidth, int screenHeight) {
        this.spriteSheetBot = BitmapFactory.decodeResource(context.getResources(), resourceId);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.walkableMap = walkableMap;
        this.totalFrames = 3;  // Calcola le dimensioni dei fotogrammi in base allo sprite sheet
        this.botType = botType;
        Log.d("BotInitialization", "BotType: " + botType);
        if (botType == BotType.LUIGI) {
            // Calcola la larghezza e altezza per Luigi
            this.frameWidth = spriteSheetBot.getWidth() / 2;  // Ad esempio 2 colonne
            this.frameHeight = spriteSheetBot.getHeight() / 2; // Ad esempio 2 righe
            this.x = startX;
            this.y = startY;
            this.frameRect = new Rect(0, 0, frameWidth, frameHeight);  // Definisce la posizione e dimensione del fotogramma
        } else if (botType == BotType.TOAD) {
            // Calcola la larghezza e altezza per Toad (ipotizzando che abbia un layout diverso)
            this.frameWidth = spriteSheetBot.getWidth() / 2;  // Ad esempio 2 colonne
            this.frameHeight = spriteSheetBot.getHeight() / 2; // Ad esempio 2 righe
            this.x = startX;
            this.y = startY;
            this.frameRect = new Rect(0, 0, frameWidth, frameHeight);  // Definisce la posizione e dimensione del fotogramma
        }
        else if(botType == BotType.FIRE){
            // Calcola la larghezza e altezza per Fire (ipotizzando che abbia un layout diverso)
            this.frameWidth = spriteSheetBot.getWidth() / 2;  // Ad esempio 2 colonne
            this.frameHeight = spriteSheetBot.getHeight() / 3; // Ad esempio 3 righe
            this.x = startX;
            this.y = startY;
            this.frameRect = new Rect(0, 0, frameWidth, frameHeight);  // Definisce la posizione e dimensione del fotogramma
        }


    }
    public float getBotX(){ return botX;}
    public float getBotY(){return botY;}
    // Metodo per settare la lista dei bot
    public void setAllBots(List<Bot> bots) {
        this.allBots = bots;
    }
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setTarget(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }
    public int getWidth() {
        return frameWidth;  // Restituisce la larghezza di un frame
    }

    public int getHeight() {
        return frameHeight;  // Restituisce l'altezza di un frame
    }

    public void bot_update() {
        // Debugging per Luigi e Toad
        Log.d("BotUpdate", "BotType: " + botType + " frameWidth: " + frameWidth + " frameHeight: " + frameHeight);

        // Gestione dell'animazione dei frame
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastFrameChangeTime + frameLengthInMillis) {
            currentFrame = (currentFrame + 1) % totalFrames;  // Passa al prossimo frame
            lastFrameChangeTime = currentTime;
        }

        // Calcola la posizione del frame nello spriteSheet in base al layout specifico del bot
        int columns = spriteSheetBot.getWidth() / frameWidth;  // Numero di colonne nel layout
        int rows = spriteSheetBot.getHeight() / frameHeight;   // Numero di righe nel layout

        int column = currentFrame % columns;  // Colonna corrente
        int row = currentFrame / columns;     // Riga corrente

        frameRect.left = column * frameWidth;
        frameRect.top = row * frameHeight;
        frameRect.right = frameRect.left + frameWidth;
        frameRect.bottom = frameRect.top + frameHeight;

        Log.d("BotUpdate", "frameRect: " + frameRect.toString());
    }
    public void draw_bot(Canvas canvas) {
        if(botType == BotType.LUIGI) {
             botX = x - (frameWidth / 10); // Posizione centrale del bot lungo X
             botY = y - (frameHeight /7); // Posizione centrale del bot lungo Y
            Log.d("BotUpdate", "BotType: " + botType + " x: " + botX + " y: " + botY);

            // Estrai il fotogramma corrente dallo sprite sheet
            Bitmap currentSprite = Bitmap.createBitmap(spriteSheetBot, frameRect.left, frameRect.top, frameWidth, frameHeight);

            // Prova a disegnare senza offset
            canvas.drawBitmap(currentSprite, botX, botY, null);
        }
        if(botType == BotType.TOAD){
             botX = x - (frameWidth / 15); // Posizione centrale del bot lungo X
             botY = y - (frameHeight /5); // Posizione centrale del bot lungo Y
            Log.d("BotUpdate", "BotType: " + botType + " x: " + botX + " y: " + botY);

            // Estrai il fotogramma corrente dallo sprite sheet
            Bitmap currentSprite = Bitmap.createBitmap(spriteSheetBot, frameRect.left, frameRect.top, frameWidth, frameHeight);

            // Prova a disegnare senza offset
            canvas.drawBitmap(currentSprite, botX, botY, null);
        }
        if(botType == BotType.FIRE){
             botX = x - (frameWidth / 7); // Posizione centrale del bot lungo X
             botY = y - (frameHeight /3.5f); // Posizione centrale del bot lungo Y
            Log.d("BotUpdate", "BotType: " + botType + " x: " + botX + " y: " + botY);

            Bitmap currentSprite = Bitmap.createBitmap(spriteSheetBot, frameRect.left, frameRect.top, frameWidth, frameHeight);

            // Ridimensiona il frame corrente
            Bitmap resizedSprite = Bitmap.createScaledBitmap(currentSprite, frameWidth / 2, frameHeight / 2, true);
            // Prova a disegnare senza offset
            canvas.drawBitmap(resizedSprite, botX, botY, null);
        }
    }
    public boolean isOtherBotBlocking(float targetX, float targetY) {
        // Scorri la lista di bot per vedere se c'è un altro bot sulla strada
        for (Bot bot : allBots) {
            // Escludi il bot che sta chiamando questa funzione
            if (bot != this) {
                // Controlla se la posizione target è occupata da un altro bot
                if (Math.abs(bot.getX() - targetX) < 20 && Math.abs(bot.getY() - targetY) < 20) {
                    return true; // La strada è occupata da un altro bot
                }
            }
        }
        return false;
    }
    public void moveBotTowardsTarget() {
        // Allineamento alla griglia (solo se necessario)
        if (alignToGrid()) {
            return; // Finché non è allineato, non calcola una nuova direzione
        }

        // Calcola la direzione se allineato a un incrocio
        if (isAlignedWithIntersection()) {
            calculateAndSetDirection();
        }

        // Muoviti nella direzione corrente
        if (!moveInCurrentDirection()) {
            calculateAndSetDirection();
        }

        // Rallenta o fermati se un altro bot sta bloccando la strada
        if (isOtherBotBlocking(x, y)) {
            stopBot(); // Ferma il bot o rallenta
        } else {
            // Prosegui nel movimento verso il target
            if (!moveInCurrentDirection()) {
                calculateAndSetDirection();
            }
        }
        // Gestione del movimento dei bot ai bordi (ricomparire dal lato opposto)
        if (x < 0) {
            x = screenWidth - getWidth()/8; // Compara al lato destro
        } else if (x + getWidth()/8 > screenWidth) {
            x = 0; // Compara al lato sinistro
        }

        if (y < 0) {
            y = screenHeight - getHeight()/2; // Compara al lato inferiore
        } else if (y + getHeight()/2 > screenHeight) {
            y = 0; // Compara al lato superiore
        }
    }
    private void stopBot() {
        // Logica per fermare o rallentare il bot
        currentDirection = Direction.NONE; // Fermati
        movementProgress = 0; // Ferma il progresso
    }
    private boolean moveInCurrentDirection() {
        // Controlla se il movimento progressivo è completato
        if (movementProgress < 55) {
            movementProgress += MOVE_STEP; // Aumenta progressivamente
        } else {
            movementProgress = 0; // Resetta al completamento del movimento

            // Muovi effettivamente il bot quando il movimento progressivo è completo
            switch (currentDirection) {
                case RIGHT:
                    if (canMoveTo(x + 20, y)) {
                        x += 20; // Sposta di 20 unità alla volta
                        return true;
                    }
                    break;
                case LEFT:
                    if (canMoveTo(x - 20, y)) {
                        x -= 20;
                        return true;
                    }
                    break;
                case DOWN:
                    if (canMoveTo(x, y + 20)) {
                        y += 20;
                        return true;
                    }
                    break;
                case UP:
                    if (canMoveTo(x, y - 20)) {
                        y -= 20;
                        return true;
                    }
                    break;
                case NONE:
                    break;
            }
        }
        return false; // Movimento non completato
    }
    private boolean canMoveTo(float x, float y) {
        // Controlla se la posizione è valida, evitando altri bot
        if (x < 0 || y < 0 || x >= walkableMap.length || y >= walkableMap[0].length) {
            return false; // Fuori dai limiti della mappa
        }

        // Controlla se la posizione è percorribile nella mappa walkable
        if (!walkableMap[(int) x][(int) y]) {
            return false; // La posizione non è percorribile
        }

        // Controlla se la posizione è occupata da un altro bot
        for (Bot bot : allBots) { // Lista dei bot
            if (bot != this && Math.abs(bot.getX() - x) < 10 && Math.abs(bot.getY() - y) < 10) {
                return false; // La posizione è occupata da un altro bot
            }
        }

        return true; // Posizione valida
    }
    private void calculateAndSetDirection() {
        float dx = targetX - x;
        float dy = targetY - y;

        // Tenta prima di avvicinarsi al giocatore
        if (Math.abs(dx) > Math.abs(dy)) {
            // Priorità al movimento orizzontale
            if (dx > 0 && canMoveTo(x + 20, y)) {
                currentDirection = Direction.RIGHT;
            } else if (dx < 0 && canMoveTo(x - 20, y)) {
                currentDirection = Direction.LEFT;
            } else if (dy > 0 && canMoveTo(x, y + 20)) {
                currentDirection = Direction.DOWN;
            } else if (dy < 0 && canMoveTo(x, y - 20)) {
                currentDirection = Direction.UP;
            }
        } else {
            // Priorità al movimento verticale
            if (dy > 0 && canMoveTo(x, y + 20)) {
                currentDirection = Direction.DOWN;
            } else if (dy < 0 && canMoveTo(x, y - 20)) {
                currentDirection = Direction.UP;
            } else if (dx > 0 && canMoveTo(x + 20, y)) {
                currentDirection = Direction.RIGHT;
            } else if (dx < 0 && canMoveTo(x - 20, y)) {
                currentDirection = Direction.LEFT;
            }
        }

        // Se tutte le direzioni sono bloccate, ferma il bot
        if (!canMoveTo(x + 20, y) && !canMoveTo(x - 20, y) &&
                !canMoveTo(x, y + 20) && !canMoveTo(x, y - 20)) {
            currentDirection = Direction.NONE;
        }
    }
    private boolean alignToGrid() {
        boolean aligned = false;
        if (x % 20 != 0) {
            x = Math.round((float) x / 20) * 20; // Allinea alla griglia orizzontale
            aligned = true;
        }
        if (y % 20 != 0) {
            y = Math.round((float) y / 20) * 20; // Allinea alla griglia verticale
            aligned = true;
        }
        return aligned;
    }

    private boolean isAlignedWithIntersection() {
        x = (x / 20) * 20;
        y = (y / 20) * 20;
        return x % 20 == 0 && y % 20 == 0;
    }

    enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT
    }
}


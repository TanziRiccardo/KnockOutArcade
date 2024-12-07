package com.example.knockoutarcade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class Player {
    private Bitmap spriteSheet;  // Sprite sheet con i frame
    private int frameWidth, frameHeight;  // Dimensioni di ogni frame
    private int currentFrame = 0;  // Frame corrente
    private int totalFrames;  // Numero totale di frame
    private long lastFrameChangeTime = 0;  // Tempo dell'ultimo frame
    private int frameLengthInMillis = 100;  // Durata di ogni frame (in millisecondi)
    private Rect frameRect;  // Rettangolo del frame corrente
    private float x, y;  // Posizione del player
    private int speedX, speedY;  // Velocità di movimento
    private float previousX;
    private float previousY;
    private int screenWidth, screenHeight;  // Dimensioni dello schermo
private int currentRow, currentColumn;
    // Numero di righe e colonne (modifica questi valori in base alla tua configurazione dello spritesheet)
    private static final int COLUMNS = 2;  // Numero di colonne nello spritesheet
    private static final int ROWS = 3;  // Numero di righe nello spritesheet

    public Player(Context context, int resourceId, int screenWidth, int screenHeight, int totalFrames, int x, int y) {
        this.spriteSheet = BitmapFactory.decodeResource(context.getResources(), resourceId);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.totalFrames = totalFrames;
        this.frameWidth = spriteSheet.getWidth() / COLUMNS;  // Calcola la larghezza di un frame
        this.frameHeight = spriteSheet.getHeight() / ROWS;  // Calcola l'altezza di un frame
        Log.d("FrameHeigth", "quetsa è frame heigth: "+frameHeight);
        Log.d("FrameWidth", "quetsa è frame width: "+frameWidth);
        Log.d("ScreenWidth", "quetsa è screen width: "+screenWidth);
        Log.d("ScreenHeight", "quetsa è screen heigth: "+screenHeight);

        this.frameRect = new Rect(0, 0, frameWidth, frameHeight);
        this.currentRow = 4;
        this.currentColumn = 4;
        this.x = x;
        this.y = y;

        this.speedX = 0;
        this.speedY = 0;
    }

    public void update() {
        // Log per monitorare le velocità
        Log.d("Player Speed", "SpeedX: " + speedX + ", SpeedY: " + speedY);

        // Aggiorna la posizione in base alla velocità
        x += speedX;
        y += speedY;

        // Log di debug per verificare le posizioni
        Log.d("Player Position", "X: " + x + ", Y: " + y);

        // Mantieni il player dentro i confini dello schermo
        if (x < 0) x = 0;  // Limite sinistro
        if (x + frameWidth > screenWidth) x = screenWidth - frameWidth;  // Limite destro

        // Limita la posizione verticale del player (y)
        if (y < 0) y = 0;  // Limite superiore
        if (y + frameHeight > screenHeight) {
            Log.d("Debug", "Limite inferiore raggiunto: Y calcolata = " + y);
            y = screenHeight - frameHeight;  // Limite inferiore
        }

        // Log per vedere se y è corretta
        Log.d("y", "questa è y calcolata: " + y);

        // Gestione dell'animazione dei frame
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastFrameChangeTime + frameLengthInMillis) {
            currentFrame = (currentFrame + 1) % totalFrames;  // Passa al prossimo frame
            lastFrameChangeTime = currentTime;
        }

        // Calcola la posizione del frame nello spriteSheet (assumendo due colonne)
        int column = currentFrame % 2;  // Due colonne di frame
        int row = currentFrame / 2;  // Determina la riga in base al numero di frame
        frameRect.left = column * frameWidth;
        frameRect.top = row * frameHeight;
        frameRect.right = frameRect.left + frameWidth;
        frameRect.bottom = frameRect.top + frameHeight;
    }




    public void draw(Canvas canvas) {
        // Estrai il fotogramma corrente dallo sprite sheet
        Bitmap currentSprite = Bitmap.createBitmap(spriteSheet, frameRect.left, frameRect.top, frameWidth, frameHeight);

        // Disegna il fotogramma corrente sulla tela
        canvas.drawBitmap(currentSprite, x, y, null);
    }

    public void setSpeed(int speedX, int speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }
public int getSpeedX(){ return speedX;}
    public int getSpeedY(){return speedY;}
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setCurrentRow(int row) {
        this.currentRow = row;
    }

    public void setCurrentColumn(int column) {
        this.currentColumn = column;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public int getCurrentColumn() {
        return currentColumn;
    }
    public int getWidth() {
        return frameWidth;  // Restituisce la larghezza di un frame
    }

    public int getHeight() {
        return frameHeight;  // Restituisce l'altezza di un frame
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
    public void setPreviousX(float x) {
        this.previousX = x;
    }

    public void setPreviousY(float y) {
        this.previousY = y;
    }

    public float getPreviousX() {
        return previousX;
    }

    public float getPreviousY() {
        return previousY;
    }
}


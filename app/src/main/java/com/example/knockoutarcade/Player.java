package com.example.knockoutarcade;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Intersection {
    float targetX;
    float targetY;

    public Intersection(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }
}
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
    private int targetX, targetY; // Target (es. posizione dell'incrocio)
    private Direction currentDirection = Direction.NONE;
    private Direction requestedDirection = Direction.NONE; // La direzione richiesta dal giocatore
    private boolean[][] walkableMap;
    private List<Intersection> targets = new ArrayList<>();
    private int currentTargetIndex = 0;
    private List<float[]> trail = new ArrayList<>(); // Traccia del percorso
    private Set<String> completedCells = new HashSet<>(); // Celle completate
    private long lastTrailPointTime = 0; // Tempo dell'ultimo punto del trail
    private static final int TRAIL_INTERVAL = 150; // Intervallo tra i punti del trail (ms)
    private static final int GRID_SIZE = 20; // Dimensione della griglia
    private Set<String> uniqueTrailPoints = new HashSet<>();
    private static final int TRAIL_SIZE = 20; // Dimensione del trail
    private int counterPoints = 0;
    private MainActivity mainActivity;
    private Trail traill;
    public Player(Context context, int resourceId, int screenWidth, int screenHeight, int totalFrames, int x, int y, boolean[][] walkableMap, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        traill = new Trail(x, y, previousX, previousY, walkableMap, mainActivity, this);
        this.spriteSheet = BitmapFactory.decodeResource(context.getResources(), resourceId);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.totalFrames = totalFrames;
        this.frameWidth = spriteSheet.getWidth() / COLUMNS;  // Calcola la larghezza di un frame
        this.frameHeight = spriteSheet.getHeight() / ROWS;  // Calcola l'altezza di un frame
        this.walkableMap = walkableMap;
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

        // Aggiungi più punti di incrocio
        targets.add(new Intersection(270, 529));  // Primo incrocio
        targets.add(new Intersection(545, 525));  // Secondo incrocio
        targets.add(new Intersection(811, 256));  // Terzo incrocio
        targets.add(new Intersection(269, 972));  // Primo incrocio
        targets.add(new Intersection(542, 972));  // Secondo incrocio
        targets.add(new Intersection(807, 972));  // Terzo incrocio
        targets.add(new Intersection(270, 1416));  // Primo incrocio
        targets.add(new Intersection(541, 1416));  // Secondo incrocio
        targets.add(new Intersection(809, 1416));  // Terzo incrocio
        targets.add(new Intersection(270, 1856));  // Primo incrocio
        targets.add(new Intersection(541, 1856));  // Secondo incrocio
        targets.add(new Intersection(811, 1856));  // Terzo incrocio
    }
    public float getPreviousX(){
        return previousX;
    }
    public float getPreviousY(){
        return previousY;
    }
    public boolean[][] getWalkableMap(){
        return walkableMap;
    }
    public int getCounterPoints() {
        return counterPoints;
    }

    public Bitmap getPlayerBitmap() {
        return spriteSheet;
    }
    float app1;
    float app2;
    public void update() {
        // Log per monitorare le velocità
        Log.d("Player Speed", "SpeedX: " + speedX + ", SpeedY: " + speedY);
        // Salva la posizione precedente
        previousX = x;
        previousY = y;
        // Aggiorna la posizione in base alla velocità
        x += speedX;
        y += speedY;

        // Log di debug per verificare le posizioni
        Log.d("Player Position", "X: " + x + ", Y: " + y);


        // Aggiungi la posizione corrente alla traccia se il player si è mosso
        long currentTime = System.currentTimeMillis();
        if ((previousX != x || previousY != y) && currentTime > lastTrailPointTime + TRAIL_INTERVAL) {
            traill.addTrailPoint();
            lastTrailPointTime = currentTime;
        }

        // Log per vedere se y è corretta
        Log.d("y", "questa è y calcolata: " + y);

        // Gestione dell'animazione dei frame
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
        // Controlla se una cella è stata completata
        traill.checkCellCompletion(x, y);
    }



    public void draw(Canvas canvas) {
        traill.draw(canvas);
        // Estrai il fotogramma corrente dallo sprite sheet
        Bitmap currentSprite = Bitmap.createBitmap(spriteSheet, frameRect.left, frameRect.top, frameWidth, frameHeight);

        // Disegna il fotogramma corrente sulla tela
        canvas.drawBitmap(currentSprite, x+(frameWidth/10), y , null);
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

        public boolean isNearIntersection() {
            // Verifica se il giocatore è abbastanza vicino a un incrocio
            return Math.abs(x % 20) < 5 && Math.abs(y % 20) < 5;
        }

int tolerance = 10;
    public void moveToNextIntersection() {
        if (currentTargetIndex < targets.size()) {
            Intersection target = targets.get(currentTargetIndex);

            // Muovi il giocatore verso il target corrente
            if (x < target.targetX) {
                x += 5;
            } else if (x > target.targetX) {
                x += 5;
            }

            if (y < target.targetY) {
                y += 5;
            } else if (y > target.targetY) {
                y += 5;
            }

            if (Math.abs(x - target.targetX) <= tolerance && Math.abs(y - target.targetY) <= tolerance) {
                currentTargetIndex++;  // Passa al prossimo target
            }
        }
    }

        public void turnToRequestedDirection(int desiredDirectionX, int desiredDirectionY) {
            if(desiredDirectionX == 1 && desiredDirectionY == 0){
                currentDirection = Direction.RIGHT;
                // Muoviti nella nuova direzione
                moveInCurrentDirection();
            }
            else if(desiredDirectionX == -1 && desiredDirectionY == 0){
                currentDirection = Direction.LEFT;
                // Muoviti nella nuova direzione
                moveInCurrentDirection();
            }
            else if(desiredDirectionX == 0 && desiredDirectionY == -1){
                currentDirection = Direction.UP;
                // Muoviti nella nuova direzione
                moveInCurrentDirection();
            }
            else if(desiredDirectionX == 0 && desiredDirectionY == 1){
                currentDirection = Direction.DOWN;
                // Muoviti nella nuova direzione
                moveInCurrentDirection();
            }
        }

        private void moveInCurrentDirection() {
            switch (currentDirection) {
                case RIGHT:
                    x += 5;
                    break;
                case LEFT:
                    x -= 5;
                    break;
                case UP:
                    y += 5;
                    break;
                case DOWN:
                    y -= 5;
                    break;
                case NONE:
                    break;
            }
        }

        public enum Direction {
            NONE, UP, DOWN, LEFT, RIGHT
        }
    }


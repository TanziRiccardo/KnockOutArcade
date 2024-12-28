package com.example.knockoutarcade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    int speed = 2; // Velocità del giocatore
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
    public Player(Context context, int resourceId, int screenWidth, int screenHeight, int totalFrames, int x, int y, boolean[][] walkableMap) {
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

    public int getCounterPoints() {
        return counterPoints;
    }

    public Bitmap getPlayerBitmap() {
        return spriteSheet;
    }
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
            addTrailPoint();
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
        checkCellCompletion();
    }


    private void addTrailPoint() {
        // Allinea il trail al centro delle linee della griglia
        float alignedX = ((x + frameWidth /2) / GRID_SIZE) * GRID_SIZE;
        float alignedY = ((y + frameHeight/2) / GRID_SIZE) * GRID_SIZE;

        // Controlla se il punto è valido per la mappa percorribile
        int gridX = Math.round(alignedX / GRID_SIZE);
        int gridY = Math.round(alignedY / GRID_SIZE);
        int tolerance = 5;
        if (alignedX < tolerance || alignedY < tolerance || alignedX >= walkableMap.length - tolerance || alignedY >= walkableMap[0].length - tolerance) {
            return;
        }

        // Verifica se il punto è già presente nel trail
        for (float[] existingPoint : trail) {
            if (Math.abs(existingPoint[0] - alignedX) < GRID_SIZE / 2 && Math.abs(existingPoint[1] - alignedY) < GRID_SIZE / 2) {
                return; // Evita di aggiungere punti già presenti
            }
        }

        // Crea una chiave unica per posizione
        String pointKey = gridX + "," + gridY;

        // Aggiungi al trail solo se il punto è nuovo
        if (!uniqueTrailPoints.contains(pointKey)) {
            trail.add(new float[]{alignedX, alignedY});
            uniqueTrailPoints.add(pointKey);
            Log.d("Trail", "Aggiunto punto: " + pointKey);
        }

        // Aggiorna la posizione precedente
        previousX = alignedX;
        previousY = alignedY;
    }
    private int[] columnWidths = {202, 202, 202, 202, 202};
    private int[] rowHeights = {486, 350, 486, 350, 486};

    private void checkCellCompletion() {
        if (trail.size() < 4){ Log.d("Aggiunta", "trail: " + trail.size()); return;} // Serve almeno un quadrato completo
        // Trova in quale cella si trova il player
        int gridX = getColumnIndex(x);
        int gridY = getRowIndex(y);

        // Salta il controllo se il player è fuori dalla griglia
        if (gridX == -1 || gridY == -1) return;

        String cellKey = gridX + "," + gridY;

        // Controlla se la cella è già completata
        if (completedCells.contains(cellKey)) return;

        // Verifica se i punti del trail circondano la cella corrente
        boolean closed = isCellSurrounded(gridX, gridY);

        if (closed) {
            completedCells.add(cellKey);
            Log.d("Cell Completion", "Cella completata: " + cellKey);
        }
    }

    private int getColumnIndex(float posX) {
        int currentX = 30; // Inizio della griglia
        for (int i = 0; i < columnWidths.length; i++) {
            if (posX >= currentX && posX < currentX + columnWidths[i]) {
                return i;
            }
            currentX += columnWidths[i];
        }
        return -1; // Fuori dai confini
    }

    private int getRowIndex(float posY) {
        int currentY = 30; // Inizio della griglia
        for (int i = 0; i < rowHeights.length; i++) {
            if (posY >= currentY && posY < currentY + rowHeights[i]) {
                return i;
            }
            currentY += rowHeights[i];
        }
        return -1; // Fuori dai confini
    }

    private boolean isCellSurrounded(int gridX, int gridY) {
        int cellLeft = getCellStartX(gridX);
        int cellRight = getCellStartX(gridX + 1);
        int cellTop = getCellStartY(gridY);
        int cellBottom = getCellStartY(gridY + 1);

        boolean left = false, right = false, top = false, bottom = false;
        float tolerance = 50; // Minimum tolerance to avoid floating-point errors

        // Extend the boundary check area by tolerance to prevent misalignment
        float extendedLeft = cellLeft - tolerance;
        float extendedRight = cellRight + tolerance;
        float extendedTop = cellTop - tolerance;
        float extendedBottom = cellBottom + tolerance;

        // Check if any point in the trail is near the extended boundaries
        for (float[] point : trail) {
            float px = point[0];
            float py = point[1];

            // Check if the point touches the left edge of the cell
            if (px >= extendedLeft && px <= extendedLeft + tolerance && py >= cellTop && py <= cellBottom) {
                Log.d("questo111", "Point: (" + px + ", " + py + "), Checking left edge: extendedLeft = " + extendedLeft + ", tolerance = " + tolerance);
                left = true;
            }

            // Check if the point touches the right edge of the cell
            Log.d("questo222", "Point: (" + px + ", " + py + "), Checking right edge: extendedBottom = " + extendedRight + ", tolerance = " + tolerance+ ", cellTOp" + cellTop +  ", cellBottom" + cellBottom);
            if (px >= extendedRight - tolerance && px <= extendedRight && py >= cellTop && py <= cellBottom) {
                Log.d("questo222DENTRO", "Point: (" + px + ", " + py + "), Checking right edge: extendedBottom = " + extendedRight + ", tolerance = " + tolerance);
                right = true;
            }

            // Check if the point touches the top edge of the cell
            Log.d("questo333", "Point: (" + px + ", " + py + "), Checking top edge: extendedTop = " + extendedTop + ", tolerance = " + tolerance + "cellLeft" + cellLeft + "cellRight" +  cellRight);
            if (py >= extendedTop && py <= extendedTop + tolerance && px >= cellLeft && px <= cellRight) {
                Log.d("questo333DENTRO", "Point: (" + px + ", " + py + "), Checking top edge: extendedTop = " + extendedTop + ", tolerance = " + tolerance);
                top = true;
            }

            // Check if the point touches the bottom edge of the cell
            if (py >= extendedBottom - tolerance && py <= extendedBottom && px >= cellLeft && px <= cellRight) {
                Log.d("questo444", "Point: (" + px + ", " + py + "), Checking bottom edge: extendedBottom = " + extendedBottom + ", tolerance = " + tolerance);
                bottom = true;
            }


            // If all boundaries are touched, return true
            if (left && right && top && bottom) {
                return true;
            }
        }

        return false;
    }



    private int getCellStartX(int columnIndex) {
        int currentX = 30; // Inizio della griglia
        for (int i = 0; i < columnIndex && i < columnWidths.length; i++) {
            currentX += columnWidths[i];
        }
        return currentX;
    }

    private int getCellStartY(int rowIndex) {
        int currentY = 30; // Inizio della griglia
        for (int i = 0; i < rowIndex && i < rowHeights.length; i++) {
            currentY += rowHeights[i];
        }
        return currentY;
    }


    public void draw(Canvas canvas) {
        // Disegna il percorso
        Paint trailPaint = new Paint();
        trailPaint.setColor(Color.YELLOW);
        trailPaint.setStyle(Paint.Style.FILL);

        // Variabili per calcolare la direzione del movimento
        float[] previousPoint = null;

        for (float[] point : trail) {
            float trailWidth, trailHeight;

            if (previousPoint != null) {
                float dx = point[0] - previousPoint[0];
                float dy = point[1] - previousPoint[1];

                // Calcola direzione del movimento
                if (Math.abs(dx) > Math.abs(dy)) {
                    // Movimento orizzontale
                    trailWidth = TRAIL_SIZE;
                    trailHeight = TRAIL_SIZE;
                } else {
                    // Movimento verticale
                    trailWidth = TRAIL_SIZE;
                    trailHeight = TRAIL_SIZE;
                }
            } else {
                // Default dimensions (prima iterazione)
                trailWidth = TRAIL_SIZE;
                trailHeight = TRAIL_SIZE;
            }

            // Disegna il rettangolo del trail
            canvas.drawRect(
                    point[0] - trailWidth / 2,
                    point[1] - trailHeight / 2,
                    point[0] + trailWidth / 2,
                    point[1] + trailHeight / 2,
                    trailPaint
            );

            // Aggiorna il punto precedente
            previousPoint = point;
        }


        // Disegna le celle completate
        Paint cellPaint = new Paint();
        cellPaint.setColor(Color.YELLOW);
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setAlpha(128);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(40); // Imposta la dimensione del testo
        textPaint.setTextAlign(Paint.Align.CENTER); // Allinea il testo al centro

        for (String cellKey : completedCells) {
            String[] parts = cellKey.split(",");
            int gridX = Integer.parseInt(parts[0]);
            int gridY = Integer.parseInt(parts[1]);

            // Ottieni le coordinate iniziali della cella
            int cellStartX = getCellStartX(gridX);
            int cellStartY = getCellStartY(gridY);

            // Ottieni la larghezza e l'altezza della cella
            int cellWidth = columnWidths[gridX];
            int cellHeight = rowHeights[gridY];

            // Calcola il margine per non coprire le linee della griglia (ad esempio, 5 pixel per lato)
            int margin = 7;

            // Disegna il rettangolo ridotto
            canvas.drawRect(
                    cellStartX + margin,           // Sposta verso destra
                    cellStartY + margin,           // Sposta verso il basso
                    cellStartX + cellWidth - margin, // Riduci larghezza
                    cellStartY + cellHeight - margin, // Riduci altezza
                    cellPaint
            );
            // Calcola il centro del rettangolo
            float centerX = cellStartX + cellWidth / 2.0f;
            float centerY = cellStartY + cellHeight / 2.0f;

            // Calcola l'offset verticale per centrare il testo rispetto alla baseline
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textCenterY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2;

            // Disegna il testo al centro
            canvas.drawText("+60", centerX, textCenterY, textPaint);
            counterPoints += 60;
        }

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


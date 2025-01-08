package com.example.knockoutarcade;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trail {
    private List<float[]> trail = new ArrayList<>(); // Traccia del percorso
    private Set<String> completedCells = new HashSet<>(); // Celle completate
    private static final int GRID_SIZE = 25; // Dimensione della griglia
    private Set<String> uniqueTrailPoints = new HashSet<>();
    private static final int TRAIL_SIZE = 20; // Dimensione del trail
    private int counterPoints = 0;
    private float x, y;  // Posizione del player
    private float previousX;
    private float previousY;
    private boolean[][] walkableMap;
    private MainActivity mainActivity;
    private Player player;
    private static Trail instance;
    public Trail(float x, float y, float previousX, float previousY, boolean[][] walkableMap, MainActivity mainActivity, Player player) {
        this.x = x;
        this.y = y;
        this.previousX = previousX;
        this.previousY = previousY;
        this.walkableMap = walkableMap;
        this.mainActivity = mainActivity;
        this.player = player;
        Log.d("TrailDebug", "Creazione nuova istanza di Trail");
    }    public static Trail getInstance(float x, float y, float previousX, float previousY, boolean[][] walkableMap, MainActivity mainActivity, Player player) {
        if (instance == null) {
            instance = new Trail(x, y, previousX, previousY, walkableMap, mainActivity, player);
        }
        return instance;
    }
    public void addTrailPoint() {

        // Ottieni la posizione attuale del giocatore
        float x = player.getX();
        float y = player.getY();

        // Ottieni la mappa percorribile
        boolean[][] walkableMap = player.getWalkableMap();
        int mapWidth = walkableMap.length;
        int mapHeight = walkableMap[0].length;

        // Calcola le coordinate centrate sulla griglia
        float alignedX = ((x + player.getWidth()/(2)) / GRID_SIZE) * GRID_SIZE; //divido per la dimensione della griglia (GRID_SIZE) e poi moltiplico di nuovo, per ottenere una coordinata "snap" ai bordi della cella
        float alignedY = ((y + player.getHeight()/(2)) / GRID_SIZE) * GRID_SIZE;
        int gridX = Math.round(alignedX / GRID_SIZE); //le coordinate vengono trasformate in indici di rifa e colonna della griglia
        int gridY = Math.round(alignedY / GRID_SIZE);
        Log.d("Grigliatore", "GridX: " + alignedX + " GridY: " + alignedY );
        // Controlla se le coordinate sono all'interno dei limiti della mappa
        if (gridX < 0 || gridY < 0 || gridX >= mapWidth || gridY >= mapHeight) {
            return;
        }

        // Controlla se il punto è percorribile nella mappa
        if (!walkableMap[(int) alignedX][(int) alignedY]) {
            return; // Punto non valido
        }


        // Evita duplicati nel trail
        for (float[] existingPoint : trail) {
            if (Math.abs(existingPoint[0] - alignedX) < GRID_SIZE / 2 && Math.abs(existingPoint[1] - alignedY) < GRID_SIZE / 2) {
                return; // Punto già presente
            }
        }

        // Aggiungi il punto al trail
        String pointKey = gridX + "," + gridY;
        if (!uniqueTrailPoints.contains(pointKey)) {
            trail.add(new float[]{alignedX, alignedY});
            uniqueTrailPoints.add(pointKey);
            Log.d("Trail", "Aggiunto punto: " + pointKey);
        }
    }

    private int[] columnWidths = {193, 193, 193, 193, 193};
    private int[] rowHeights = {470, 350, 470, 350, 470};

    public void checkCellCompletion(float x, float y) {
        if (trail.size() < 4){ Log.d("Aggiunta", "trail: " + trail.size()); return;} // Serve almeno un quadrato completo
        // Trova in quale cella si trova il player
        int gridX = getColumnIndex(x); //restituisce l'indice della colonna in base alla coordinata x
        int gridY = getRowIndex(y); //restituisce l'indice della riga in base alla coordinata y
        Log.d("Griglia:", "gridX: "+ gridX + "gridY" + gridY);
        // Salta il controllo se il player è fuori dalla griglia
        if (gridX == -1 || gridY == -1) return;

        String cellKey = gridX + "," + gridY; //chiave univoca per la cella corrente, combinando l'indice della colonna (gridX) e della riga (gridY)

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
        int currentX = 55; // Inizio della griglia
        for (int i = 0; i < columnWidths.length; i++) {
            if (posX >= currentX && posX < currentX + columnWidths[i]) {
                return i;
            }
            currentX += columnWidths[i];
        }
        return -1; // Fuori dai confini
    }

    private int getRowIndex(float posY) {
        int currentY = 55; // Inizio della griglia
        for (int i = 0; i < rowHeights.length; i++) {
            if (posY >= currentY && posY < currentY + rowHeights[i]) {
                return i;
            }
            currentY += rowHeights[i];
        }
        return -1; // Fuori dai confini
    }

    private boolean isCellSurrounded(int gridX, int gridY) {
        // Calcola i limiti della cella
        int cellLeft = getCellStartX(gridX); //coordinata X del lato sinistro della cella
        int cellRight = getCellEndX(gridX);  //coordinata X del lato destro della cella
        int cellTop = getCellStartY(gridY) + mainActivity.getStatusBarHeight(); //coordinata Y del lato superiore della cella
        int cellBottom = getCellEndY(gridY); //coordinata Y del lato inferiore della cella

        // Log dei valori calcolati
        Log.d("GridInfo", "Cell boundaries - Left: " + cellLeft + ", Right: " + cellRight +
                ", Top: " + cellTop + ", Bottom: " + cellBottom);


        int tolerance = 100;  // tolleranza per evitare errori di floating point

        // Estendi i confini della cella con la tolleranza, questo garantisce che piccoli errori di posizionamento non impediscano alla funzione di rilevare il contatto.
        float extendedLeft = cellLeft - tolerance;  //lato sinistro esteso verso sinistra di tolerance
        float extendedRight = cellRight + tolerance; //lato destro esteso verso destra di tolerance
        float extendedTop = cellTop - tolerance; //lato superiore esteso verso l'alto di tolerance
        float extendedBottom = cellBottom + tolerance; //Lato inferiore esteso verso il basso di tolerance
        Log.d("CellBoundaries", "GridX: " + gridX + ", GridY: " + gridY +
                ", Left: " + extendedLeft + ", Right: " + extendedRight +
                ", Top: " + extendedTop + ", Bottom: " + extendedBottom);
        boolean left = false, right = false, top = false, bottom = false;

        // Controlla se il trail tocca i confini estesi
        for (float[] point : trail) {
            float px = point[0];
            float py = point[1];

            if (px >= extendedLeft && px <= extendedLeft + tolerance && py >= cellTop && py <= cellBottom) { //se px è compreso tra extendedLeft e extendedLeft + tolerance, e il punto si trova verticalmente all'interno della cella (py tra cellTop e cellBottom), allora left diventa true
                Log.d("GridInfogf", "Cell boundaries QUI1");
                left = true;
            }
            if (px >= extendedRight - tolerance && px <= extendedRight && py >= cellTop && py <= cellBottom) { //se px è compreso tra extendedRight - tolerance e extendedRight, e il punto si trova verticalmente all'interno della cella, allora right diventa true
                Log.d("GridInfogf", "Cell boundaries QUI2");
                right = true;
            }
            if (py >= extendedTop && py <= extendedTop + tolerance && px >= cellLeft && px <= cellRight) { //se py è compreso tra extendedTop e extendedTop + tolerance, e il punto si trova orizzontalmente all'interno della cella (px tra cellLeft e cellRight), allora top diventa true
                Log.d("GridInfogf", "Cell boundaries QUI3");
                top = true;
            }
            if (py >= extendedBottom - tolerance && py <= extendedBottom && px >= cellLeft && px <= cellRight) { //se py è compreso tra extendedBottom - tolerance e extendedBottom, e il punto si trova orizzontalmente all'interno della cella, allora bottom diventa true
                Log.d("GridInfogf", "Cell boundaries QUI4");
                bottom = true;
            }if (!left && px < extendedLeft) {
                left = true;
                Log.d("Debug", "Left boundary touched");
            }
            if (!right && px > extendedRight) {
                right = true;
                Log.d("Debug", "Right boundary touched");
            }
            if (!top && py < extendedTop) {
                top = true;
                Log.d("Debug", "Top boundary touched");
            }
            if (!bottom && py > extendedBottom) {
                bottom = true;
                Log.d("Debug", "Bottom boundary touched");
            }
            // Se tutti i confini sono toccati, la cella è circondata
            if (left && right && top && bottom) {
                counterPoints = counterPoints + 60;
                return true;
            }
        }
        if (!left) Log.d("Debug", "Left not touched");
        if (!right) Log.d("Debug", "Right not touched");
        if (!top) Log.d("Debug", "Top not touched");
        if (!bottom) Log.d("Debug", "Bottom not touched");

        return false;  // La cella non è circondata
    }

    private int getCellStartX(int columnIndex) {
        int currentX = 55; // Inizio della griglia (con il bordo iniziale)
        for (int i = 0; i < columnIndex; i++) {
            currentX += columnWidths[i]; // Somma la larghezza delle colonne precedenti
        }
        return currentX;
    }

    private int getCellStartY(int rowIndex) {
        int currentY = mainActivity.getStatusBarHeight() + 55; // Inizio della griglia (con il bordo iniziale)
        for (int i = 0; i < rowIndex; i++) {
            currentY += rowHeights[i]; // Somma l'altezza delle righe precedenti
        }
        return currentY;
    }

    private int getCellEndX(int columnIndex) {
        return getCellStartX(columnIndex + 1); // Posizione del bordo destro della cella
    }

    private int getCellEndY(int rowIndex) {
        return getCellStartY(rowIndex + 1) - mainActivity.getStatusBarHeight(); // Posizione del bordo inferiore della cella
    }

    public void draw(Canvas canvas) {
        // Disegna il percorso
        Paint trailPaint = new Paint();
        trailPaint.setColor(Color.YELLOW);
        trailPaint.setStyle(Paint.Style.FILL);

        for (float[] point : trail) {

            Log.d("puntatore", "point[0]: " + point[0] + " point[1]: " + point[1]);
            // Disegna il rettangolo del trail
            canvas.drawRect(
                    point[0] - TRAIL_SIZE / 2,
                    point[1] - TRAIL_SIZE / 2,
                    point[0] + TRAIL_SIZE / 2,
                    point[1] + TRAIL_SIZE / 2,
                    trailPaint
            );
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
        Paint debugPaint = new Paint();
        debugPaint.setColor(Color.RED);
        debugPaint.setStyle(Paint.Style.STROKE);
        for (String cellKey : completedCells) {
            String[] parts = cellKey.split(",");
            int gridX = Integer.parseInt(parts[0]);
            int gridY = Integer.parseInt(parts[1]);

            // Ottieni le coordinate iniziali della cella
            int cellStartX = getCellStartX(gridX);
            int cellStartY = getCellStartY(gridY);
            Log.d("cellStartY", "cella: " + cellStartY);
            // Ottieni la larghezza e l'altezza della cella
            int cellWidth = columnWidths[gridX];
            int cellHeight = rowHeights[gridY];

            // Calcola il margine per non coprire le linee della griglia (ad esempio, 5 pixel per lato)
            int margin = 7;

            // Disegna il rettangolo ridotto
            canvas.drawRect(
                    cellStartX + margin,           // Sposta verso destra
                    (cellStartY - mainActivity.getStatusBarHeight()) + margin ,           // Sposta verso il basso
                    cellStartX + cellWidth - margin, // Riduci larghezza
                    (cellStartY - mainActivity.getStatusBarHeight()) + cellHeight - margin, // Riduci altezza
                    cellPaint
            );

            // Calcola il centro del rettangolo
            float centerX = cellStartX + cellWidth / 2.0f;
            float centerY = (cellStartY - mainActivity.getStatusBarHeight()) + cellHeight / 2.0f;

            // Calcola l'offset verticale per centrare il testo rispetto alla baseline
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textCenterY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2;

            // Disegna il testo al centro
            canvas.drawText("+60", centerX, textCenterY, textPaint);
            Log.d("puntiorarararara", "punti get: " + getCounterPoints());

        }
    }
    public synchronized int getCounterPoints() {
        return counterPoints;
    }

}
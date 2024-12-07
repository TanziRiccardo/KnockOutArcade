package com.example.knockoutarcade;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private Player player;
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private int fieldWidth;
    private int fieldHeight;
    private int screenWidthapp;
    private Bitmap background;
    private int imageWidth, imageHeight;
    private int backgroundX1, backgroundX2;
    private int backgroundOffsetX = 0;
    private int backgroundSpeed = 0;
    private boolean isPadVisible;
    private Bitmap upArrow, downArrow, leftArrow, rightArrow;  // Immagini per le frecce
    private Bitmap basePad;  // Base del pad direzionale
    private Bitmap sceneBitmap;
    private ArrayList<Trail> trails; // Lista per le tracce
    public boolean[][] walkableMap;
    private List<Bot> bots; // Lista per memorizzare i bot
    private int tolerance;
    private int desiredDirectionX = 0; // Direzione scelta dal giocatore (1=destra, -1=sinistra, 0=nessuna direzione)
    private int desiredDirectionY = 0; // Direzione scelta dal giocatore (1=giù, -1=su, 0=nessuna direzione)
    private int currentDirectionX = 0; // Direzione attuale del movimento
    private int currentDirectionY = 0; // Direzione attuale del movimento
    private Handler handler = new Handler();
    private Runnable stopMovementTask;
    private boolean isNearIntersection = false;
    private boolean waitingForDirection = false;
    private static final int DIRECTION_TIMEOUT = 1000; // Tempo limite in millisecondi (1 secondo)

    // Variabile booleana per avviare il movimento dei bot solo la prima volta che viene chiamato onDraw
    private boolean firstTimeDraw = true;

    private static final int TRAIL_LIFETIME = 3000; // Durata di ogni traccia in millisecondi
    public GameView(Context context, int screenWidth, int screenHeight) {
        super(context);
        surfaceHolder = getHolder();
        paint = new Paint();
        // Carica lo sfondo e le frecce direzionali
        background = BitmapFactory.decodeResource(context.getResources(), R.drawable.sfondo_prova);
        background = Bitmap.createScaledBitmap(background, screenWidth, screenHeight, true);
        imageWidth = background.getWidth();
        imageHeight = background.getHeight();
        upArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow_up);
        downArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow_down);
        leftArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow_left);
        rightArrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow_right);
        basePad = BitmapFactory.decodeResource(context.getResources(), R.drawable.base_pad); // Base del pad direzionale

        upArrow = Bitmap.createScaledBitmap(upArrow, 100, 100, true);
        downArrow = Bitmap.createScaledBitmap(downArrow, 100, 100, true);
        leftArrow = Bitmap.createScaledBitmap(leftArrow, 100, 100, true);
        rightArrow = Bitmap.createScaledBitmap(rightArrow, 100, 100, true);
        basePad = Bitmap.createScaledBitmap(basePad, 500, 500, true);  // Dimensioni della base del pad
        isPadVisible = false;
        fieldWidth = background.getWidth();
        fieldHeight = screenHeight;
        screenWidthapp = screenWidth;
        player = new Player(context, R.drawable.player_bross, screenWidth, screenHeight, 6, fieldWidth / 2, fieldHeight - 200);
        trails = new ArrayList<>();

        initializePlayerPosition();
        Bitmap sceneBitmap = getBitmapFromScene();
        initializeWalkableMap(sceneBitmap);
        Bot bot = new Bot(walkableMap, (int) player.getX(), (int) player.getY());
        bots = new ArrayList<>(); // Inizializza la lista
        initializeBots(); // Aggiungi bot alla lista
        tolerance = 5;
    }

    private void initializeBots() {
            bots = new ArrayList<>();

        // Posiziona i bot su linee bianche specifiche della griglia
        bots.add(new Bot(walkableMap, fieldWidth / 4, fieldHeight / 5)); // Posizione sulla prima linea bianca
        bots.add(new Bot(walkableMap, 2 * fieldWidth / 4, 2 * fieldHeight / 5)); // Posizione su un'altra linea bianca
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();  // Chiama il metodo draw() personalizzato
            sleep();
        }
    }
    private void startDirectionTimeout() {
        if (stopMovementTask != null) {
            handler.removeCallbacks(stopMovementTask);
        }

        waitingForDirection = true;

        stopMovementTask = () -> {
            if (waitingForDirection) {
                stopPlayer(); // Ferma il player se non è stata scelta una direzione
            }
        };

        handler.postDelayed(stopMovementTask, DIRECTION_TIMEOUT);
    }

    private void stopPlayer() {
        player.setSpeed(0, 0);
        currentDirectionX = 0;
        currentDirectionY = 0;
        waitingForDirection = false;
    }
    private void changeDirection(int desiredX, int desiredY) {
        if (isNearIntersection) {
            handler.removeCallbacks(stopMovementTask); // Interrompi il timer
            waitingForDirection = false;

            if (desiredX != 0 && canMoveTo((int) player.getX() + desiredX * 10, (int) player.getY())) {
                currentDirectionX = desiredX;
                currentDirectionY = 0;
            } else if (desiredY != 0 && canMoveTo((int) player.getX(), (int) player.getY() + desiredY * 10)) {
                currentDirectionX = 0;
                currentDirectionY = desiredY;
            }
        }
    }

    private void initializePlayerPosition() {
        // Calcola le dimensioni delle celle
        float cellWidth = (float) fieldWidth / 4;   // Usa float per precisione
        float cellHeight = (float) fieldHeight / 5; // Usa float per precisione

        // Posizione iniziale sulla griglia (centrato sulla cella centrale)
        float startX = 2 * cellWidth - (player.getWidth() / 2.0f); // Centrato sulla 2a colonna
        float startY = 2 * cellHeight - (player.getHeight() / 2.0f); // Centrato sulla 2a riga

        player.setX((int) startX); // Posizione iniziale del giocatore in X
        player.setY((int) startY); // Posizione iniziale del giocatore in Y
    }


    public void update() {
        for (Bot bot : bots) {
            bot.setTarget((int) player.getX(), (int) player.getY()); // Passa le coordinate del giocatore come target
        }
        player.update(); // Aggiorna animazioni o stato del giocatore

        if (waitingForDirection) {
            // Il giocatore è in attesa di una direzione, quindi non muoviamolo
            return;
        }

        int nextX = (int) player.getX() + currentDirectionX * player.getSpeedX();
        int nextY = (int) player.getY() + currentDirectionY * player.getSpeedY();

        if (canMoveTo(nextX, (int) player.getY()) && currentDirectionX != 0) {
            player.setX(nextX);
        } else if (canMoveTo((int) player.getX(), nextY) && currentDirectionY != 0) {
            player.setY(nextY);
        }

        if (isNearIntersection()) {
            alignToGrid(); // Allinea il giocatore
            isNearIntersection = true;
        } else {
            isNearIntersection = false;
        }

        for (Bot bot: bots){
            bot.moveBotTowardsTarget();
        }

        // Dimensione dell'area statica (margini ai bordi)
        int staticAreaMargin = 10; // Imposta un margine per il movimento dello sfondo (puoi regolare questo valore)

        // Controlla se il giocatore si avvicina ai bordi
        if (player.getX() < staticAreaMargin) {
            // Sposta lo sfondo a destra (solo quando il giocatore è a sinistra)
            backgroundSpeed = 5;
            if (player.getSpeedX() == 0) {
                backgroundSpeed = 0;
            }
        } else if (player.getX() > screenWidthapp - staticAreaMargin - player.getWidth()) {
            // Sposta lo sfondo a sinistra (solo quando il giocatore è a destra)
            backgroundSpeed = -5;
            if (player.getSpeedX() == 0) {
                backgroundSpeed = 0;
            }
        } else {
            // Lo sfondo resta immobile (quando il giocatore non è vicino ai bordi)
            backgroundSpeed = 0;
        }

        // Blocca il giocatore all'interno dei confini dello schermo
        if (player.getX() < 0) {
            player.setX(0);
        } else if (player.getX() + player.getWidth() > screenWidthapp) {
            player.setX(screenWidthapp - player.getWidth());
        }
        if (player.getY() < 0) {
            player.setY(0);
        } else if (player.getY() + player.getHeight() > fieldHeight) {
            player.setY(fieldHeight - player.getHeight());
        }
        // Aggiorna la posizione dello sfondo in base alla velocità
        updateBackgroundPosition();
    }


    public Bitmap getBitmapFromScene() {
        // Crea un Bitmap con le dimensioni della scena
        Bitmap bitmap = Bitmap.createBitmap(screenWidthapp, fieldHeight, Bitmap.Config.ARGB_8888);

        // Crea una canvas per disegnare su questo bitmap
        Canvas canvas = new Canvas(bitmap);

        // Disegna lo sfondo
        canvas.drawBitmap(background, -backgroundOffsetX, 0, null);

        // Disegna la griglia
        drawGrid(canvas);
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "scene_bitmap.png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Log.d("BitmapSave", "File salvato in: " + file.getAbsolutePath());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


    public void initializeWalkableMap(Bitmap sceneBitmap) {
        int width = sceneBitmap.getWidth();
        int height = sceneBitmap.getHeight();
        walkableMap = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixelColor = sceneBitmap.getPixel(x, y);
                int red = Color.red(pixelColor);
                int green = Color.green(pixelColor);
                int blue = Color.blue(pixelColor);

                int tolerance = 10;
                walkableMap[x][y] = Math.abs(red - 255) < tolerance &&
                        Math.abs(green - 255) < tolerance &&
                        Math.abs(blue - 255) < tolerance;
            }
        }
    }
    public boolean canMoveTo(int x, int y) {
        int tolerance = 5;  // Diamo una tolleranza di 5 pixel
        if (x < tolerance || y < tolerance || x >= walkableMap.length - tolerance || y >= walkableMap[0].length - tolerance) {
            return false;
        }
        return walkableMap[x][y];
    }
    private void alignToGrid() {
        int cellWidth = fieldWidth / 4;
        int cellHeight = fieldHeight / 5;

        int playerX = (int) player.getX();
        int playerY = (int) player.getY();

        // Allinea il giocatore orizzontalmente
        if (currentDirectionX != 0) {
            player.setY(Math.round(playerY / (float) cellHeight) * cellHeight);
        }

        // Allinea il giocatore verticalmente
        if (currentDirectionY != 0) {
            player.setX(Math.round(playerX / (float) cellWidth) * cellWidth);
        }
    }

    private boolean isNearIntersection() {
        int cellWidth = fieldWidth / 4;
        int cellHeight = fieldHeight / 5;

        int playerX = (int) player.getX();
        int playerY = (int) player.getY();

        int offsetX = playerX % cellWidth;
        int offsetY = playerY % cellHeight;

        // Controlla se il giocatore è entro una tolleranza (es. 5 pixel) dall'incrocio
        return offsetX <= 5 || (cellWidth - offsetX) <= 5 ||
                offsetY <= 5 || (cellHeight - offsetY) <= 5;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float touchX = event.getX();
        float touchY = event.getY();

        // Impostiamo isPadVisible a true quando il giocatore tocca lo schermo
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            isPadVisible = true;
            waitingForDirection = false;
            handler.removeCallbacks(stopMovementTask); // Interrompi il timer
        } else if (action == MotionEvent.ACTION_UP) {
            isPadVisible = false;
            desiredDirectionX = 0;
            desiredDirectionY = 0;
        }

        // Calcoliamo la larghezza e l'altezza delle celle
        float cellWidth = (float) fieldWidth / 4;
        float cellHeight = (float) fieldHeight / 5;

        // Controllo del movimento
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            // Verifica se il giocatore vuole andare a destra
            if (isTouchInsideArea(touchX, touchY, screenWidthapp - 650, fieldHeight - 250, 100, 100)) {
                if (player.getX() + player.getWidth() < (cellWidth * 3)) {
                    int newX = (int) (player.getX() + player.getWidth() + 1);
                    if (canMoveTo(newX, (int) player.getY() + 1) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() / 2) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() - 1)) {
                        player.setSpeed(10, player.getSpeedY());
                        desiredDirectionX = 1;
                        desiredDirectionY = 0;
                        Log.d("Movement", "Moving right");
                    } else {
                        Log.d("Movement", "Blocked right");
                    }
                }
            }
            // Verifica se il giocatore vuole andare a sinistra
            else if (isTouchInsideArea(touchX, touchY, screenWidthapp - 950, fieldHeight - 250, 100, 100)) {
                if (player.getX() > cellWidth) {
                    int newX = (int) (player.getX() - 1);
                    if (canMoveTo(newX, (int) player.getY() + 1) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() / 2) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() - 1)) {
                        player.setSpeed(-10, player.getSpeedY());
                        desiredDirectionX = -1;
                        desiredDirectionY = 0;
                        Log.d("Movement", "Moving left");
                    } else {
                        Log.d("Movement", "Blocked left");
                    }
                }
            }
            // Verifica se il giocatore vuole andare su
            else if (isTouchInsideArea(touchX, touchY, screenWidthapp - 800, fieldHeight - 400, 100, 100)) {
                if (player.getY() > cellHeight) {
                    int newY = (int) (player.getY() - 5);
                    if (canMoveTo((int) player.getX() + player.getWidth() / 2, newY)) {
                        player.setSpeed(player.getSpeedX(), -10);
                        desiredDirectionX = 0;
                        desiredDirectionY = -1;
                        Log.d("Movement", "Moving up");
                    } else {
                        Log.d("Movement", "Blocked up");
                    }
                }
            }
            // Verifica se il giocatore vuole andare giù
            else if (isTouchInsideArea(touchX, touchY, screenWidthapp - 800, fieldHeight - 100, 100, 100)) {
                if (player.getY() + player.getHeight() < (cellHeight * 4)) {
                    int newY = (int) (player.getY() + player.getHeight() + 1);
                    if (canMoveTo((int) player.getX() + player.getWidth() / 2, newY)) {
                        player.setSpeed(player.getSpeedX(), 10);
                        desiredDirectionX = 0;
                        desiredDirectionY = 1;
                        Log.d("Movement", "Moving down");
                    } else {
                        Log.d("Movement", "Blocked down");
                    }
                }
            }
        }
        return true;
    }



    // Funzione per verificare se il tocco è dentro un'area specifica
    private boolean isTouchInsideArea(float touchX, float touchY, float x, float y, float width, float height) {
        return touchX >= x && touchX <= (x + width) && touchY >= y && touchY <= (y + height);
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            drawGame(canvas);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawGame(Canvas canvas) {
        Paint botpaint = new Paint();
        botpaint.setColor(Color.YELLOW);
        // Cancella il canvas
        canvas.drawColor(Color.parseColor("#1D481D"));

        // Disegna lo sfondo
        updateBackgroundPosition();
        canvas.drawBitmap(background, -backgroundOffsetX, 0, null);

        drawGrid(canvas);
        drawTrails(canvas); // Disegna le tracce
        // Disegna la base del pad e le frecce, se il pad è visibile
        if (isPadVisible) {
            drawDirectionPad(canvas);
        }

        if (bots != null) {
            for (Bot bot : bots) {
                canvas.drawCircle(bot.getX(), bot.getY(), 20, botpaint); // Disegna il bot
            }
        }
        // Disegna il giocatore
        player.draw(canvas);

    }

    private void drawTrails(Canvas canvas) {
        Paint trailPaint = new Paint();
        trailPaint.setColor(Color.YELLOW);
        trailPaint.setStyle(Paint.Style.FILL);

        for (Trail trail : trails) {
            canvas.drawRect(
                    trail.getX(),
                    trail.getY(),
                    trail.getX() + player.getWidth() / 2,
                    trail.getY() + player.getHeight() / 2,
                    trailPaint
            );
        }
    }
    private void drawDirectionPad(Canvas canvas) {

        // Disegna le frecce direzionali sopra la base
        canvas.drawBitmap(upArrow, screenWidthapp - 800, fieldHeight - 400, paint);  // Freccia su
        canvas.drawBitmap(downArrow, screenWidthapp - 800, fieldHeight - 100, paint);  // Freccia giù
        canvas.drawBitmap(leftArrow, screenWidthapp - 950, fieldHeight - 250, paint);  // Freccia sinistra
        canvas.drawBitmap(rightArrow, screenWidthapp - 650, fieldHeight - 250, paint);  // Freccia destra
    }

    private void drawGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.argb(255, 255, 255, 255)); // Bianco opaco
        gridPaint.setStrokeWidth(10);

        int numColumns = 4; // Numero di colonne
        int numRows = 5; // Numero di righe
        int cellWidth = screenWidthapp / numColumns; // Larghezza delle celle
        int cellHeight = fieldHeight / numRows; // Altezza delle celle

        // Disegna linee verticali
        for (int i = 0; i <= numColumns; i++) { // Aggiungi una linea in più per il bordo finale
            float x = i * cellWidth;
            canvas.drawLine(x, 0, x, fieldHeight, gridPaint);
        }

        // Disegna linee orizzontali
        for (int j = 0; j <= numRows; j++) { // Aggiungi una linea in più per il bordo finale
            float y = j * cellHeight;
            canvas.drawLine(0, y, screenWidthapp, y, gridPaint);
        }

    }

    private void updateBackgroundPosition() {
        if (backgroundSpeed != 0) {
            // Muove lo sfondo in base alla velocità del giocatore
            backgroundOffsetX -= backgroundSpeed;
        }

        // Gestione ciclica dello sfondo: quando l'offset esce dallo schermo, lo resettiamo
        if (backgroundOffsetX <= -fieldWidth) {
            backgroundOffsetX = 0;
        } else if (backgroundOffsetX >= fieldWidth) {
            backgroundOffsetX = 0;
        }
    }


    public boolean[][] getWalkableMap() {
        return walkableMap;
    }
    private void sleep() {
        try {
            Thread.sleep(17); // Circa 60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

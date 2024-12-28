package com.example.knockoutarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.media.MediaPlayer;
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
    private int[] rowHeights;
    private int[] columnWidths;
    private static final int TRAIL_LIFETIME = 3000; // Durata di ogni traccia in millisecondi
    private MainActivity mainActivity;
    public GameView(Context context, int screenWidth, int screenHeight, MainActivity mainActivity) {
        super(context);
        this.mainActivity = mainActivity;
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
        trails = new ArrayList<>();
        // Definisci larghezze delle colonne (valori personalizzabili)
        columnWidths = new int[]{202, 202, 202, 202, 202};
        // Definisci altezze delle righe (valori personalizzabili)
        rowHeights = new int[]{486, 350, 486, 350, 486};
        Bitmap sceneBitmap = getBitmapFromScene();
        initializeWalkableMap(sceneBitmap);
        player = new Player(context, R.drawable.player_bross, screenWidth, screenHeight, 6, fieldWidth / 2, fieldHeight - 200, walkableMap);
        initializePlayerPosition();
        bots = new ArrayList<>(); // Inizializza la lista
        initializeBots(); // Aggiungi bot alla lista

        tolerance = 5;
    }

    private void initializeBots() {
            bots = new ArrayList<>();
        // Calcolare la posizione del primo bot tra la prima e la seconda riga
        int firstBotY = rowHeights[0] + (rowHeights[1] - rowHeights[0]) / 2; // Posizione tra prima e seconda riga
        int firstBotX = columnWidths[1] + 30; // Colonna tra la prima e la seconda colonna

        // Calcolare la posizione del secondo bot tra l'ultima e la penultima riga
        int secondBotY = 600; // Posizione tra l'ultima e la penultima riga
        int secondBotX = 556; // Colonna tra l'ultima e la penultima colonna

        // Posizionare i bot
        Bot luigiBot = new Bot(getContext(), R.drawable.luigi_bot, walkableMap, firstBotX, firstBotY, Bot.BotType.LUIGI, screenWidthapp, fieldHeight); // Primo bot
        Bot toadBot = new Bot(getContext(), R.drawable.toad_bot, walkableMap, 231, 1787, Bot.BotType.TOAD, screenWidthapp, fieldHeight); // Secondo bot
        Bot fireBot = new Bot(getContext(), R.drawable.fire_bot, walkableMap, 838, 1787, Bot.BotType.FIRE, screenWidthapp, fieldHeight);
        bots.add(luigiBot);
        bots.add(toadBot);
        bots.add(fireBot);
        for (Bot bot : bots){
            bot.setAllBots(bots);
        }

    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();  // Chiama il metodo draw() personalizzato
            sleep();
        }
    }
    private void initializePlayerPosition() {
        // Calcola le dimensioni delle celle
        float cellWidth = (float) fieldWidth / 5;   // Usa float per precisione
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
            bot.bot_update();
        }
        player.update(); // Aggiorna animazioni o stato del giocatore



        for (Bot bot: bots){
            bot.moveBotTowardsTarget();

            if(checkCollision(player, bot)){
                handleCollision(player, bot);
            }
        }
        // Sposta il giocatore al lato opposto quando tocca un bordo
        if (player.getX() < 0) {
            player.setX(screenWidthapp - player.getWidth()); // Compara al lato destro
        } else if (player.getX() + player.getWidth() > screenWidthapp) {
            player.setX(0); // Compara al lato sinistro
        }

        if (player.getY() < 0) {
            player.setY(fieldHeight - player.getHeight()); // Compara al lato inferiore
        } else if (player.getY() + player.getHeight() > fieldHeight) {
            player.setY(0); // Compara al lato superiore
        }
    }

    public boolean checkCollision(Player player, Bot bot) {
        // Ottieni le coordinate e le dimensioni del player
        float playerLeft = player.getX();
        float playerTop = player.getY();
        float playerRight = player.getX() + player.getWidth();
        float playerBottom = player.getY() + player.getHeight();

        // Ottieni le coordinate e le dimensioni del bot
        float botLeft = bot.getX();
        float botTop = bot.getY();
        float botRight = bot.getX() + (bot.getWidth()/9);
        float botBottom = bot.getY() + (bot.getHeight()/2);
// Log per stampare i dati
        Log.d("Collision Info", "Player Coordinates: Left=" + playerLeft + ", Top=" + playerTop + ", Right=" + playerRight + ", Bottom=" + playerBottom);
        Log.d("Collision Info", "Bot Coordinates: Left=" + botLeft + ", Top=" + botTop + ", Right=" + botRight + ", Bottom=" + botBottom);
        // Verifica se le due bounding boxes si sovrappongono
        int tolerance = 30; //i bot si avvicinano il più possibile al player
        return playerRight >= botLeft + tolerance && playerLeft + tolerance <= botRight  &&
                playerBottom >= botTop + tolerance && playerTop + tolerance <= botBottom;
    }
    public void handleCollision(Player player, Bot bot) {
        Log.d("Collision Info", "ESCE");
        MediaPlayer mediaPlayer = mainActivity.getMediaPlayer(); // Ottieni il MediaPlayer
        // Verifica che il MediaPlayer sia valido prima di utilizzarlo
        if (mediaPlayer != null) {
            try {
                // Ferma la musica corrente
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;

                // Riproduci un'altra traccia audio per la collisione
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.knockout_loose); // sostituisci con il nome del tuo file audio
                mediaPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();  // Gestisci l'eccezione
            }
        }


        // 1. Anima il giocatore verso il basso
        animatePlayerFall(player);

        animateScreenClose();
            // Aggiungi logica per reazioni diverse alla collisione, ad esempio:
            // - Rallenta il giocatore
            // - Cambia direzione del giocatore
            // - Esegui altre azioni

    }
    private void animatePlayerFall(Player player) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            // Animazione per far cadere il player verso il basso
            ValueAnimator fallAnimator = ValueAnimator.ofFloat(player.getY(), fieldHeight);
            fallAnimator.setDuration(1000);
            fallAnimator.setInterpolator(new LinearInterpolator());

            fallAnimator.addUpdateListener(animation -> {
                float newY = (float) animation.getAnimatedValue();
                player.setY(newY);

                // Forza il ridisegno della GameView
                invalidate();
            });

            // Avvia l'animazione
            fallAnimator.start();
        });
    }
    private float screenCloseProgress = 0f; // Valore iniziale di progresso
    private void animateScreenClose() {
        this.post(() -> { // Questo assicura che il codice venga eseguito nel UI Thread
            // Animator per chiudere lo schermo
            ValueAnimator closeAnimator = ValueAnimator.ofFloat(0f, 1f);
            closeAnimator.setDuration(1000); // Durata dell'animazione
            closeAnimator.setInterpolator(new LinearInterpolator());

            closeAnimator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();
                // Logica per disegnare l'animazione di chiusura
                screenCloseProgress = progress; // Variabile globale per il progresso
                invalidate(); // Richiede il ridisegno della GameView
            });

            closeAnimator.start();
        });
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
                walkableMap[x][y] = Math.abs(red - 0) < tolerance &&
                        Math.abs(green - 195) < tolerance &&
                        Math.abs(blue - 222) < tolerance;
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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float touchX = event.getX();
        float touchY = event.getY();

        // Impostiamo isPadVisible a true quando il giocatore tocca lo schermo
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            isPadVisible = true;
            handler.removeCallbacks(stopMovementTask); // Interrompi il timer
        } else if (action == MotionEvent.ACTION_UP) {
            isPadVisible = false;
            desiredDirectionX = 0;
            desiredDirectionY = 0;
            player.setSpeed(0,0);
        }

        // Calcoliamo la larghezza e l'altezza delle celle
        float cellWidth = (float) fieldWidth / 5;
        float cellHeight = (float) fieldHeight / 5;

        // Controllo del movimento
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            // Verifica se il giocatore vuole andare a destra
            if (isTouchInsideArea(touchX, touchY, screenWidthapp - 650, fieldHeight - 250, 100, 100)) {
                Log.d("Movement", "cliccato destra");
                Log.d("Movementyyyyy", "player.getX(): " + player.getX() + "player.getWidth()" + player.getWidth() + "CellWidth: " + cellWidth*3);
                if (player.getX() + player.getWidth() < (cellWidth * 3) || player.getX() + player.getWidth() > (cellWidth * 3)) {
                    int newX = (int) (player.getX() + player.getWidth() + 1);
                    if (canMoveTo(newX, (int) player.getY() + 1) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() / 2) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() - 1)) {
                        // Imposta la velocità e la direzione desiderata
                        player.setSpeed(10, player.getSpeedY());
                        desiredDirectionX = 1;
                        desiredDirectionY = 0;
                        // Verifica se il giocatore è all'incrocio
                        if (player.isNearIntersection()) {
                            // Quando il giocatore è all'incrocio, controlla la direzione
                            if (desiredDirectionX != 0 || desiredDirectionY != 0) {
                                player.moveToNextIntersection();
                                // Muovi il giocatore nella direzione richiesta
                                player.turnToRequestedDirection(desiredDirectionX, desiredDirectionY);
                                Log.d("Movement", "Player moving towards desired direction");
                            }
                        }
                        Log.d("Movement", "Moving right");
                    } else {
                        Log.d("Movement", "Blocked right");
                    }
                }
            }
            // Verifica se il giocatore vuole andare a sinistra
            else if (isTouchInsideArea(touchX, touchY, screenWidthapp - 950, fieldHeight - 250, 100, 100)) {
                if (player.getX() > cellWidth || player.getX() < cellWidth) {
                    int newX = (int) (player.getX() - 1);
                    if (canMoveTo(newX, (int) player.getY() + 1) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() / 2) ||
                            canMoveTo(newX, (int) player.getY() + player.getHeight() - 1)) {
                        // Imposta la velocità e la direzione desiderata
                        player.setSpeed(-10, player.getSpeedY());
                        desiredDirectionX = -1;
                        desiredDirectionY = 0;
                        // Verifica se il giocatore è all'incrocio
                        if (player.isNearIntersection()) {
                            // Quando il giocatore è all'incrocio, controlla la direzione
                            if (desiredDirectionX != 0 || desiredDirectionY != 0) {
                                player.moveToNextIntersection();
                                // Muovi il giocatore nella direzione richiesta
                                player.turnToRequestedDirection(desiredDirectionX, desiredDirectionY);
                                Log.d("Movement", "Player moving towards desired direction");
                            }
                        }
                        Log.d("Movement", "Moving left");
                    } else {
                        Log.d("Movement", "Blocked left");
                    }
                }
            }
            // Verifica se il giocatore vuole andare su
            else if (isTouchInsideArea(touchX, touchY, screenWidthapp - 800, fieldHeight - 400, 100, 100)) {
                if (player.getY() + player.getHeight() > (cellHeight) || player.getY() + player.getHeight() < (cellHeight)) {
                    int newY = (int) (player.getY() - 5);
                    if (canMoveTo((int) player.getX() + player.getWidth() / 2, newY)) {
                        // Imposta la velocità e la direzione desiderata
                        player.setSpeed(player.getSpeedX(), -10);
                        desiredDirectionX = 0;
                        desiredDirectionY = -1;
                        // Verifica se il giocatore è all'incrocio
                        if (player.isNearIntersection()) {
                            // Quando il giocatore è all'incrocio, controlla la direzione
                            if (desiredDirectionX != 0 || desiredDirectionY != 0) {
                                player.moveToNextIntersection();
                                // Muovi il giocatore nella direzione richiesta
                                player.turnToRequestedDirection(desiredDirectionX, desiredDirectionY);
                                Log.d("Movement", "Player moving towards desired direction");
                            }
                        }
                        Log.d("Movement", "Moving up");
                    } else {
                        Log.d("Movement", "Blocked up");
                    }
                }
            }
            // Verifica se il giocatore vuole andare giù
            else if (isTouchInsideArea(touchX, touchY, screenWidthapp - 800, fieldHeight - 100, 100, 100)) {
                if (player.getY() + player.getHeight() < (cellHeight * 5)) {
                    int newY = (int) (player.getY() + player.getHeight() + 5);
                    if (canMoveTo((int) player.getX() + player.getWidth() / 2, newY)) {
                        // Imposta la velocità e la direzione desiderata
                        player.setSpeed(player.getSpeedX(), 10);
                        desiredDirectionX = 0;
                        desiredDirectionY = 1;
                        // Verifica se il giocatore è all'incrocio
                        if (player.isNearIntersection()) {
                            // Quando il giocatore è all'incrocio, controlla la direzione
                            if (desiredDirectionX != 0 || desiredDirectionY != 0) {
                                player.moveToNextIntersection();
                                // Muovi il giocatore nella direzione richiesta
                                player.turnToRequestedDirection(desiredDirectionX, desiredDirectionY);
                                Log.d("Movement", "Player moving towards desired direction");
                            }
                        }
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
        // Cancella il canvas
        canvas.drawColor(Color.BLACK);

        // Disegna lo sfondo
        updateBackgroundPosition();
        canvas.drawBitmap(background, -backgroundOffsetX, 0, null);

        drawGrid(canvas);
        drawFieldBorder(canvas);
        // Disegna la base del pad e le frecce, se il pad è visibile
        if (isPadVisible) {
            drawDirectionPad(canvas);
        }

        if (bots != null) {
            for (Bot bot : bots) {
                bot.draw_bot(canvas);
            }
        }
        // Disegna il giocatore
        player.draw(canvas);
        // Crea un Paint per i rettangoli
        Paint rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE); // Modalità bordo (no riempimento)
        rectPaint.setColor(Color.RED);          // Colore del rettangolo
        rectPaint.setStrokeWidth(5);            // Spessore della linea

        // Disegna il rettangolo del giocatore
        float playerLeft = player.getX();
        float playerTop = player.getY();
        float playerRight = player.getX() + player.getWidth();
        float playerBottom = player.getY() + player.getHeight();
        canvas.drawRect(playerLeft, playerTop, playerRight, playerBottom, rectPaint);

        // Disegna i rettangoli dei bot
        for (Bot bot : bots) {
            float botLeft = bot.getBotX();
            float botTop = bot.getBotY();
            float botRight = botLeft + (bot.getWidth() / 9);
            float botBottom = botTop + (bot.getHeight() / 3);
            canvas.drawRect(botLeft, botTop, botRight, botBottom, rectPaint);
        }

        // Effetto di chiusura dello schermo (cerchio nero)
        if (screenCloseProgress > 0) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            float maxRadius = (float) Math.hypot(centerX, centerY);
            float currentRadius = maxRadius * (1 - screenCloseProgress);

            Paint blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);

            // Disegna il cerchio nero
            canvas.drawCircle(centerX, centerY, currentRadius, blackPaint);

                showGameOverScreen(); // Chiama il metodo per mostrare la schermata di Game Over


        }
    }
    // Metodo per salvare il punteggio
    private void savePlayerScore(Context context, int score) {
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Recupera i punteggi esistenti
        String json = prefs.getString("scores", null);
        List<Integer> scores;
        if (json != null) {
            Type type = new TypeToken<List<Integer>>() {}.getType();
            scores = new Gson().fromJson(json, type);
        } else {
            scores = new ArrayList<>();
        }

        // Aggiungi il punteggio attuale
        scores.add(score);

        // Salva nuovamente i punteggi come JSON
        String updatedJson = new Gson().toJson(scores);
        editor.putString("scores", updatedJson);
        editor.apply();
    }
    private void showGameOverScreen() {
        Intent intent = new Intent(getContext(), GameOverActivity.class);
        savePlayerScore(getContext(), player.getCounterPoints()); // Passa il punteggio del giocatore
        getContext().startActivity(intent);
    }
    private void drawFieldBorder(Canvas canvas) {
        // Carica il Bitmap del bordo
        Bitmap borderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.borderbitmap);

        int borderWidth = 30; // Spessore del bordo

        // Crea un Bitmap scalato in base alla larghezza del bordo
        int scaledSize = borderWidth; // Il bordo dovrebbe avere larghezza uguale al quadrato
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(borderBitmap, scaledSize, scaledSize, false);

        // Configura il BitmapShader con TileMode.REPEAT
        BitmapShader borderShader = new BitmapShader(scaledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        Paint borderPaint = new Paint();
        borderPaint.setShader(borderShader);

        // Disegna il bordo superiore
        canvas.drawRect(0, 0, screenWidthapp, borderWidth, borderPaint);

        // Disegna il bordo inferiore
        canvas.drawRect(0, fieldHeight - borderWidth, screenWidthapp, fieldHeight, borderPaint);

        // Disegna il bordo sinistro
        canvas.drawRect(0, 0, borderWidth, fieldHeight, borderPaint);

        // Disegna il bordo destro
        canvas.drawRect(screenWidthapp - borderWidth, 0, screenWidthapp, fieldHeight, borderPaint);
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
        gridPaint.setColor(Color.argb(255, 0, 195, 222)); // Colore della griglia
        gridPaint.setStrokeWidth(25);

        int borderWidth = 30; // Spessore del bordo decorativo

        // Definisci larghezze delle colonne (valori personalizzabili)
        int[] columnWidths = {202, 202, 202, 202, 202};
        // Definisci altezze delle righe (valori personalizzabili)
        int[] rowHeights = {486, 350, 486, 350, 486};

        // Calcola la posizione delle linee verticali tenendo conto del bordo
        int currentX = borderWidth;
        for (int i = 0; i <= columnWidths.length; i++) {
            canvas.drawLine(currentX, borderWidth, currentX, fieldHeight - borderWidth, gridPaint); // Linee verticali
            if (i < columnWidths.length) {
                currentX += columnWidths[i];
            }
        }

        // Calcola la posizione delle linee orizzontali tenendo conto del bordo
        int currentY = borderWidth;
        for (int j = 0; j <= rowHeights.length; j++) {
            canvas.drawLine(borderWidth, currentY, screenWidthapp - borderWidth, currentY, gridPaint); // Linee orizzontali
            if (j < rowHeights.length) {
                currentY += rowHeights[j];
            }
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

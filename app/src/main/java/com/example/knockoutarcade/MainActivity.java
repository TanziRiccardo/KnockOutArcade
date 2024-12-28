package com.example.knockoutarcade;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        // Mostra le dimensioni dello schermo per il debug
        //Toast.makeText(this, "Screen Width: " + screenWidth + ", Screen Height: " + screenHeight, Toast.LENGTH_SHORT).show();
// Inizializza MediaPlayer con il file audio
        mediaPlayer = MediaPlayer.create(this, R.raw.knockout_music);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
        // Inizializza la GameView con le dimensioni dello schermo
        gameView = new GameView(this, screenWidth, screenHeight, this);
        setContentView(gameView);  // Imposta la GameView come contenuto dell'attività

    }
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
        // Avvia o riprendi la riproduzione quando l'app torna in primo piano
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();

        // Controlla che il MediaPlayer sia valido prima di chiamare isPlaying()
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (IllegalStateException e) {
                // Gestisci il caso in cui il MediaPlayer sia in uno stato non valido
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}


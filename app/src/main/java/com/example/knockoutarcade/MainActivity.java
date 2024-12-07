package com.example.knockoutarcade;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        // Mostra le dimensioni dello schermo per il debug
        Toast.makeText(this, "Screen Width: " + screenWidth + ", Screen Height: " + screenHeight, Toast.LENGTH_SHORT).show();

        // Inizializza la GameView con le dimensioni dello schermo
        gameView = new GameView(this, screenWidth, screenHeight);
        setContentView(gameView);  // Imposta la GameView come contenuto dell'attività
    }
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}


package com.example.knockoutarcade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import com.example.knockoutarcade.MainActivity;
import com.example.knockoutarcade.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameOverActivity extends Activity {
    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_SCORES = "scores";
    private Button restartButton;
    private ListView scoreListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_activity);

        restartButton = findViewById(R.id.restart_button);
        scoreListView = findViewById(R.id.scoreListView);

        // Recupera i punteggi
        List<Integer> scores = loadScores(this);

        // Ordina i punteggi in ordine decrescente (dal più alto al più basso)
        Collections.sort(scores, Collections.reverseOrder());

        // Limita a un massimo di 6 elementi
        if (scores.size() > 6) {
            scores = scores.subList(0, 6);
        }

        // Adattatore personalizzato
        ScoreAdapter adapter = new ScoreAdapter(this, scores);
        scoreListView.setAdapter(adapter);

        // Imposta il listener per il bottone di riavvio
        restartButton.setOnClickListener(v -> restartGame());
    }

    private List<Integer> loadScores(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("scores", null);

        if (json != null) {
            Type type = new TypeToken<List<Integer>>() {}.getType();
            return new Gson().fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }


    private void restartGame() {
        Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

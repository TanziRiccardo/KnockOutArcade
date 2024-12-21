package com.example.knockoutarcade;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class SplashActivity extends AppCompatActivity {

    private ImageView splashlogoFirst; // Prima immagine
    private ImageView splashlogo;     // Seconda immagine
    private ImageView botImageView1, botImageView2, botImageView3, splashID;  // Sprite dei bot
    private TextView characters;
    // Array delle TextView dei nomi dei bot
    private TextView[] botNameTextViews;
    private final int[] botNameTextViewIds = {
            R.id.bot_name_text_view_1,
            R.id.bot_name_text_view_2,
            R.id.bot_name_text_view_3
    };

    // Riferimenti agli ID dei tuoi spritesheet
    private int[] botSpritesheetIds = {
            R.drawable.luigi_bot,
            R.drawable.toad_bot,
            R.drawable.fire_bot
    };

    private Bitmap[] spritesheetBitmaps = new Bitmap[3];  // Per caricare tutti gli spritesheet
    private int currentBotIndex = 0;

    private final int[] spritesheetRows = {2, 2, 3};  // Numero di righe per ciascuno spritesheet
    private final int[] spritesheetCols = {2, 2, 2};  // Numero di colonne per ciascuno spritesheet
    private int spriteWidth, spriteHeight;
    private int[] currentFrames = new int[botSpritesheetIds.length];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        characters = findViewById(R.id.characters);
        splashID = findViewById(R.id.splashID);
        // Trova le immagini nel layout
        splashlogoFirst = findViewById(R.id.splash_logo_first);
        splashlogo = findViewById(R.id.splash_logo);
        botImageView1 = findViewById(R.id.bot_image_view_1);
        botImageView2 = findViewById(R.id.bot_image_view_2);
        botImageView3 = findViewById(R.id.bot_image_view_3);

        // Trova tutte le TextView dei nomi dei bot
        botNameTextViews = new TextView[botNameTextViewIds.length];
        for (int i = 0; i < botNameTextViewIds.length; i++) {
            botNameTextViews[i] = findViewById(botNameTextViewIds[i]);
            botNameTextViews[i].setVisibility(View.GONE); // Nascondi tutte le TextView inizialmente
        }

        // Fase 1: Mostra la prima immagine
        splashlogoFirst.setVisibility(View.VISIBLE);
        splashlogo.setVisibility(View.GONE);
        botImageView1.setVisibility(View.GONE);
        botImageView2.setVisibility(View.GONE);
        botImageView3.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            // Fase 2: Mostra la seconda immagine
            splashlogoFirst.setVisibility(View.GONE);
            splashlogo.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                // Fase 3: Carica tutti gli spritesheet e mostra gli sprite
                splashlogo.setVisibility(View.GONE);
                characters.setVisibility(View.VISIBLE);
                splashID.setVisibility(View.VISIBLE);
                botImageView1.setVisibility(View.GONE);
                botImageView2.setVisibility(View.GONE);
                botImageView3.setVisibility(View.GONE);

                // Carica gli spritesheet
                loadAllSpritesheets();

                // Inizia la sequenza delle animazioni
                playAnimationSequence();

            }, 2000); // Durata della seconda immagine (2 secondi)

        }, 2000); // Durata della prima immagine (2 secondi)
    }

    private void loadAllSpritesheets() {
        // Carica tutti gli spritesheet in memoria
        for (int i = 0; i < botSpritesheetIds.length; i++) {
            spritesheetBitmaps[i] = BitmapFactory.decodeResource(getResources(), botSpritesheetIds[i]);
        }
    }

    private void playAnimationSequence() {
        // Mostra tutti i bot e inizia le loro animazioni
        Handler handler = new Handler();
        for (int i = 0; i < botNameTextViews.length; i++) {
            final int index = i; // Capture the current index for the animation
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Mostra la TextView del nome
                    TextView currentBotTextView = botNameTextViews[index];
                    currentBotTextView.setVisibility(View.VISIBLE);


                    // Recupera il nome del bot dal file strings.xml
                    String botName = getString(getResources().getIdentifier("bot_name_" + (index + 1), "string", getPackageName()));
                    // Mostra l'immagine del bot e avvia l'animazione
                    ImageView currentBotImageView = findViewById(getResources().getIdentifier(
                            "bot_image_view_" + (index + 1), "id", getPackageName()));
                    currentBotImageView.setVisibility(View.VISIBLE);

                    // Animate text letter by letter
                    animateTextLetterByLetter(currentBotTextView, botName);
                }
            }, i * 1500); // Delay between animations (1500ms = 1.5 seconds per bot)
        }

        // Programma il passaggio alla MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, botNameTextViews.length * 1500 + 1000); // Adjust the total delay before transitioning to MainActivity
    }


    private void animateTextLetterByLetter(final TextView textView, final String text) {
        final Handler handler = new Handler();
        final int delay = 200; // Tempo di attesa tra ogni lettera (200 ms)
        final int length = text.length();
        final StringBuilder currentText = new StringBuilder();

        Runnable runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i < length) {
                    currentText.append(text.charAt(i));
                    textView.setText(currentText.toString());

                    // Usa un contesto valido, come textView.getContext()
                    Typeface creepsterFont = ResourcesCompat.getFont(textView.getContext(), R.font.creepsterregular);
                    textView.setTypeface(creepsterFont);


                    i++;
                    handler.postDelayed(this, delay);
                }
            }
        };

        handler.post(runnable);
    }



}

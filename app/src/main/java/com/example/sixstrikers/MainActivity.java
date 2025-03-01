package com.example.sixstrikers;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView backgroundImage;
    private VideoView bowlingVideo;
    private VideoView battingVideo;
    private ImageView runBadge;
    private Handler handler;
    private ProgressBar progressBar;
    private TextView timerText;
    private TextView scoreText;
    private TextView oversText;
    private TextView scoreTarget;

    private int score = 0;
    private int wickets = 0;
    private float overs = 0.0f;

    private int target = 0;
    private int botScore = 0;
    private int botWickets = 0;

    private boolean isBotInning = false;
    private boolean buttonClicked = false;
    private boolean videosCompleted = true;

    private int chances = 3;

    private CountDownTimer countDownTimer;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make the activity full-screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize the views
        backgroundImage = findViewById(R.id.backgroundImage);
        bowlingVideo = findViewById(R.id.bowlingVideo);
        battingVideo = findViewById(R.id.battingVideo);
        runBadge = findViewById(R.id.runBadge);
        progressBar = findViewById(R.id.progressBar);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        oversText = findViewById(R.id.oversText);
        scoreTarget = findViewById(R.id.scoreTarget);

        handler = new Handler();

        // Set up click listeners for the buttons
        Button[] buttons = new Button[6];
        for (int i = 0; i < buttons.length; i++) {
            int resId = getResources().getIdentifier("button" + (i + 1), "id", getPackageName());
            buttons[i] = findViewById(resId);
            buttons[i].setOnClickListener(buttonClickListener);
        }

        // Start the timer
        startTimer();
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!videosCompleted || buttonClicked) return;

            buttonClicked = true;
            stopTimer(); // Stop the timer when a button is clicked

            Button button = (Button) v;
            String userChoice = button.getText().toString();
            processChoice(userChoice);
        }
    };

    private void processChoice(String choice) {
        String botChoice = String.valueOf(random.nextInt(6) + 1); // Bot chooses a number between 1 and 6
        if (choice.equals(botChoice)) {
            chances--;
            if (isBotInning) {
                botWickets++;
                if (chances == 0) {
                    endGame();
                }
            } else {
                wickets++;
                if (chances == 0) {
                    // User's inning ends
                    target = score + 1; // Set target for the bot, +1 to make it more challenging
                    resetForBotInning();
                }
            }
            startVideoSequence("OUT"); // You can create a video for OUT or handle it differently
        } else {
            int run = Integer.parseInt(choice);
            if (isBotInning) {
                botScore += run;
                if (botScore > target) {
                    endGame();
                }
            } else {
                score += run;
            }
            startVideoSequence(choice);
        }
    }

    private void resetForBotInning() {
        isBotInning = true;
        botScore = 0;
        botWickets = 0;
        overs = 0.0f;
        chances = 3;

        scoreTarget.setText("Target: " + target); // Display the target for the bot
        scoreText.setText("Player 2: " + botScore + "-" + botWickets);
        oversText.setText("Overs: " + overs);

        startTimer(); // Restart the timer for bot's inning
    }

    private void endGame() {
        if (botScore >= target) {
            showRunBadge("Player 2 Wins!");
        } else if (chances == 0) {
            showRunBadge("Player 1 Win!");
        }
        stopTimer(); // Stop the timer when the game ends
    }

    private void startVideoSequence(final String run) {
        videosCompleted = false;
        disableButtons(); // Disable buttons during video playback
        backgroundImage.setVisibility(View.GONE);
        bowlingVideo.setVisibility(View.VISIBLE);
        battingVideo.setVisibility(View.GONE);
        runBadge.setVisibility(View.GONE);

        Uri bowlingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bowling);
        bowlingVideo.setVideoURI(bowlingUri);
        bowlingVideo.start();

        bowlingVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playBattingVideo(run);
            }
        });
    }

    private void playBattingVideo(final String run) {
        bowlingVideo.setVisibility(View.GONE);
        battingVideo.setVisibility(View.VISIBLE);
        runBadge.setVisibility(View.GONE);

        // Determine batting video based on run
        Uri battingUri;
        if ("OUT".equals(run)) {
            battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.out); // Assume an OUT video
        } else {
            battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting01); // Default to batting01
            switch (run) {
                case "1":
                    battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting01);
                    break;
                case "2":
                    battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting02);
                    break;
                case "3":
                    battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting03);
                    break;
                case "4":
                    battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting04);
                    break;
                case "5":
                    battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting05);
                    break;
                case "6":
                    battingUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.batting06);
                    break;
            }
        }

        battingVideo.setVideoURI(battingUri);
        battingVideo.start();

        battingVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                showRunBadge(run);
            }
        });
    }

    private void showRunBadge(final String run) {
        battingVideo.setVisibility(View.GONE);
        runBadge.setVisibility(View.VISIBLE);
        backgroundImage.setVisibility(View.VISIBLE);

        // Determine badge drawable based on run
        int runBadgeResId;
        if ("OUT".equals(run)) {
            runBadgeResId = R.drawable.out_badge; // Assume a drawable for OUT
        } else if ("Bot Wins!".equals(run) || "You Win!".equals(run)) {
            runBadgeResId = R.drawable.trophy_badge; // Assume a trophy badge
        } else {
            runBadgeResId = getResources().getIdentifier("run_" + run, "drawable", getPackageName());
        }
        runBadge.setImageResource(runBadgeResId);

        // Update score and overs
        if (!"OUT".equals(run)) {
            overs += 0.1;
            if (overs % 1.0 >= 0.6) {
                overs = (int) overs + 1.0f; // Roll over to next over
            }
        }
        updateScorecard();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runBadge.setVisibility(View.GONE);
                videosCompleted = true; // All videos completed
                enableButtons(); // Re-enable buttons after videos are done
                startTimer(); // Restart the timer
            }
        }, 2000); // Delay for badge display
    }

    private void updateScorecard() {
        if (isBotInning) {
            scoreText.setText("Player 2: " + botScore + "-" + botWickets);
        } else {
            scoreText.setText("Player 1: " + score + "-" + wickets);
        }
        oversText.setText(String.format("Overs: %.1f", overs));
    }

    private void startTimer() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(100);
        buttonClicked = false; // Reset button click status

        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                progressBar.setProgress(secondsRemaining * 10); // Set progress bar
                timerText.setText("" + secondsRemaining);
            }

            public void onFinish() {
                if (!buttonClicked && chances > 0 && !videosCompleted) {
                    processChoice(String.valueOf(random.nextInt(6) + 1)); // Random choice if user didn't click a button
                }
                enableButtons(); // Re-enable buttons after timer finishes
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void disableButtons() {
        Button[] buttons = new Button[6];
        for (int i = 0; i < buttons.length; i++) {
            int resId = getResources().getIdentifier("button" + (i + 1), "id", getPackageName());
            buttons[i] = findViewById(resId);
            buttons[i].setEnabled(false);
        }
    }

    private void enableButtons() {
        Button[] buttons = new Button[6];
        for (int i = 0; i < buttons.length; i++) {
            int resId = getResources().getIdentifier("button" + (i + 1), "id", getPackageName());
            buttons[i] = findViewById(resId);
            buttons[i].setEnabled(true);
        }
    }
}

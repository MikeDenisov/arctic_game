package com.goodcodeforfun.iceplorers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

public class PlayServicesGamesDemoActivity extends AppCompatActivity implements PlayServices {

    private final static int requestCode = 1;
    private GameHelper gameHelper;

    @Override
    public void signIn() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gameHelper.beginUserInitiatedSignIn();
                }
            });
        } catch (Exception e) {
            Log.e("DemoActivity", "Log in failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void signOut() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gameHelper.signOut();
                }
            });
        } catch (Exception e) {
            Log.e("DemoActivity", "Log out failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void rateGame() {
        String str = "Your PlayStore Link";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
    }

    @Override
    public void unlockAchievement() {
        Games.Achievements.unlock(gameHelper.getApiClient(),
                getString(R.string.achievement_dum_dum));
    }

    @Override
    public void submitScore(int highScore) {
        if (isSignedIn() == true) {
            Games.Leaderboards.submitScore(gameHelper.getApiClient(),
                    getString(R.string.leaderboard_highest), highScore);
        }
    }

    @Override
    public void showAchievement() {
        if (isSignedIn() == true) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(gameHelper.getApiClient()), requestCode);
        } else {
            signIn();
        }
    }

    @Override
    public void showScore() {
        if (isSignedIn() == true) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(),
                    getString(R.string.leaderboard_highest)), requestCode);
        } else {
            signIn();
        }
    }

    @Override
    public boolean isSignedIn() {
        return gameHelper.isSignedIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_services_games_demo);
        gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
        gameHelper.enableDebugLog(false);

        GameHelper.GameHelperListener gameHelperListener = new GameHelper.GameHelperListener() {
            @Override
            public void onSignInFailed() {
            }

            @Override
            public void onSignInSucceeded() {
            }
        };

        gameHelper.setup(gameHelperListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gameHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameHelper.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        gameHelper.onActivityResult(requestCode, resultCode, data);
    }

}

package com.goodcodeforfun.iceplorers;

/**
 * Created by snigavig on 29.04.17.
 */


public interface PlayServices {
    public void signIn();

    public void signOut();

    public void rateGame();

    public void unlockAchievement();

    public void submitScore(int highScore);

    public void showAchievement();

    public void showScore();

    public boolean isSignedIn();
}
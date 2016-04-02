package com.example.uru.myfirstgame;

import android.graphics.Bitmap;

public class Animation {
    private Bitmap[] frames;
    private int currFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(Bitmap[] frames){
      this.frames=frames;
        currFrame=0;
        startTime=System.nanoTime();
    }

    public void setDelay(long d){
        delay=d;
    }

    public void setFrame(int i){
        currFrame=i;
    }
    public void update(){
        long elapsed=(System.nanoTime()-startTime)/1000000;
        if(elapsed>delay){
            currFrame++;
            startTime=System.nanoTime();
        }
        if(currFrame==frames.length){
            currFrame=0;
            playedOnce=true;
        }
    }

    public Bitmap getImage(){
        return frames[currFrame];
    }

    public int getFrame(){
        return currFrame;
    }

    public boolean PlayedOnce(){
        return playedOnce;
    }
}

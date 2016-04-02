package com.example.uru.myfirstgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Background {
    private Bitmap image;
    private int x,y,dx;

    public Background(Bitmap res)
    {
        image=res;
        dx=GamePanel.moveSpeed;
    }

    public void update(){
        x+=dx;
        if(x<0)
            x=0;

    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y, null);
        if(x<=GamePanel.width){
            canvas.drawBitmap(image, x+GamePanel.width, y, null);
        }
    }

}

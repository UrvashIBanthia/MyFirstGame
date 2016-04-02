package com.example.uru.myfirstgame;


import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BottomBorder extends GameObject{
    private Bitmap image;

    public BottomBorder(Bitmap res, int x, int y){
        height=200;
        width=20;
        this.x=x;
        this.y=y;
        dx=GamePanel.moveSpeed;
        image=Bitmap.createBitmap(res, 0, 0, width,height);
    }

    public void update(){
        x+=dx;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, 0,0, null);
    }



}

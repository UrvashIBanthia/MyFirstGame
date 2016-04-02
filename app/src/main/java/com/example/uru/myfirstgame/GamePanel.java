package com.example.uru.myfirstgame;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.test.suitebuilder.annotation.Smoke;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

import static android.R.color.black;
import static android.R.color.darker_gray;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int width=856;
    public static final int height=480;
    public static final int moveSpeed=-5;
    private long smokeStartTimer;
    private long missileStartTime;
    private long missileElapsed;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BottomBorder> botborder;
    private Random random=new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown=true;
    private boolean botDown=true;
    private boolean newGameCreated;

    //increase to slow down difficulty and decrease to increase difficulty
    private int progressDemo=20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int best;


    public GamePanel(Context context){
        super(context);
        getHolder().addCallback(this);

        setFocusable(true);
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry =true;
        int counter=0;
        while(retry && counter<1000){
            counter++;
            try{
                thread.setRunning(false);
                thread.join();
                retry=false;
                thread=null;
            }
            catch(InterruptedException e){
                e.printStackTrace();}
        }

    }

    public void surfaceCreated(SurfaceHolder holder){

        bg=new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player=new Player(BitmapFactory.decodeResource(getResources(),R.drawable.helicopter),65,25,3);
        smoke=new ArrayList<SmokePuff>();
        missiles=new ArrayList<Missile>();
        topborder=new ArrayList<TopBorder>();
        botborder=new ArrayList<BottomBorder>();
        smokeStartTimer=System.nanoTime();
        missileStartTime=System.nanoTime();
        thread=new MainThread(getHolder(),this);
         thread.setRunning(true);
        thread.start();
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(!player.getPlaying())
            {
                if(!started)
                    started=true;
                reset=false;
                player.setUp(true);
            }
            return true;
        }

        if(event.getAction()==MotionEvent.ACTION_UP) {
        player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {
            if (botborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            maxBorderHeight = 30 + player.getScore() / progressDemo;
            if (maxBorderHeight > height / 4)
                maxBorderHeight = height / 4;

            minBorderHeight = 5 + player.getScore() / progressDemo;

            for (int i = 0; i < topborder.size(); i++) {
                if (collisions(topborder.get(i), player))
                    player.setPlaying(false);
            }

            for (int i = 0; i < botborder.size(); i++) {
                if (collisions(botborder.get(i), player))
                    player.setPlaying(false);

            }

            this.updateBotBorder();

            this.updateTopBorder();

            missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;

            if (missileElapsed > (2000 - player.getScore() / 4)) {
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), width + 10, height / 2, 45, 15, player.getScore(), 13));

                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), width + 10,
                            (int) ((random.nextDouble() * (height - (maxBorderHeight * 2)) + maxBorderHeight)), 45, 15, player.getScore(), 13));
                }
                missileStartTime = System.nanoTime(); //reset timer
            }

            for (int i = 0; i < missiles.size(); i++) {
                missiles.get(i).update();
                if (collisions(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }

            long elapsed = (System.nanoTime() - smokeStartTimer) / 1000000;
            if (elapsed > 120)
                smoke.add(new SmokePuff(player.getX(), player.getY() + 10));
            smokeStartTimer = System.nanoTime();

            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10) {
                    smoke.remove(i);
                }
            }
        }
        else{
            player.resetDY();
            if(!reset) {
                newGameCreated = false;
                startReset=System.nanoTime();
                reset=true;
                disappear=true;
                explosion=new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion), player.getX(), player.getY()-30,100,100,25);

            }

            explosion.update();
            long resetElapsed=(System.nanoTime()-startReset)/1000000;

            if(resetElapsed>2500 && !newGameCreated){
                newGame();
            }
        }
    }
    public void draw(Canvas canvas) {
        final float ScaleFactorX=getWidth()/(width*1.f);
        final float ScaleFactorY=getHeight()/(height*1.f);
        if (canvas != null) {
            final int savedState=canvas.save();
            canvas.scale(ScaleFactorX,ScaleFactorY);
            bg.draw(canvas);
            if(!disappear){
            player.draw(canvas);}
            for(SmokePuff sp:smoke){
                sp.draw(canvas);
            }

            for(Missile m:missiles) {
                m.draw(canvas);
            }

           for(TopBorder tb: topborder)
               tb.draw(canvas);

            for(BottomBorder bb: botborder)
                bb.draw(canvas);

            if(started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }

    }

    public boolean collisions(GameObject missile, GameObject player){

        if(Rect.intersects(missile.getRectangle(), player.getRectangle())) {
            return true;
        }
        return false;
    }

    public void updateTopBorder(){
//every 50 points, insert randomly placed top blocks that break the pattern
        if(player.getScore()%50==0)
        {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick
            ),topborder.get(topborder.size()-1).getX()+20,0,(int)((random.nextDouble()*(maxBorderHeight
            ))+1)));
        }

        for (int i=0;i<topborder.size();i++){
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20){
                topborder.remove(i);
                //remove element of arraylist, add a new one instead

                //calculate topdown which determines the direction the border is moving up or down
                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight){
                    topDown=false;
                }

                if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight){
                    topDown=true;
                }

                //new border added wil have larger height
                if(topDown){

                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,0, topborder.get(topborder.size()-1).getHeight()+1));
                }
                //new border added will have lesser height
                else{
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,0, topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBotBorder(){

        if(player.getScore()%40==0){
            botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size()-1).getX()+20,(int)((random.nextDouble()
                    *maxBorderHeight)+(height-maxBorderHeight))));
        }

        for(int i=0;i<botborder.size();i++){
            botborder.get(i).update();

            //if border is moving off screen remove it, and add a new one

            if(botborder.get(i).getX()<-20) {
                botborder.remove(i);

// determine whether border will move up or down
                if (botborder.get(botborder.size() - 1).getHeight() >= maxBorderHeight) {
                    botDown = false;
                }

                if (botborder.get(botborder.size() - 1).getHeight() <= minBorderHeight) {
                    botDown = true;
                }

                if (botDown) {
                    botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1).getY() + 1));
                } else {
                    botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1).getY() - 1));
                }
            }

        }
    }

    public void newGame(){
        disappear=false;
        botborder.clear();
        topborder.clear();
        missiles.clear();
        smoke.clear();

        minBorderHeight=5;
        maxBorderHeight=30;
        player.resetDY();
        player.resetScore();
        player.setY(height/2);

        if(player.getScore()>best){
            best= player.getScore();
        }

        for(int i=0;i*20<width+40;i++){
            if(i==0){
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,0,10));
            }
            else
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,0,topborder.get(i-1).getHeight()+1));
        }

        for(int i=0;i*20<width+40;i++){
            if(i==0){
                botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,height-minBorderHeight));
            }
            else
                botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,botborder.get(i-1).getY()-1));
        }

        newGameCreated=true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(black);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("DISTANCE: "+ (player.getScore()*3), 10,height-10,paint);
        canvas.drawText("BEST: "+ best,width-215, height-10,paint);

        if(!player.getPlaying() && newGameCreated && reset){
            Paint paint1=new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START ", width/2-50, height/2,paint);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", width/2-50, height/2+20,paint1);
            canvas.drawText("RELEASE TO GO DOWN", width/2-50, height/2+40,paint1);


        }


    }




}

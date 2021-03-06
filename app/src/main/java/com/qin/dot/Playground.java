package com.qin.dot;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by qin on 2017-03-13.
 */

public class Playground extends SurfaceView implements View.OnTouchListener {
    private static  int WIDTH = 40;
    private static final int ROW = 10;
    private static final int COL = 10;
    private static final int BLOCKS = 15;//默认添加的路障数量

    private int num=0;

    private Dot matrix[][];
    private Dot cat;

    private String newGame="New Game";

    private SharedPreferences sharedPreferences;

    public Playground(Context context) {
        super(context);
        getHolder().addCallback(callback);
        matrix = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }
        setOnTouchListener(this);
        initGame();
    }

    private Dot getDot(int x,int y) {
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d) {
        if (d.getX()*d.getY() == 0 || d.getX()+1 == COL || d.getY()+1 == ROW) {
            return true;
        }
        return false;
    }

    private Dot getNeighbour(Dot one,int dir) {
        switch (dir) {
            case 1:
                return getDot(one.getX()-1, one.getY());
            case 2:
                if (one.getY()%2 == 0) {
                    return getDot(one.getX()-1, one.getY()-1);
                }else {
                    return getDot(one.getX(), one.getY()-1);
                }
            case 3:
                if (one.getY()%2 == 0) {
                    return getDot(one.getX(), one.getY()-1);
                }else {
                    return getDot(one.getX()+1, one.getY()-1);
                }
            case 4:
                return getDot(one.getX()+1, one.getY());
            case 5:
                if (one.getY()%2 == 0) {
                    return getDot(one.getX(), one.getY()+1);
                }else {
                    return getDot(one.getX()+1, one.getY()+1);
                }
            case 6:
                if (one.getY()%2 == 0) {
                    return getDot(one.getX()-1, one.getY()+1);
                }else {
                    return getDot(one.getX(), one.getY()+1);
                }

            default:
                break;
        }
        return null;
    }

    private int getDistance(Dot one,int dir) {
//		System.out.println("X:"+one.getX()+" Y:"+one.getY()+" Dir:"+dir);
        int distance = 0;
        if (isAtEdge(one)) {
            return 1;
        }
        Dot ori = one,next;
        while(true){
            next = getNeighbour(ori, dir);
            if (next.getStatus() == Dot.STATUS_ON) {
                return distance*-1;
            }
            if (isAtEdge(next)) {
                distance++;
                return distance;
            }
            distance++;
            ori = next;
        }
    }

    private void MoveTo(Dot one) {
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);;
        cat.setXY(one.getX(), one.getY());
    }

    private void move() {
        if (isAtEdge(cat)) {
            lose();return;
        }
        Vector<Dot> avaliable = new Vector<>();
        Vector<Dot> positive = new Vector<>();
        HashMap<Dot, Integer> al = new HashMap<Dot, Integer>();
        for (int i = 1; i < 7; i++) {
            Dot n = getNeighbour(cat, i);
            if (n.getStatus() == Dot.STATUS_OFF) {
                avaliable.add(n);
                al.put(n, i);
                if (getDistance(n, i) > 0) {
                    positive.add(n);

                }
            }
        }
        if (avaliable.size() == 0) {
            win();
        }else if (avaliable.size() == 1) {
            MoveTo(avaliable.get(0));
        }else{
            Dot best = null;
            if (positive.size() != 0 ) {//存在可以直接到达屏幕边缘的走向
                System.out.println("向前进");
                int min = 999;
                for (int i = 0; i < positive.size(); i++) {
                    int a = getDistance(positive.get(i), al.get(positive.get(i)));
                    if (a < min) {
                        min = a;
                        best = positive.get(i);
                    }
                }
                MoveTo(best);
            }else {//所有方向都存在路障
                System.out.println("躲路障");
                int max = 0;
                for (int i = 0; i < avaliable.size(); i++) {
                    int k = getDistance(avaliable.get(i), al.get(avaliable.get(i)));
                    if (k <= max) {
                        max = k;
                        best = avaliable.get(i);
                    }
                }
                MoveTo(best);
            }
        }
    }

    private void redraw() {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROW; i++) {
            int offset = 0;
            if (i%2 != 0) {
                offset = WIDTH/2;
            }
            for (int j = 0; j < COL; j++) {
                Dot one = getDot(j, i);
                switch (one.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;

                    default:
                        break;
                }
                c.drawOval(new RectF(one.getX()*WIDTH+offset, one.getY()*WIDTH,
                        (one.getX()+1)*WIDTH+offset, (one.getY()+1)*WIDTH), paint);

            }

        }
        paint.setColor(0xFFFF0000);
        paint.setTextSize(40);
//        c.drawText(newGame,WIDTH,WIDTH*11,paint);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.refresh);
        c.drawBitmap(bmp, WIDTH*5, WIDTH*11, null);

        getHolder().unlockCanvasAndPost(c);
    }

    Callback callback = new Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            // TODO Auto-generated method stub
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            WIDTH = arg2/(COL+1);
            redraw();
        }
    };

    private void initGame() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);
        for (int i = 0; i < BLOCKS;) {
            int x = (int) ((Math.random()*1000)%COL);
            int y = (int) ((Math.random()*1000)%ROW);
            if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
                //System.out.println("Block:"+i);
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//			Toast.makeText(getContext(), e.getX()+":"+e.getY(), Toast.LENGTH_SHORT).show();
            int x,y;
            y = (int) (motionEvent.getY()/WIDTH);
            if (y%2 == 0) {
                x = (int) (motionEvent.getX()/WIDTH);
            }else {
                x = (int) ((motionEvent.getX()-WIDTH/2)/WIDTH);
            }
            if (x+1 > COL || y+1 > ROW) {
                System.out.println(x);
                System.out.println(y);
                if((x>=4)&&(x<=6)&&(y>=10)&&(y<=12)){
                    initGame();
                }
            } else if(getDot(x, y).getStatus() == Dot.STATUS_OFF){
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
                num++;
            }
            redraw();
        }
        return true;
    }

    public void lose() {
        Toast.makeText(getContext(), "You Lose：总共使用了"+num+"步", Toast.LENGTH_SHORT).show();
        initGame();
        Toast.makeText(getContext(), "胜败乃兵家常事，大侠请重新来过！", Toast.LENGTH_SHORT).show();
        num=0;
    }

    public void win() {
        Toast.makeText(getContext(), "You Win：只使用了"+num+"步", Toast.LENGTH_SHORT).show();

        sharedPreferences=getContext().getSharedPreferences("gamedata",MODE_PRIVATE);

        int lastNum=sharedPreferences.getInt("score",100);
        if (num<lastNum)
        {
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putInt("score",num);
            editor.commit();

            Toast.makeText(getContext(), "恭喜你，成功刷新了最高纪录", Toast.LENGTH_SHORT).show();
        }
        initGame();
        num=0;
    }



}

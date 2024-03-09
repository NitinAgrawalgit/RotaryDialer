package com.example.rotary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.List;

public class RotaryDialerView extends View {

    public interface DialListener {
        void onDial(int number);
    }

    private final List<DialListener> dialListeners = new ArrayList<DialListener>();
    private final Drawable rotorDrawable;
    private float rotorAngle;
    private final int r1 = 150;
    private final int r2 = 400;
    private double lastFi;
    private double maxRotorAngle;

    public RotaryDialerView(Context context) {
        this(context, null);
    }

    public RotaryDialerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotaryDialerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rotorDrawable = AppCompatResources.getDrawable(context, R.drawable.dialer_large);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        canvas.save();

        rotorDrawable.setBounds(0, 0, rotorDrawable.getIntrinsicWidth(), rotorDrawable.getIntrinsicHeight());
        //rotorDrawable.draw(canvas);
        if (rotorAngle != 0) {
            canvas.rotate(rotorAngle, x, y);
        }
        rotorDrawable.draw(canvas);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x0 = getWidth() / 2;
        final float y0 = getHeight() / 2;
        float x1 = event.getX();
        float y1 = event.getY();
        float x = x0 - x1;
        float y = y0 - y1;
        double r = Math.sqrt(x * x + y * y);
        double sinfi = y / r;
        double fi = Math.toDegrees(Math.asin(sinfi));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (r > r1 && rotorAngle <= maxRotorAngle+15) { // && r < r2 /** Removed this from the if condition */
                    rotorAngle += Math.abs(fi - lastFi) + 0.25f;
                    rotorAngle %= 360;
                    lastFi = fi;
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                rotorAngle = 0;
                lastFi = fi;

                setMaxRotorAngle(fi, x1, y1, x0, y0);

                return true;

            case MotionEvent.ACTION_UP:
                final float angle = rotorAngle % 360;
                int number = Math.round(angle - 20) / 30;

                if (number > 0) {
                    if (number == 10) {
                        number = 0;
                    }

                    fireDialListenerEvent(number);
                }

                rotorAngle = 0;
                maxRotorAngle = 0;

                rotateBackAnimation(angle);

                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    /** DialListener Interface related Functions */
    public void addDialListener(DialListener listener) {
        dialListeners.add(listener);
    }

    public void removeDialListener(DialListener listener) {
        dialListeners.remove(listener);
    }

    private void fireDialListenerEvent(int number) {
        for (DialListener listener : dialListeners) {
            listener.onDial(number);
        }
    }

    private void setMaxRotorAngle(double fi, float x1, float y1, float x0, float y0){
        if(x1 > x0 && y0 > y1){
            maxRotorAngle = fi;        //First Quadrant
        }else if(x0 > x1 && y0 > y1){
            maxRotorAngle = 180 - fi;  //Second Quadrant
        }else if(x0 > x1 && y1 > y0){
            maxRotorAngle = 180 - fi;  //Third Quadrant
        }else {
            maxRotorAngle = 360 + fi;  //Fourth Quadrant
        }
    }

    private void rotateBackAnimation(float angle){
        post(new Runnable() {
            public void run() {
                float fromDegrees = angle;
                Animation anim = new RotateAnimation(fromDegrees, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R.anim.decelerate_interpolator));
                //anim.setDuration((long) (angle * 5L));

                if(angle < 150){
                    anim.setDuration(500);
                }else if(angle < 270){
                    anim.setDuration(700);
                }else {
                    anim.setDuration(1000);
                }

                startAnimation(anim);
            }
        });
    }
}

package com.notnotme.sketchup.view;

import android.view.MotionEvent;
import android.view.View;

public abstract class RatioTouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();

                if (x > view.getRight()) {
                    x = view.getRight();
                } else if (x < view.getLeft()) {
                    x = view.getLeft();
                }

                if (y < view.getTop()) {
                    y = view.getTop();
                } else if (y > view.getBottom()) {
                    y = view.getBottom();
                }

                float ratioX = 1f / (view.getWidth() / x);
                float ratioY = 1f / (view.getHeight() / y);

                return onTouch(view, x, y, Math.min(Math.max(ratioX, 0f), 1f), Math.min(Math.max(ratioY, 0f), 1f));
            default:
                return false;
        }
    }

    public abstract boolean onTouch(View v, float x, float y, float ratioX, float ratioY);

}

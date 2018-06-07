package com.notnotme.sketchup.view.drawing;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.notnotme.sketchup.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public final class DrawingView extends View {

    public static final int STROKE_DEFAULT_SIZE = 15;
    private static final String TAG = DrawingView.class.getSimpleName();

    private static final String SAVESTATE_SKETCH_FILE = TAG + ".state_sketch";
    private static final String STATE_STROKE_WIDTH = TAG + ".state_stroke_width";
    private static final String STATE_COLOR = TAG + ".state_color";
    private static final String STATE_DRAW_MODE = TAG + ".state_draw_mode";
    private static final String STATE_DRAW_EFFECT = TAG + ".state_effect";
    private static final String STATE_BASE = TAG + ".state_base";

    private Stack<CanvasDrawable> mRedos;
    private Path mDrawPath;
    private Paint mDrawPaint;
    private Paint mCanvasPaint;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;
    private Bitmap mOriginalBitmap;
    private int mCurrentColor;
    private float mCurrentStrokeWidth;
    private Effect mCurrentEffect;
    private DrawMode mDrawMode;

    // First touch (action down)
    private float mFirstTouchX;
    private float mFirstTouchY;

    // Old touch position (action move)
    private float mPrevTouchX;
    private float mPrevTouchY;


    public DrawingView(Context context) {
        super(context);
        setupDrawing();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupDrawing();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupDrawing();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCanvasBitmap == null) return;
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mDrawPath, mDrawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDrawCanvas == null) {
            performClick();
            return false;
        }

        float touchX = event.getX();
        float touchY = event.getY();
        int eventAction = event.getAction();

        if (eventAction == MotionEvent.ACTION_DOWN) {
            mFirstTouchX = touchX;
            mFirstTouchY = touchY;
        }

        switch (mDrawMode) {
            case FREE:
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        mDrawPath.moveTo(touchX, touchY);
                        mPrevTouchX = touchX;
                        mPrevTouchY = touchY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mDrawPath.quadTo(mPrevTouchX, mPrevTouchY, (touchX + mPrevTouchX)/2, (touchY + mPrevTouchY)/2);
                        mPrevTouchX = touchX;
                        mPrevTouchY = touchY;
                        break;
                    case MotionEvent.ACTION_UP:
                        mRedos.push(new PathDrawable(mDrawPaint.getColor(), mDrawPaint.getStrokeWidth(), mCurrentEffect.mPathEffect, mDrawPath));
                        mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
                        break;
                }
                break;

            case LINES:
                switch (eventAction) {
                    case MotionEvent.ACTION_MOVE:
                        mDrawPath.rewind();
                        mDrawPath.moveTo(mFirstTouchX, mFirstTouchY);
                        mDrawPath.lineTo(touchX, touchY);
                        break;
                    case MotionEvent.ACTION_UP:
                        mRedos.push(new PathDrawable(mDrawPaint.getColor(), mDrawPaint.getStrokeWidth(), mCurrentEffect.mPathEffect, mDrawPath));
                        mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
                        break;
                }
                break;
        }


        if (eventAction == MotionEvent.ACTION_UP) {
            mDrawPath = new Path();
        }

        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(STATE_BASE, super.onSaveInstanceState());
        state.putFloat(STATE_STROKE_WIDTH, mCurrentStrokeWidth);
        state.putInt(STATE_COLOR, mCurrentColor);
        state.putString(STATE_DRAW_MODE, mDrawMode.name());
        state.putString(STATE_DRAW_EFFECT, mCurrentEffect.name());

        // Save sketch to a temporary file
        try {
            File tempSketch = Utils.saveImageToAppStorage(getContext(), SAVESTATE_SKETCH_FILE, mCanvasBitmap);
            state.putString(SAVESTATE_SKETCH_FILE, tempSketch.getPath());
        } catch (IOException e) {
            Log.e(TAG, "Unable to save sketch while saving instance state: " + e.getMessage());
        }

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;
        super.onRestoreInstanceState(savedState.getParcelable(STATE_BASE));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        mOriginalBitmap = BitmapFactory.decodeFile(savedState.getString(SAVESTATE_SKETCH_FILE), options);

        setDrawMode(DrawMode.valueOf(savedState.getString(STATE_DRAW_MODE)));
        setStrokeWidth(savedState.getFloat(STATE_STROKE_WIDTH));
        setColor(savedState.getInt(STATE_COLOR));
        setEffect(Effect.valueOf(savedState.getString(STATE_DRAW_EFFECT)));
        setBitmap(mOriginalBitmap);
    }

    private void setupDrawing() {
        mRedos = new Stack<>();

        mDrawPath = new Path();
        mDrawPaint = new Paint();
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setAntiAlias(true);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);

        setDrawMode(DrawMode.FREE);
        setStrokeWidth(STROKE_DEFAULT_SIZE);
        setColor(Color.BLACK);
        setEffect(Effect.PLAIN);
    }

    public boolean canUndo() {
        return !mRedos.empty();
    }

    public void undo() {
        if (!canUndo()) return;

        mRedos.pop();
        mDrawCanvas.drawColor(Color.WHITE);
        if (mOriginalBitmap != null) {
            mDrawCanvas.drawBitmap(mOriginalBitmap, 0, 0, null);
        }

        int undoSize = mRedos.size();
        for (int i = 0; i < undoSize; i++) {
            mRedos.get(i).draw(mDrawCanvas, mDrawPaint);
        }

        mDrawPaint.setColor(mCurrentColor);
        mDrawPaint.setStrokeWidth(mCurrentStrokeWidth);
        mDrawPaint.setPathEffect(mCurrentEffect.mPathEffect);
        invalidate();
    }

    public float getStrokeWidth() {
        return mDrawPaint.getStrokeWidth();
    }

    public void setStrokeWidth(float strokeWidth) {
        mCurrentStrokeWidth = strokeWidth;
        mDrawPaint.setStrokeWidth(strokeWidth);
    }

    public int getColor() {
        return mDrawPaint.getColor();
    }

    public void setColor(int color) {
        mCurrentColor = color;
        mDrawPaint.setColor(color);
    }

    public Effect getEffect() {
        return mCurrentEffect;
    }

    public void setEffect(Effect effect) {
        mCurrentEffect = effect;
        mDrawPaint.setPathEffect(effect.mPathEffect);
    }

    public DrawMode getDrawMode() {
        return mDrawMode;
    }

    public void setDrawMode(DrawMode drawMode) {
        mDrawMode = drawMode;
    }

    public Bitmap getBitmap() {
        return mCanvasBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        // Eat up a little memory to ensure no null pointer exception if we got in instance state to quickly
        // and trying to save a null bitmap.
        mCanvasBitmap = Bitmap.createBitmap(1,1, Bitmap.Config.RGB_565);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mCanvasBitmap.recycle();

                int width = getMeasuredWidth();
                int height = getMeasuredHeight();
                if (width <= 0 || height <= 0) {
                    // At this point the view is not visible to the user so we can return
                    // and avoid a crash
                    return;
                }

                mCanvasBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.RGB_565);
                mDrawCanvas = new Canvas(mCanvasBitmap);

                if (bitmap != null) {
                    mOriginalBitmap = bitmap;
                    mDrawCanvas.drawColor(Color.WHITE);
                    mDrawCanvas.drawBitmap(mOriginalBitmap, 0, 0, null);
                } else {
                    mOriginalBitmap = null;
                    mDrawCanvas.drawColor(Color.WHITE);
                }
                invalidate();
            }
        });
        requestLayout();
    }

    public void resetHistory() {
        mRedos.clear();
    }

    public enum DrawMode {
        FREE,
        LINES
    }

}

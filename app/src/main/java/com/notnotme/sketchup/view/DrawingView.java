package com.notnotme.sketchup.view;

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

import com.notnotme.sketchup.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public final class DrawingView extends View {

    private static final String TAG = DrawingView.class.getSimpleName();

    private static final String SAVESTATE_SKETCH_FILE = TAG + ".state_sketch";
    private static final String STATE_STROKE_WIDTH = TAG + ".state_stroke_width";
    private static final String STATE_COLOR = TAG + ".state_color";
    private static final String STATE_BASE = TAG + ".state_base";

    public static final int STROKE_SMALL_SIZE = 5;
    public static final int STROKE_MEDIUM_SIZE = 15;
    public static final int STROKE_LARGE_SIZE = 30;

    // todo: save state my ass it is feasible but boring :D
    private Stack<DrawingElement> mRedos;

    private Path mDrawPath;
    private Paint mDrawPaint;
    private Paint mCanvasPaint;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;
    private Bitmap mOriginalBitmap;

    private int mCurrentColor;
    private float mCurrentStrokeWidth;


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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOriginalBitmap != null) {
            mCanvasBitmap = mOriginalBitmap.copy(Bitmap.Config.RGB_565, true);
            mDrawCanvas = new Canvas(mCanvasBitmap);
        } else {
            if (mCanvasBitmap != null) {
                mCanvasBitmap.eraseColor(Color.WHITE);
            } else {
                mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
                mCanvasBitmap.eraseColor(Color.WHITE);
                mDrawCanvas = new Canvas(mCanvasBitmap);
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCanvasBitmap == null) return;
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mDrawPath, mDrawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDrawCanvas == null) return false;

        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                mRedos.push(new DrawingElement(mDrawPath, mDrawPaint.getColor(), mDrawPaint.getStrokeWidth()));
                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
                mDrawPath = new Path();
                break;
            default:
                return false;
        }

        invalidate();
        performClick();
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

        // Save sketch to a temporary file
        try {
            File tempSketch = Utils.saveImageToExternalStorage(getContext(), SAVESTATE_SKETCH_FILE, mCanvasBitmap);
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
        setBitmap(BitmapFactory.decodeFile(savedState.getString(SAVESTATE_SKETCH_FILE), options));

        mCurrentStrokeWidth = savedState.getFloat(STATE_STROKE_WIDTH);
        mCurrentColor = savedState.getInt(STATE_COLOR);
        mDrawPaint.setStrokeWidth(mCurrentStrokeWidth);
        mDrawPaint.setColor(mCurrentColor);

        // todo: restore undo steps ETA: chrismas 2025+
        // todo: limit redo step to 10 ? yeah. 2025.
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

        setBrushStrokeWidth(STROKE_MEDIUM_SIZE);
        setBrushColor(Color.BLACK);
    }

    public void clearImage(int color) {
        if (mCanvasBitmap == null) return;

        mRedos.clear();
        mCanvasBitmap.eraseColor(color);

        invalidate();
    }

    public boolean canUndo() {
        return !mRedos.empty();
    }

    public void undo() {
        if (!canUndo()) return;

        mRedos.pop();
        if (mOriginalBitmap != null) {
            mCanvasBitmap = mOriginalBitmap.copy(Bitmap.Config.RGB_565, true);
            mDrawCanvas = new Canvas(mCanvasBitmap);
        } else {
            mCanvasBitmap.eraseColor(Color.WHITE);
        }

        int undoSize = mRedos.size();
        for (int i=0; i<undoSize; i++) {
            DrawingElement de = mRedos.get(i);
            mDrawPaint.setColor(de.getColor());
            mDrawPaint.setStrokeWidth(de.getStrokeWidth());
            mDrawCanvas.drawPath(de.getPath(), mDrawPaint);
        }

        mDrawPaint.setColor(mCurrentColor);
        mDrawPaint.setStrokeWidth(mCurrentStrokeWidth);
        invalidate();
    }

    public float getbrushStrokeWidth() {
        return mDrawPaint.getStrokeWidth();
    }

    public void setBrushStrokeWidth(float strokeWidth) {
        mCurrentStrokeWidth = strokeWidth;
        mDrawPaint.setStrokeWidth(strokeWidth);
    }

    public int getBrushColor() {
        return mDrawPaint.getColor();
    }

    public void setBrushColor(int color) {
        mCurrentColor = color;
        mDrawPaint.setColor(color);
    }

    public Bitmap getBitmap() {
        return mCanvasBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        if (mOriginalBitmap != null && !mOriginalBitmap.isRecycled()) {
            mOriginalBitmap.recycle();
        }

        if (bitmap != null) {
            mOriginalBitmap = bitmap;
            mCanvasBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        } else {
            mOriginalBitmap = null;
            mCanvasBitmap.eraseColor(Color.WHITE);
        }

        mDrawCanvas = new Canvas(mCanvasBitmap);
        invalidate();
    }

    public void resetHistory() {
        mRedos.clear();
    }


    public final static class DrawingElement {

        private final Path mPath;
        private final int mColor;
        private final float mStrokeWidth;

        public DrawingElement(Path path, int color, float strokeWidth) {
            mPath = path;
            mColor = color;
            mStrokeWidth = strokeWidth;
        }

        public Path getPath() {
            return mPath;
        }

        public int getColor() {
            return mColor;
        }

        public float getStrokeWidth() {
            return mStrokeWidth;
        }

    }

}

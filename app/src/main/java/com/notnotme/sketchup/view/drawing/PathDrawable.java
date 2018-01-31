package com.notnotme.sketchup.view.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;

final class PathDrawable implements CanvasDrawable {

    private final int mColor;
    private final float mStrokeWidth;
    private final PathEffect mPathEffect;
    private final Path mPath;

    PathDrawable(int color, float strokeWidth, PathEffect pathEffect, Path path) {
        mColor = color;
        mStrokeWidth = strokeWidth;
        mPathEffect = pathEffect;
        mPath = path;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(mStrokeWidth);
        paint.setColor(mColor);
        paint.setPathEffect(mPathEffect);
        canvas.drawPath(mPath, paint);
    }

}
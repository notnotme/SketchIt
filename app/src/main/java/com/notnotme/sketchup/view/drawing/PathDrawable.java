package com.notnotme.sketchup.view.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;

/* todo: implement Parcelable and implement in inherited class too */
final class PathDrawable implements CanvasDrawable {

    private final int mColor;
    private final float mStrokeWidth;
    private final Effect mEffect;
    private final Path mPath;

    PathDrawable(int color, float strokeWidth, Effect effect, Path path) {
        mColor = color;
        mStrokeWidth = strokeWidth;
        mEffect = effect;
        mPath = path;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(mStrokeWidth);
        paint.setColor(mColor);

        PathEffect currentEffect = paint.getPathEffect();
        switch (mEffect) {
            case NONE:
                if (currentEffect == null) break;
                paint.setPathEffect(mEffect.mPathEffect);
            case DASHED:
                if (Effect.DASHED.mPathEffect.equals(currentEffect)) break;
                paint.setPathEffect(mEffect.mPathEffect);
                break;
            case DOTTED:
                if (Effect.DOTTED.mPathEffect.equals(currentEffect)) break;
                paint.setPathEffect(mEffect.mPathEffect);
                break;
        }

        canvas.drawPath(mPath, paint);
    }

}
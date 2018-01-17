package com.notnotme.sketchup.view;

import android.graphics.Path;

public final class DrawingElement {

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

package com.notnotme.sketchup.view.drawing;

import android.graphics.DashPathEffect;

/* todo: implement Parcelable */
enum Effect {
    NONE    (null),
    DASHED  (new DashPathEffect(new float[] {50, 50}, 0)),
    DOTTED  (new DashPathEffect(new float[] {0,  40}, 0));

    DashPathEffect mPathEffect;

    Effect(DashPathEffect effect) {
        mPathEffect = effect;
    }

}
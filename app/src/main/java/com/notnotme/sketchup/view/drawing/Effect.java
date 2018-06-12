package com.notnotme.sketchup.view.drawing;

import android.graphics.DashPathEffect;

public enum Effect {

    PLAIN(null),
    DASHES(new DashPathEffect(new float[]{50, 50}, 0)),
    DOTS(new DashPathEffect(new float[]{0, 40}, 0));

    final DashPathEffect mPathEffect;

    Effect(DashPathEffect effect) {
        mPathEffect = effect;
    }

}
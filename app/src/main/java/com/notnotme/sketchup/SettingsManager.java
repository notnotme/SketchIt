package com.notnotme.sketchup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class SettingsManager {

    private static final String THEME_KEY = "theme";
    private static final String SMOOTH_DRAW_KEY = "smooth_draw";

    private SharedPreferences mSharedPreferences;

    public SettingsManager(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Theme getTheme() {
        return Theme.valueOf(mSharedPreferences.getString(THEME_KEY, Theme.Blue.name()));
    }

    public void setTheme(Theme theme) {
        mSharedPreferences.edit().putString(THEME_KEY, theme.name()).apply();
    }

    public void setSmoothDrawingEnabled(boolean enable) {
        mSharedPreferences.edit().putBoolean(SMOOTH_DRAW_KEY, enable).apply();
    }

    public boolean isSmoothDrawingEnabled() {
        return mSharedPreferences.getBoolean(SMOOTH_DRAW_KEY, false);
    }

}

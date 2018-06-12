package com.notnotme.sketchup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class SettingsManager {

    private static final String THEME_KEY = "theme";
    private static final String SMOOTH_DRAW_KEY = "smooth_draw";
    private static final String BACK_BUTTON_UNDO_KEY = "back_button_undo";

    private final SharedPreferences mSharedPreferences;

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

    public void setBackButtonUndo(boolean enable) {
        mSharedPreferences.edit().putBoolean(BACK_BUTTON_UNDO_KEY, enable).apply();
    }

    public boolean isBackButtonUndo() {
        return mSharedPreferences.getBoolean(BACK_BUTTON_UNDO_KEY, false);
    }

}

package com.notnotme.sketchup;

import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;

public enum Theme {
    Classic(R.color.colorPrimary, R.color.colorPrimaryDark,R.style.AppTheme),
    Blue(R.color.theme_blue_primary, R.color.theme_blue_primary_dark, R.style.AppTheme_Blue),
    Green(R.color.theme_green_primary, R.color.theme_green_primary_dark, R.style.AppTheme_Green),
    Purple(R.color.theme_purple_primary, R.color.theme_purple_primary_dark, R.style.AppTheme_Purple),
    Red(R.color.theme_red_primary, R.color.theme_red_primary_dark, R.style.AppTheme_Red),
    Orange(R.color.theme_orange_primary, R.color.theme_orange_primary_dark, R.style.AppTheme_Orange);

    private final int mColorPrimary;
    private final int mColorPrimaryDark;
    private final int mStyleId;

    Theme(int colorPrimary, int colorPrimaryDark, int styleId) {
        mColorPrimary = colorPrimary;
        mColorPrimaryDark = colorPrimaryDark;
        mStyleId = styleId;
    }

    @ColorRes
    public int getColorPrimary() {
        return mColorPrimary;
    }

    @ColorRes
    public int getColorPrimaryDark() {
        return mColorPrimaryDark;
    }

    @StyleRes
    public int getStyleId() {
        return mStyleId;
    }

}

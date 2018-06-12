package com.notnotme.sketchup.activity;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.notnotme.sketchup.SettingsManager;
import com.notnotme.sketchup.Theme;
import com.notnotme.sketchup.dao.LocalDatabase;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private LocalDatabase mLocalDatabase;
    private SettingsManager mSettingsManager;
    private Handler mMainHandler;
    private Theme mTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettingsManager = new SettingsManager(this);
        mLocalDatabase = Room.databaseBuilder(this, LocalDatabase.class, "local_storage.db").build();
        mMainHandler = new Handler(Looper.getMainLooper());

        mTheme = mSettingsManager.getTheme();
        setTheme(mTheme.getStyleId());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This update the theme if it was changed in SettingsActivity
        Theme theme = getSettingsManager().getTheme();
        if (!theme.equals(mTheme)) {
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalDatabase.close();
    }

    protected LocalDatabase getLocalDatabase() {
        return mLocalDatabase;
    }

    protected SettingsManager getSettingsManager() {
        return mSettingsManager;
    }

    protected Handler getMainHandler() {
        return mMainHandler;
    }

}

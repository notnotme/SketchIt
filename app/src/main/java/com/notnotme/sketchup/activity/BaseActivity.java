package com.notnotme.sketchup.activity;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.notnotme.sketchup.dao.LocalDatabase;


public class BaseActivity extends AppCompatActivity {

    private LocalDatabase mLocalDatabase;
    private Handler mMainHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalDatabase = Room.databaseBuilder(this, LocalDatabase.class, "local_storage.db").build();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalDatabase.close();
    }

    public LocalDatabase getLocalDatabase() {
        return mLocalDatabase;
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

}

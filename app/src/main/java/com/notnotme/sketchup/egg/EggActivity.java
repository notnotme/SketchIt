package com.notnotme.sketchup.egg;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.notnotme.sketchup.egg.renderer.OpenGLRenderer;
import com.notnotme.sketchup.egg.sound.ModulePlayer;

import java.io.IOException;
import java.io.InputStream;

public final class EggActivity extends AppCompatActivity {

    private static final String TAG = EggActivity.class.getSimpleName();

    private ModulePlayer mPlayer;
    private Thread mPlayerThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InputStream is = null;

        try {
            is = getResources().getAssets().open("clawz-hotwires.mod");
            mPlayer = new ModulePlayer(is);
            mPlayer.setPaused(true);
            is.close();
        } catch (IOException e) {
            Log.d(TAG, "Sound error: " + e.getMessage());
            if (is != null) {
                try { is.close(); } catch (IOException fuckedUp) {
                    fuckedUp.printStackTrace();
                }
            }

            finish();
            return;
        }

        GLSurfaceView glView = new GLSurfaceView(this);
        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(5, 6, 5, 0, 16, 0);
        glView.setRenderer(new OpenGLRenderer(this, mPlayer));
        glView.setOnClickListener(v -> Log.d(TAG, "Click"));

        setContentView(glView);
        mPlayerThread = new Thread(mPlayer);
        mPlayerThread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayer.setPaused(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlayer.setPaused(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
        try {
            mPlayerThread.join(1000);
        } catch (InterruptedException e) {
            Log.d(TAG, "Sound error: " + e.getMessage());
        }
    }

}

package com.notnotme.sketchup;

import android.app.Application;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;

import com.squareup.leakcanary.LeakCanary;

public final class SketchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) return;
        LeakCanary.install(this);

        EmojiCompat.init(new BundledEmojiCompatConfig(this));
    }

}

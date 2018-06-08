package com.notnotme.sketchup.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.notnotme.sketchup.BuildConfig;
import com.notnotme.sketchup.R;
import com.notnotme.sketchup.Theme;
import com.notnotme.sketchup.Utils;
import com.notnotme.sketchup.dao.DaoManager;
import com.notnotme.sketchup.dao.Sketch;
import com.notnotme.sketchup.egg.EggActivity;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SettingsActivity extends BaseActivity {

    private static final int EGG_CLICK_COUNT = 7;
    private AlertDialog mAlertDialog;
    private int mEggCounter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.delete_all_sketches).setOnClickListener(v -> mAlertDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_all_sketches)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, i) -> deleteAllSketches())
                .show());

        findViewById(R.id.rate).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                startActivity(intent);
            }
        });

        findViewById(R.id.share).setOnClickListener(v -> {
            ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this);
            intentBuilder.setType("text/plain")
                    .setText("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    .startChooser();
        });

        findViewById(R.id.egg).setOnClickListener(v -> {
            mEggCounter = mEggCounter + 1;
            if (mEggCounter == EGG_CLICK_COUNT) {
                startActivity(new Intent(this, EggActivity.class));
            } else {
                Handler handler = getMainHandler();
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> mEggCounter = 0, TimeUnit.SECONDS.toMillis(3));
            }
        });

        Spinner spinner = findViewById(R.id.theme_spinner);
        spinner.setAdapter(new ThemeAdapter(this, Arrays.asList(Theme.values())));
        spinner.setSelection(getSettingsManager().getTheme().ordinal());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Theme theme = (Theme) adapterView.getAdapter().getItem(i);
                Theme currentTheme = getSettingsManager().getTheme();

                if (!currentTheme.equals(theme)) {
                    getSettingsManager().setTheme(theme);
                    recreate();
                }
            }
        });

        CheckBox smoothDrawCheckBox = findViewById(R.id.smooth_draw);
        smoothDrawCheckBox.setChecked(getSettingsManager().isSmoothDrawingEnabled());
        smoothDrawCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> getSettingsManager().setSmoothDrawingEnabled(isChecked));

        CheckBox backButtonUndo = findViewById(R.id.back_button_undo);
        backButtonUndo.setChecked(getSettingsManager().isBackButtonUndo());
        backButtonUndo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSettingsManager().setBackButtonUndo(isChecked);
            }
        });

        String versionString = getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME;
        versionString += System.lineSeparator();
        versionString += "(" + BuildConfig.BUILD_TYPE + ")";

        TextView version = findViewById(R.id.version);
        version.setText(versionString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    public void deleteAllSketches() {
        AsyncTask.execute(() -> {
            if (isDestroyed() || isFinishing()) return;

            DaoManager daoManager = getLocalDatabase().getDaoManager();
            AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
            try {
                List<Sketch> sketches = daoManager.getAllSketch();
                daoManager.deleteSketches(sketches);
                for (Sketch sketch : sketches) {
                    Utils.deleteImageFile(this, new File(sketch.getPath()));
                }
            } catch (Exception e) {
                exceptionAtomicReference.set(e);
            }

            getMainHandler().post(() -> {
                if (isDestroyed() || isFinishing()) return;

                Exception exception = exceptionAtomicReference.get();
                if (exception != null) {
                    Snackbar.make(findViewById(R.id.coordinator),
                            "Error : " + System.lineSeparator() + exception.getLocalizedMessage(),
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.coordinator),
                            getString(R.string.all_sketches_deleted),
                            Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

}

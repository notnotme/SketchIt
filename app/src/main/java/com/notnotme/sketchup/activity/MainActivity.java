package com.notnotme.sketchup.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewSwitcher;

import com.notnotme.sketchup.Callback;
import com.notnotme.sketchup.R;
import com.notnotme.sketchup.Utils;
import com.notnotme.sketchup.dao.Sketch;
import com.notnotme.sketchup.fragment.AlbumFragment;
import com.notnotme.sketchup.fragment.SketchFragment;
import com.notnotme.sketchup.fragment.ToolsFragment;
import com.notnotme.sketchup.view.drawing.DrawingView;
import com.notnotme.sketchup.view.drawing.Effect;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class MainActivity extends BaseActivity implements SketchFragment.SketchFragmentCallback,
        AlbumFragment.AlbumFragmentCallback, ToolsFragment.ToolsCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    public final static String ARG_SKETCH = TAG + ".arg_sketch";

    private final static String STATE_SWITCHER = TAG + ".switcher";

    private final static int SWITCHER_SKETCH = 0;
    private final static int SWITCHER_ALBUM = 1;

    private ViewSwitcher mViewSwitcher;
    private AlertDialog mAlertDialog;
    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewSwitcher = findViewById(R.id.switcher);

        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    FragmentManager fm = getSupportFragmentManager();
                    ToolsFragment toolsFragment = (ToolsFragment) fm.findFragmentById(R.id.fragment_tools);
                    toolsFragment.resetScroll();
                }
            }
        });

        if (savedInstanceState != null) {
            switch (savedInstanceState.getInt(STATE_SWITCHER)) {
                case SWITCHER_ALBUM:
                    showAlbumFragment();
                    break;
                case SWITCHER_SKETCH:
                    showSketchFragment();
                    break;
            }
        } else {
            showSketchFragment();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getData() != null) {
            onNewIntent(intent);
            intent.setData(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SWITCHER, mViewSwitcher.getDisplayedChild());
    }

    @Override
    public void onBackPressed() {
        switch (mViewSwitcher.getDisplayedChild()) {
            case SWITCHER_ALBUM:
                AlbumFragment albumFragment = (AlbumFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_album);
                if (albumFragment.isInEditMode()) {
                    albumFragment.exitEditMode();
                    return;
                } else {
                    showSketchFragment();
                    return;
                }

            case SWITCHER_SKETCH:
                SketchFragment sketchFragment = (SketchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_sketch);
                if (sketchFragment.isInImport()) {
                    sketchFragment.exitImportMode();
                    return;
                } else if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    return;
                }
        }

        super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String path = intent.getStringExtra(ARG_SKETCH);
        if (path == null && intent.getData() != null) {
            path = intent.getData().toString();
        }

        if (path != null) {
            loadSketch(path, path.startsWith("content"));
        }
    }

    @Override
    public void showSketchFragment() {
        mViewSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_1));
        mViewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_1));
        mViewSwitcher.setDisplayedChild(SWITCHER_SKETCH);
    }

    @Override
    public void showAlbumFragment() {
        SketchFragment sketchFragment = (SketchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_sketch);
        if (sketchFragment.isInImport()) {
            sketchFragment.exitImportMode();
        }

        mViewSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_2));
        mViewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_2));
        mViewSwitcher.setDisplayedChild(SWITCHER_ALBUM);
    }

    @Override
    public void showSketch(Sketch sketch) {
        Intent viewSketchIntent = new Intent(this, SketchViewActivity.class);
        viewSketchIntent.putExtra(SketchViewActivity.ARG_SKETCH, sketch);
        startActivity(viewSketchIntent);
    }

    @Override
    public void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public void showToolsFragment() {
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void hideToolsFragment() {
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public boolean isToolsFragmentVisible() {
        return mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED
                || mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    @Override
    public void saveSketch(Bitmap bitmap) {
        AsyncTask.execute(() -> {
            if (isDestroyed() || isFinishing()) return;

            AtomicReference<String> picturePath = new AtomicReference<>();
            AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
            try {
                long now = new Date().getTime();

                picturePath.set(Utils.saveImageToAppStorage(this, String.valueOf(now), bitmap).getPath());
                getLocalDatabase().getDaoManager().saveSketch(new Sketch(picturePath.get(), now));
            } catch (Exception e) {
                exceptionAtomicReference.set(e);
            }

            getMainHandler().post(() -> {
                if (isDestroyed() || isFinishing()) return;

                Exception exception = exceptionAtomicReference.get();
                if (exception != null) {
                    mAlertDialog = new AlertDialog.Builder(this)
                            .setMessage(exception.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    AlbumFragment albumFragment = (AlbumFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_album);
                    albumFragment.loadSketches(null);

                    Snackbar.make(findViewById(R.id.coordinator),
                            getString(R.string.sketch_saved),
                            Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void newSketch() {
        SketchFragment sketchFragment = (SketchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_sketch);

        if (mViewSwitcher.getDisplayedChild() != SWITCHER_SKETCH) {
            showSketchFragment();
        } else if (sketchFragment.isInImport()) {
            sketchFragment.exitImportMode();
        }

        sketchFragment.setSketch(null, false);
    }

    @Override
    public void loadSketch(String path, boolean isImport) {
        SketchFragment sketchFragment = (SketchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_sketch);

        if (mViewSwitcher.getDisplayedChild() != SWITCHER_SKETCH) {
            showSketchFragment();
        } else if (sketchFragment.isInImport()) {
            sketchFragment.exitImportMode();
        }


        AsyncTask.execute(() -> {
            if (isDestroyed() || isFinishing()) return;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            AtomicReference<Bitmap> bitmap = new AtomicReference<>();

            if (path.startsWith("content")) {

                try {
                    bitmap.set(MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path)));
                } catch (Exception e) {
                    getMainHandler().post(() -> {
                        if (isDestroyed() || isFinishing()) return;
                        Snackbar.make(findViewById(R.id.coordinator),
                                "Error : " + System.lineSeparator() + e.getLocalizedMessage(),
                                Snackbar.LENGTH_SHORT).show();
                    });
                    return;
                }

            } else {
                bitmap.set(BitmapFactory.decodeFile(path, options));
            }

            getMainHandler().post(() -> {
                if (isDestroyed() || isFinishing()) return;
                sketchFragment.setSketch(bitmap.get(), isImport);
            });
        });
    }

    @Override
    public void deleteSketches(List<Sketch> sketches, Callback<List<Sketch>> callback) {
        AsyncTask.execute(() -> {
            if (isDestroyed() || isFinishing()) return;

            AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
            try {
                getLocalDatabase().getDaoManager().deleteSketches(sketches);
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
                    callback.failure(exception);
                } else {
                    callback.success(sketches);
                    Snackbar.make(findViewById(R.id.coordinator),
                            getString(R.string.sketch_deleted, String.valueOf(sketches.size())),
                            Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void shareSketches(List<Sketch> sketches) {
        if (sketches.isEmpty()) return;

        ShareCompat.IntentBuilder shareIntentBuilder = ShareCompat.IntentBuilder.from(this).setType("image/png");

        if (sketches.size() > 1) {
            for (Sketch sketch : sketches) {
                shareIntentBuilder.addStream(FileProvider
                        .getUriForFile(this, getPackageName() + ".provider", new File(sketch.getPath())));
            }
        } else {
            shareIntentBuilder.setStream(FileProvider
                    .getUriForFile(this, getPackageName() + ".provider", new File(sketches.get(0).getPath())));
        }

        if (shareIntentBuilder.getIntent().resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntentBuilder.createChooserIntent());
        }
    }

    @Override
    public void shareSketch(Bitmap bitmap) {
        AsyncTask.execute(() -> {
            if (isDestroyed() || isFinishing()) return;

            AtomicReference<File> picturePath = new AtomicReference<>();
            AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
            try {
                picturePath.set(Utils.saveTempImage(this, bitmap));
            } catch (Exception e) {
                exceptionAtomicReference.set(e);
            }

            getMainHandler().post(() -> {
                if (isDestroyed() || isFinishing()) return;

                Exception exception = exceptionAtomicReference.get();
                if (exception != null) {
                    mAlertDialog = new AlertDialog.Builder(this)
                            .setMessage(exception.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    ShareCompat.IntentBuilder shareIntentBuilder = ShareCompat.IntentBuilder.from(this).setType("image/png");
                    shareIntentBuilder.setStream(FileProvider
                            .getUriForFile(this, getPackageName() + ".provider", picturePath.get()));

                    if (shareIntentBuilder.getIntent().resolveActivity(getPackageManager()) != null) {
                        startActivity(shareIntentBuilder.createChooserIntent());
                    }
                }
            });
        });
    }

    @Override
    public void getAllSketches(Callback<List<Sketch>> callback) {
        AsyncTask.execute(() -> {
            AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();

            List<Sketch> sketchList = new ArrayList<>();
            try {
                sketchList.addAll(getLocalDatabase().getDaoManager().getAllSketch());
            } catch (Exception e) {
                exceptionAtomicReference.set(e);
            }

            getMainHandler().post(() -> {
                if (isDestroyed() || isFinishing()) return;

                Exception exception = exceptionAtomicReference.get();
                if (exception != null) {
                    callback.failure(exception);
                } else {
                    callback.success(sketchList);
                }
            });
        });
    }

    @Override
    public void setDrawMode(DrawingView.DrawMode mode, float width) {

    }

    @Override
    public void setCurrentEffect(Effect effect) {

    }

    @Override
    public void setPaintColor(int color) {

    }

}

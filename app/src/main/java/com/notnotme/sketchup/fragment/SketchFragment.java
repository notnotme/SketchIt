package com.notnotme.sketchup.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.jsibbold.zoomage.ZoomageView;
import com.notnotme.sketchup.R;
import com.notnotme.sketchup.popup.MainMenuPopup;
import com.notnotme.sketchup.view.drawing.DrawingView;

public final class SketchFragment extends BaseFragment {

    private static final String TAG = SketchFragment.class.getSimpleName();

    private static final String STATE_IS_IMPORTING = TAG + ".importing";
    private static final String STATE_IMPORT_IMAGE_PATH = TAG + ".import_image_path";

    private static final int REQUEST_IMPORT_PICTURE = 1337;

    private SketchFragmentCallback mCallback;
    private DrawingView mDrawingView;

    private ZoomageView mImportImage;
    private FloatingActionButton mFab;

    private MainMenuPopup mFilePopup;
    private AlertDialog mAlertDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sketch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDrawingView = view.findViewById(R.id.sketch_drawing);

        ImageButton btnPlus = view.findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            if (mCallback.isToolsFragmentVisible()) {
                mCallback.hideToolsFragment();
                return;
            }

            mFilePopup.showAtLocation(btnPlus, Gravity.NO_GRAVITY,
                    btnPlus.getLeft() + btnPlus.getWidth() / 3, btnPlus.getBottom() + 50);
        });

        view.findViewById(R.id.undo).setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            if (mCallback.isToolsFragmentVisible()) {
                mCallback.hideToolsFragment();
                return;
            }

            mDrawingView.undo();
        });

        view.findViewById(R.id.btn_albums).setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            if (mCallback.isToolsFragmentVisible()) {
                mCallback.hideToolsFragment();
                return;
            }
            mCallback.showAlbumFragment();
        });

        view.findViewById(R.id.btn_tools).setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            if (mCallback.isToolsFragmentVisible()) {
                mCallback.hideToolsFragment();
            } else {
                mCallback.showToolsFragment();
            }
        });

        mImportImage = view.findViewById(R.id.import_image);
        mFab = view.findViewById(R.id.import_ok);

        mDrawingView.setStrokeWidth(DrawingView.STROKE_DEFAULT_SIZE);
        mDrawingView.setColor(Color.BLACK);
        mDrawingView.setOnTouchListener((view1, motionEvent) -> {
            if (mCallback.isToolsFragmentVisible()) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        view1.performClick();
                        return true;
                    case MotionEvent.ACTION_UP:
                        view1.performClick();
                        mCallback.hideToolsFragment();
                        return true;
                }
            }

            return false;
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            mDrawingView.setBitmap(null);
            mFab.setTag(false);
        } else {
            if (savedInstanceState.getBoolean(STATE_IS_IMPORTING)) {
                String uri = savedInstanceState.getString(STATE_IMPORT_IMAGE_PATH);
                mImportImage.setImageURI(Uri.parse(uri));
                mImportImage.setTag(uri);
                enterImportMode();
            } else {
                mFab.setTag(false);
            }
        }

        Context context = getContext();
        mFilePopup = new MainMenuPopup(context, new MainMenuPopup.PopupListener() {
            @Override
            public void newSketch() {
                mFilePopup.dismiss();
                mAlertDialog = new AlertDialog.Builder(getContext())
                        .setMessage(R.string.start_drawing_question)
                        .setPositiveButton(android.R.string.yes, (dialog, id) -> mCallback.newSketch())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            @Override
            public void saveSketch() {
                mFilePopup.dismiss();
                mAlertDialog = new AlertDialog.Builder(getContext())
                        .setMessage(R.string.save_drawing_question)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> mCallback.saveSketch(mDrawingView.getBitmap()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            @Override
            public void importSketch() {
                mFilePopup.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.import_picture_from)), REQUEST_IMPORT_PICTURE);
            }

            @Override
            public void shareSketch() {
                mCallback.shareSketch(mDrawingView.getBitmap());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // This should be enabled here in case a change happen in settings
        mDrawingView.setSmoothDrawing(getSettingsManager().isSmoothDrawingEnabled());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_IMPORTING, (Boolean) mFab.getTag());
        outState.putString(STATE_IMPORT_IMAGE_PATH, (String) mImportImage.getTag());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (SketchFragmentCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFilePopup != null && mFilePopup.isShowing()) {
            mFilePopup.dismiss();
        }

        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMPORT_PICTURE && resultCode == Activity.RESULT_OK) {
            Uri contentUri = data.getData();
            if (contentUri != null) {
                String imageUri = data.getData().toString();
                mImportImage.setTag(imageUri);
                mCallback.loadSketch(imageUri, true);
            }
        }
    }

    public boolean isInImport() {
        if (mFab == null || mFab.getTag() == null) return false;
        return (Boolean) mFab.getTag();
    }

    public void enterImportMode() {
        mImportImage.setVisibility(View.VISIBLE);
        mFab.setTag(true);
        mFab.show();
        mFab.setOnClickListener(view -> {
            mFab.setOnClickListener(null);

            mImportImage.setDrawingCacheEnabled(true);
            setSketch(mImportImage.getDrawingCache().copy(Bitmap.Config.RGB_565, true), false);
            mImportImage.setDrawingCacheEnabled(false);

            exitImportMode();
        });
    }

    public void exitImportMode() {
        mFab.hide();
        mFab.setTag(false);
        mImportImage.setImageResource(0);
        mImportImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImportImage.setVisibility(View.GONE);
    }

    public void setSketch(Bitmap bitmap, boolean imported) {
        if (!imported) {
            mDrawingView.setBitmap(bitmap);
            mDrawingView.resetHistory();
        } else {
            mImportImage.setImageBitmap(bitmap);
            mImportImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            enterImportMode();
        }
    }

    public DrawingView getDrawingView() {
        return mDrawingView;
    }


    public interface SketchFragmentCallback {
        void showAlbumFragment();

        void saveSketch(Bitmap bitmap);

        void shareSketch(Bitmap bitmap);

        void newSketch();

        void loadSketch(String path, boolean isImport);

        void showToolsFragment();

        void hideToolsFragment();

        boolean isToolsFragmentVisible();
    }

}

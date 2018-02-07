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
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.jsibbold.zoomage.ZoomageView;
import com.notnotme.sketchup.R;
import com.notnotme.sketchup.popup.ColorPopup;
import com.notnotme.sketchup.popup.HSVColorPopup;
import com.notnotme.sketchup.popup.MainMenuPopup;
import com.notnotme.sketchup.popup.PencilPopup;
import com.notnotme.sketchup.view.drawing.DrawingView;
import com.notnotme.sketchup.view.drawing.Effect;

public final class SketchFragment extends Fragment {

    private static final String TAG = SketchFragment.class.getSimpleName();
    private static final int REQUEST_IMPORT_PICTURE = 1337;
    private static final String STATE_IS_IMPORTING = TAG + ".importing";
    private static final String STATE_IMPORT_IMAGE_PATH = TAG + ".import_image_path";

    private SketchFragmentCallback mCallback;
    private ImageButton mBtnPlus;
    private ImageButton mBtnPencil;
    private ImageButton mBtnColors;
    private DrawingView mDrawingView;

    private ZoomageView mImportImage;
    private FloatingActionButton mFab;

    private MainMenuPopup mFilePopup;
    private PencilPopup mPencilPopup;
    private ColorPopup mColorPopup;
    private HSVColorPopup mHSVColorPopup;
    private PopupWindow mPopupWindow;
    private AlertDialog mAlertDialog;

    private View.OnClickListener mImportOkClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFab.setOnClickListener(null);

                    mImportImage.setDrawingCacheEnabled(true);
                    setSketch(mImportImage.getDrawingCache().copy(Bitmap.Config.RGB_565, true), false);
                    mImportImage.setDrawingCacheEnabled(false);

                    exitImportMode();
                }
            };

    private MainMenuPopup.PopupListener mFilePopupListener =
            new MainMenuPopup.PopupListener() {
                @Override
                public void newSketch() {
                    mPopupWindow.dismiss();
                    mAlertDialog = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.start_drawing_question)
                            .setPositiveButton(android.R.string.yes, (dialog, id) -> mCallback.newSketch())
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }

                @Override
                public void saveSketch() {
                    mPopupWindow.dismiss();
                    mAlertDialog = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.save_drawing_question)
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> mCallback.saveSketch(mDrawingView.getBitmap()))
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }

                @Override
                public void importSketch() {
                    mPopupWindow.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.import_picture_from)), REQUEST_IMPORT_PICTURE);
                }

                @Override
                public void shareSketch() {
                    mCallback.shareSketch(mDrawingView.getBitmap());
                }
            };

    private ColorPopup.PopupListener mColorPopupListener =
            new ColorPopup.PopupListener() {
                @Override
                public void setColor(int color) {
                    mPopupWindow.dismiss();
                    mDrawingView.setBrushColor(color);
                }

                @Override
                public void moreColor() {
                    mPopupWindow.dismiss();
                    mPopupWindow = mHSVColorPopup;

                    mHSVColorPopup.setColor(mDrawingView.getBrushColor());
                    mHSVColorPopup.showAtLocation(mBtnColors, Gravity.NO_GRAVITY,
                            mBtnColors.getLeft() + mBtnColors.getWidth() / 3, mBtnColors.getBottom() + 50);
                }

                @Override
                public int getCurrentColor() {
                    return mDrawingView.getBrushColor();
                }
            };

    private HSVColorPopup.PopupListener mHSVColorPopupListener =
            new HSVColorPopup.PopupListener() {
                @Override
                public void setColor(int color) {
                    mPopupWindow.dismiss();
                    mDrawingView.setBrushColor(color);
                    mColorPopup.notifyColorChanged();
                }
            };

    private PencilPopup.PopupListener mPencilPopupListener =
            new PencilPopup.PopupListener() {
                @Override
                public void setDrawMode(DrawingView.DrawMode mode, float width) {
                    mPopupWindow.dismiss();
                    mDrawingView.setDrawMode(mode);
                    mDrawingView.setBrushWidth(width);
                }

                @Override
                public Effect getCurrentEffect() {
                    return mDrawingView.getEffect();
                }

                @Override
                public void setCurrentEffect(Effect effect) {
                    mPopupWindow.dismiss();
                    mDrawingView.setCurrentEffect(effect);
                }

                @Override
                public DrawingView.DrawMode getCurrentDrawMode() {
                    return mDrawingView.getDrawMode();
                }

                @Override
                public float getBrushWidth() {
                    return mDrawingView.getBrushWidth();
                }
            };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sketch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDrawingView = view.findViewById(R.id.sketch_drawing);
        mBtnPlus = view.findViewById(R.id.btn_plus);
        mBtnPencil = view.findViewById(R.id.btn_pencil);
        mBtnColors = view.findViewById(R.id.btn_color);

        mBtnPlus.setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            mPopupWindow = mFilePopup;
            mFilePopup.showAtLocation(mBtnPlus, Gravity.NO_GRAVITY,
                    mBtnPlus.getLeft() + mBtnPlus.getWidth() / 3, mBtnPlus.getBottom() + 50);
        });

        mBtnPencil.setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            mPopupWindow = mPencilPopup;
            mPencilPopup.showAtLocation(mBtnPencil, Gravity.NO_GRAVITY,
                    mBtnPencil.getLeft() + mBtnPencil.getWidth() / 3, mBtnPencil.getBottom() + 50);
        });

        mBtnColors.setOnClickListener(v -> {
            if (isInImport()) exitImportMode();
            mPopupWindow = mColorPopup;
            mColorPopup.showAtLocation(mBtnColors, Gravity.NO_GRAVITY,
                    mBtnColors.getLeft() + mBtnColors.getWidth() / 3, mBtnColors.getBottom() + 50);
        });

        view.findViewById(R.id.undo).setOnClickListener(v -> mDrawingView.undo());
        view.findViewById(R.id.btn_albums).setOnClickListener(v -> mCallback.showAlbumFragment());

        mImportImage = view.findViewById(R.id.import_image);
        mFab = view.findViewById(R.id.import_ok);

        mDrawingView.setBrushWidth(DrawingView.STROKE_DEFAULT_SIZE);
        mDrawingView.setBrushColor(Color.BLACK);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            mDrawingView.setBitmap(null);
            mFab.setTag(false);
        } else {
            if (savedInstanceState.getBoolean(STATE_IS_IMPORTING)) {
                mImportImage.setImageURI(Uri.parse(savedInstanceState.getString(STATE_IMPORT_IMAGE_PATH)));
                enterImportMode();
            } else {
                mFab.setTag(false);
            }
        }

        Context context = getContext();
        mFilePopup = new MainMenuPopup(context, mFilePopupListener);
        mColorPopup = new ColorPopup(context, mColorPopupListener);
        mHSVColorPopup = new HSVColorPopup(context, mHSVColorPopupListener);
        mPencilPopup = new PencilPopup(context, mPencilPopupListener);
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
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
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
        mFab.setOnClickListener(mImportOkClickListener);
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

    public interface SketchFragmentCallback {
        void showAlbumFragment();

        void saveSketch(Bitmap bitmap);

        void shareSketch(Bitmap bitmap);

        void newSketch();

        void loadSketch(String path, boolean isImport);
    }

}

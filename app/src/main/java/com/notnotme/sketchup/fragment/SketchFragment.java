package com.notnotme.sketchup.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.github.chrisbanes.photoview.PhotoView;
import com.notnotme.sketchup.R;
import com.notnotme.sketchup.Utils;
import com.notnotme.sketchup.view.DrawingView;
import com.notnotme.sketchup.view.RatioTouchListener;

import java.io.IOException;
import java.util.Arrays;

import static android.provider.MediaStore.Images;

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
    private PopupWindow mPopupWindow;

    private PhotoView mImportImage;
    private FloatingActionButton mFab;

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

        mBtnPlus.setOnClickListener(v -> showPlusPopup());
        mBtnPencil.setOnClickListener(v -> showPencilPopup());
        mBtnColors.setOnClickListener(v -> showColorPopup());
        view.findViewById(R.id.undo).setOnClickListener(v -> undoDrawing());
        view.findViewById(R.id.btn_albums).setOnClickListener(v -> mCallback.showAlbumFragment());

        mImportImage = view.findViewById(R.id.import_image);
        mImportImage.setMinimumScale(0.5f);
        mFab = view.findViewById(R.id.import_ok);

        mDrawingView.setBrushStrokeWidth(DrawingView.STROKE_MEDIUM_SIZE);
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
        Context context = getContext();

        if (requestCode == REQUEST_IMPORT_PICTURE && resultCode == Activity.RESULT_OK && context != null) {
            Uri contentUri = data.getData();
            if (contentUri != null) {
                Uri imageUri = data.getData();
                try {
                    mImportImage.setTag(imageUri.toString());
                    Bitmap bitmap = Images.Media.getBitmap(context.getContentResolver(), imageUri);
                    setSketch(bitmap, true);
                } catch (IOException e) {
                    mAlertDialog = new AlertDialog.Builder(context)
                            .setMessage(e.getLocalizedMessage())
                            .setCancelable(true)
                            .show();
                }
            }
        }
    }

    private void undoDrawing() {
        mDrawingView.undo();
    }

    public boolean isInImport() {
        if (mFab == null || mFab.getTag() == null) return false;
        return  (Boolean) mFab.getTag();
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
        mImportImage.setVisibility(View.GONE);
    }

    // todo: make async loading because photo can be uber huge
    public void setSketch(String path, boolean imported) {
        if (!imported) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            mDrawingView.setBitmap(BitmapFactory.decodeFile(path, options));
            mDrawingView.resetHistory();
        } else {
            mImportImage.setImageURI(Uri.parse(path));
            enterImportMode();
        }
    }

    // todo: make async loading because photo can be uber huge
    public void setSketch(Bitmap bitmap, boolean imported) {
        if (!imported) {
            mDrawingView.setBitmap(bitmap);
            mDrawingView.resetHistory();
        } else {
            mImportImage.setImageBitmap(bitmap);
            enterImportMode();
        }
    }

    private void showPlusPopup() {
        if (isInImport()) exitImportMode();

        Context context = getContext();
        View layout = View.inflate(context, R.layout.popup_plus, null);
        mPopupWindow = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        layout.findViewById(R.id.btn_new).setOnClickListener(v -> {
            mPopupWindow.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.start_drawing_question);
            builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {
                mDrawingView.setBitmap(null);
                mDrawingView.resetHistory();
                dialog.dismiss();
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        layout.findViewById(R.id.btn_save).setOnClickListener(v -> {
            mPopupWindow.dismiss();
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(context);
            saveDialog.setMessage(R.string.save_drawing_question);
            saveDialog.setPositiveButton(android.R.string.yes, (dialog, which) -> mCallback.saveSketch(mDrawingView.getBitmap()));
            saveDialog.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
            saveDialog.show();
        });

        layout.findViewById(R.id.btn_import).setOnClickListener(view -> {
            mPopupWindow.dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.import_picture_from)), REQUEST_IMPORT_PICTURE);
        });

        // Creating the PopupWindow
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAtLocation(getView(), Gravity.NO_GRAVITY, mBtnPlus.getLeft() + mBtnPlus.getWidth() / 3, mBtnPlus.getBottom() + 50);
    }

    private void showPencilPopup() {
        if (isInImport()) exitImportMode();

        Context context = getContext();
        View layout = View.inflate(context, R.layout.popup_pencil, null);

        mPopupWindow = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        RecyclerView rc = layout.findViewById(R.id.recycler);
        rc.setHasFixedSize(true);
        rc.setAdapter(new PencilAdapter(Arrays.asList(
                new PencilAdapter.Pencil(DrawingView.STROKE_SMALL_SIZE, R.mipmap.pen_small),
                new PencilAdapter.Pencil(DrawingView.STROKE_MEDIUM_SIZE, R.mipmap.pen_medium),
                new PencilAdapter.Pencil(DrawingView.STROKE_LARGE_SIZE, R.mipmap.pen_large)),
                pencil -> {
                    mPopupWindow.dismiss();
                    mDrawingView.setBrushStrokeWidth(pencil.getSize());
                }));

        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAtLocation(getView(), Gravity.NO_GRAVITY, mBtnPencil.getLeft() + mBtnPencil.getWidth() / 3, mBtnPencil.getBottom() + 50);
    }

    private void showColorPopup() {
        if (isInImport()) exitImportMode();

        View layout = View.inflate(getContext(), R.layout.popup_colors, null);
        RecyclerView rv = layout.findViewById(R.id.recycler);

        mPopupWindow = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        rv.setHasFixedSize(true);
        rv.setAdapter(new ColorAdapter(
                Arrays.asList(getResources().getStringArray(R.array.colors_array)),
                color -> {
                    mPopupWindow.dismiss();
                    mDrawingView.setBrushColor(color);
                }));

        layout.findViewById(R.id.more).setOnClickListener(view -> {
            mPopupWindow.dismiss();
            showHsvPopup(mDrawingView.getBrushColor());
        });

        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, mBtnColors.getLeft() + mBtnColors.getWidth() / 3, mBtnColors.getBottom() + 50);
    }

    private void showHsvPopup(int color) {
        View layout = View.inflate(getContext(), R.layout.popup_hsv, null);

        ImageView hueImage = layout.findViewById(R.id.hue_color);
        ImageView hueSelector = layout.findViewById(R.id.hue_selector);
        ImageView colorImage = layout.findViewById(R.id.hue_mask);
        ImageView colorSelector = layout.findViewById(R.id.color_selector);
        ImageView colorPreview = layout.findViewById(R.id.color_preview);
        Bitmap hueBitmap = ((BitmapDrawable) hueImage.getDrawable()).getBitmap();

        float tempHSV[] = new float[3];

        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), tempHSV);
        colorPreview.setColorFilter(Color.HSVToColor(tempHSV), PorterDuff.Mode.SRC);

        layout.findViewById(R.id.hue_container).setOnTouchListener(new RatioTouchListener() {
            @Override
            public boolean onTouch(View view, float x, float y, float ratioX, float ratioY) {
                hueSelector.animate()
                        .y(y - hueSelector.getHeight()/2)
                        .setDuration(0)
                        .start();

                int bitmapOffsetY = (int) ((hueBitmap.getHeight()-1)*ratioY);
                int hueValue = hueBitmap.getPixel(hueBitmap.getWidth() / 2, bitmapOffsetY);
                tempHSV[0] = Utils.clamp(ratioY, 0f, 1f) * 360.0f;

                colorImage.setColorFilter(hueValue, PorterDuff.Mode.MULTIPLY);
                colorPreview.setColorFilter(Color.HSVToColor(tempHSV), PorterDuff.Mode.SRC);
                return true;
            }
        });

        layout.findViewById(R.id.color_container).setOnTouchListener(new RatioTouchListener() {
            @Override
            public boolean onTouch(View view, float x, float y, float ratioX, float ratioY) {
                colorSelector.animate()
                        .x(x - colorSelector.getWidth()/2)
                        .y(y - colorSelector.getHeight()/2)
                        .setDuration(0)
                        .start();

                tempHSV[2] = Utils.clamp(ratioX, 0f, 1f);
                tempHSV[1] = 1f - Utils.clamp(ratioY, 0f, 1f);
                colorPreview.setColorFilter(Color.HSVToColor(tempHSV), PorterDuff.Mode.SRC);
                return true;
            }
        });

        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float hueStep = (float) hueBitmap.getHeight() / 360f;
                float offsetY = ((tempHSV[0]/360.0f) * hueImage.getHeight()) / hueStep;


                int bitmapOffsetY = (int) ((hueBitmap.getHeight()-1) * (tempHSV[0] / 360.0f));
                int hueValue = hueBitmap.getPixel(hueBitmap.getWidth() / 2, bitmapOffsetY);
                colorImage.setColorFilter(hueValue, PorterDuff.Mode.MULTIPLY);

                hueSelector.animate()
                        .y(offsetY * hueStep - (hueSelector.getHeight()/2))
                        .alpha(1)
                        .setDuration(0);

                colorSelector.animate()
                        .x((tempHSV[2] * colorImage.getWidth()) - (colorSelector.getWidth()/2))
                        .y(((1f - tempHSV[1]) * colorImage.getHeight()) - (colorSelector.getHeight()/2))
                        .alpha(1)
                        .setDuration(0);
            }
        });

        mPopupWindow = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mPopupWindow.setFocusable(true);
        mPopupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, mBtnColors.getLeft() + mBtnColors.getWidth() / 3, mBtnColors.getBottom() + 50);
        mPopupWindow.setOnDismissListener(() -> mDrawingView.setBrushColor(Color.HSVToColor(tempHSV)));
    }


    public interface SketchFragmentCallback {
        void showAlbumFragment();

        void saveSketch(Bitmap bitmap);
    }

}

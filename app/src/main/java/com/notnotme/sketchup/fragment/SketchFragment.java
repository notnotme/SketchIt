package com.notnotme.sketchup.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.view.DrawingView;

import java.util.Arrays;

public final class SketchFragment extends Fragment {

    private SketchFragmentCallback mCallback;
    private ImageButton mBtnPlus;
    private ImageButton mBtnPencil;
    private ImageButton mBtnColors;
    private DrawingView mDrawingView;

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
        mBtnColors.setOnClickListener(v -> showColoursPopup());
        view.findViewById(R.id.undo).setOnClickListener(v -> undoDrawing());
        view.findViewById(R.id.btn_albums).setOnClickListener(v -> mCallback.showAlbumFragment());

        mDrawingView.setBrushStrokeWidth(DrawingView.STROKE_MEDIUM_SIZE);
        mDrawingView.setBrushColor(Color.BLACK);
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
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private void undoDrawing() {
        mDrawingView.undo();
    }

    public void setSketch(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        mDrawingView.setBitmap(BitmapFactory.decodeFile(path, options));
        mDrawingView.resetHistory();
    }

    private void showPlusPopup() {
        Context context = getContext();
        View layout = View.inflate(context, R.layout.popup_plus, null);
        PopupWindow popup = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        layout.findViewById(R.id.btn_new).setOnClickListener(v -> {
            popup.dismiss();
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
            popup.dismiss();
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(context);
            saveDialog.setMessage(R.string.save_drawing_question);
            saveDialog.setPositiveButton(android.R.string.yes, (dialog, which) -> mCallback.saveSketch(mDrawingView.getBitmap()));
            saveDialog.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
            saveDialog.show();
        });

        // Creating the PopupWindow
        popup.setAnimationStyle(android.R.style.Animation_Dialog);
        popup.setFocusable(true);
        popup.showAtLocation(getView(), Gravity.NO_GRAVITY, mBtnPlus.getLeft() + mBtnPlus.getWidth() / 3, mBtnPlus.getBottom() + 50);
    }

    private void showPencilPopup() {
        Context context = getContext();
        View layout = View.inflate(context, R.layout.popup_pencil, null);

        PopupWindow popup = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        RecyclerView rc = layout.findViewById(R.id.recycler);
        rc.setHasFixedSize(true);
        rc.setAdapter(new PencilAdapter(Arrays.asList(
                new PencilAdapter.Pencil(DrawingView.STROKE_SMALL_SIZE, R.mipmap.pen_small),
                new PencilAdapter.Pencil(DrawingView.STROKE_MEDIUM_SIZE, R.mipmap.pen_medium),
                new PencilAdapter.Pencil(DrawingView.STROKE_LARGE_SIZE, R.mipmap.pen_large)),
                pencil -> {
                    mDrawingView.setBrushStrokeWidth(pencil.getSize());
                    popup.dismiss();
                }));

        popup.setAnimationStyle(android.R.style.Animation_Dialog);
        popup.setFocusable(true);
        popup.showAtLocation(getView(), Gravity.NO_GRAVITY, mBtnPencil.getLeft() + mBtnPencil.getWidth() / 3, mBtnPencil.getBottom() + 50);
    }

    private void showColoursPopup() {
        View layout = View.inflate(getContext(), R.layout.popup_colors, null);
        RecyclerView rv = layout.findViewById(R.id.recycler);

        PopupWindow popup = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        rv.setHasFixedSize(true);
        rv.setAdapter(new ColorAdapter(
                Arrays.asList(getResources().getStringArray(R.array.colors_array)),
                color -> {
                    mDrawingView.setBrushColor(color);
                    popup.dismiss();
                }));

        popup.setAnimationStyle(android.R.style.Animation_Dialog);
        popup.setFocusable(true);
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, mBtnColors.getLeft() + mBtnColors.getWidth() / 3, mBtnColors.getBottom() + 50);
    }

    public interface SketchFragmentCallback {
        void showAlbumFragment();

        void saveSketch(Bitmap bitmap);
    }

}

package com.notnotme.sketchup.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.popup.HSVColorPopup;
import com.notnotme.sketchup.view.drawing.DrawingView;
import com.notnotme.sketchup.view.drawing.Effect;

import java.util.Arrays;

public final class ToolsFragment extends BaseFragment {

    private final static int MIN_STROKE_WIDTH = 4;
    private final static int MAX_STROKE_WIDTH = 50;

    private NestedScrollView mNestedScrollView;
    private ColorAdapter mColorAdapter;
    private ToolsCallback mCallback;
    private HSVColorPopup mHSVColorPopup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNestedScrollView = view.findViewById(R.id.scroll);
        mHSVColorPopup = new HSVColorPopup(getContext(), color -> {
            mHSVColorPopup.dismiss();
            mCallback.getDrawingView().setColor(color);
            mColorAdapter.notifyDataSetChanged();
        });

        Context context = getContext();
        if (context != null) {
            view.findViewById(R.id.border).setBackgroundResource(
                    getSettingsManager().getTheme().getColorPrimary());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        View view = getView();
        if (view != null) {
            initializeSeekBar(view);
            initializeBrushStyles(view, savedInstanceState);
            initializeBrushForms(view, savedInstanceState);
            initializeBrushColors(view);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (ToolsCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void resetScroll() {
        mNestedScrollView.scrollTo(0,0);
    }

    private void initializeSeekBar(View view) {
        View strokeSizePreview = view.findViewById(R.id.stroke_size_preview);
        TextView strokeSizeText = view.findViewById(R.id.text_size);
        SeekBar strokeSizeSeekBar = view.findViewById(R.id.stroke_width);

        int strokeWidth = (int) mCallback.getDrawingView().getStrokeWidth();
        strokeSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int strokeSize = i + MIN_STROKE_WIDTH;

                strokeSizeText.setText(String.valueOf(strokeSize));
                strokeSizePreview.getLayoutParams().height = strokeSize;
                strokeSizePreview.requestLayout();

                mCallback.getDrawingView().setStrokeWidth(strokeSize);
            }
        });

        strokeSizeSeekBar.setMax(MAX_STROKE_WIDTH - MIN_STROKE_WIDTH);
        strokeSizeSeekBar.setProgress(strokeWidth);
    }

    private void initializeBrushStyles(View view, Bundle savedInstanceState) {
        RadioGroup brushTypeGroup = view.findViewById(R.id.brush_style_group);

        if (savedInstanceState == null) {
            int checkedId = R.id.brush_style_plain;
            switch (mCallback.getDrawingView().getEffect()) {
                case DOTS:
                    checkedId = R.id.brush_style_dots;
                    break;
                case DASHES:
                    checkedId = R.id.brush_style_dashes;
                    break;
            }
            brushTypeGroup.check(checkedId);
        } else {
            // Avoid animation bug that cause button to be partially checked
            brushTypeGroup.jumpDrawablesToCurrentState();
        }

        brushTypeGroup.setOnCheckedChangeListener((group, checked) -> {
            switch (checked) {
                case R.id.brush_style_plain:
                    mCallback.getDrawingView().setEffect(Effect.PLAIN);
                    break;

                case R.id.brush_style_dots:
                    mCallback.getDrawingView().setEffect(Effect.DOTS);
                    break;

                case R.id.brush_style_dashes:
                    mCallback.getDrawingView().setEffect(Effect.DASHES);
                    break;
            }
        });
    }

    private void initializeBrushForms(View view, Bundle savedInstanceState) {
        RadioGroup brushFormGroup = view.findViewById(R.id.brush_form_group);

        if (savedInstanceState == null) {
            int checkedId = R.id.brush_form_free;
            switch (mCallback.getDrawingView().getDrawMode()) {
                case LINES:
                    checkedId = R.id.brush_form_line;
                    break;
            }
            brushFormGroup.check(checkedId);
        } else {
            // Avoid animation bug that cause button to be partially checked
            brushFormGroup.jumpDrawablesToCurrentState();
        }

        brushFormGroup.setOnCheckedChangeListener((group, checked) -> {
            switch (checked) {
                case R.id.brush_form_free:
                    mCallback.getDrawingView().setDrawMode(DrawingView.DrawMode.FREE);
                    break;

                case R.id.brush_form_line:
                    mCallback.getDrawingView().setDrawMode(DrawingView.DrawMode.LINES);
                    break;
            }
        });
    }

    private void initializeBrushColors(View view) {
        view.findViewById(R.id.color_more).setOnClickListener(view1 -> showHSVColorPopup());

        RecyclerView rv = view.findViewById(R.id.color_recycler);
        rv.setHasFixedSize(true);

        Context context = getContext();
        if (context == null) return;

        mColorAdapter = new ColorAdapter(Arrays.asList(
                ContextCompat.getColor(context, R.color.palette_0),
                ContextCompat.getColor(context, R.color.palette_1),
                ContextCompat.getColor(context, R.color.palette_2),
                ContextCompat.getColor(context, R.color.palette_3),
                ContextCompat.getColor(context, R.color.palette_4),
                ContextCompat.getColor(context, R.color.palette_5),
                ContextCompat.getColor(context, R.color.palette_6),
                ContextCompat.getColor(context, R.color.palette_7),
                ContextCompat.getColor(context, R.color.palette_8),
                ContextCompat.getColor(context, R.color.palette_9),
                ContextCompat.getColor(context, R.color.palette_10),
                ContextCompat.getColor(context, R.color.palette_11),
                ContextCompat.getColor(context, R.color.palette_13),
                ContextCompat.getColor(context, R.color.palette_14)),
                new ColorAdapter.ColorAdapterListener() {
                    @Override
                    public void onItemClick(int color) {
                        mCallback.getDrawingView().setColor(color);
                    }

                    @Override
                    public int getCurrentColor() {
                        return mCallback.getDrawingView().getColor();
                    }
                });

        rv.setAdapter(mColorAdapter);
    }

    private void showHSVColorPopup() {
        mHSVColorPopup.setColor(mCallback.getDrawingView().getColor());
        mHSVColorPopup.showAtLocation(mNestedScrollView, Gravity.CENTER, 0, 0);
    }

    public interface ToolsCallback {
        DrawingView getDrawingView();
    }

}

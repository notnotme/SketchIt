package com.notnotme.sketchup.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.view.drawing.DrawingView;

public class ToolsFragment extends BaseFragment {

    private NestedScrollView mNestedScrollView;
    private ToolsCallback mCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNestedScrollView = view.findViewById(R.id.scroll);

        SeekBar seekBar = view.findViewById(R.id.stroke_width);
        seekBar.setProgress((int) mCallback.getDrawingView().getStrokeWidth());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mCallback.getDrawingView().setStrokeWidth(2+i);
            }
        });

        Context context = getContext();
        if (context != null) {
            view.findViewById(R.id.border).setBackgroundResource(
                    getSettingsManager().getTheme().getColorPrimary());
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

    public interface ToolsCallback {
        DrawingView getDrawingView();
    }

}

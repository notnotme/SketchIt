package com.notnotme.sketchup.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.view.drawing.DrawingView;
import com.notnotme.sketchup.view.drawing.Effect;

public class ToolsFragment extends Fragment {

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
        void setDrawMode(DrawingView.DrawMode mode, float width);

        void setCurrentEffect(Effect effect);

        void setPaintColor(int color);
    }

}

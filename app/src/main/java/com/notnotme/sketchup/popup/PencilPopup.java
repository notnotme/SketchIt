package com.notnotme.sketchup.popup;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.view.drawing.DrawingView;

import java.util.Arrays;

public final class PencilPopup extends PopupWindow {

    private PopupListener mPopupListener;
    private PencilAdapter mPenAdapter;
    private PencilAdapter mLineAdapter;
    private PencilAdapter mEffectAdapter;

    private PencilAdapter.PencilAdapterListener mPencilAdapterListener =
            new PencilAdapter.PencilAdapterListener() {
                @Override
                public void onItemClick(PencilAdapter adapter, PencilAdapter.Item item) {
                    switch (item.getIconRes()) {
                        case R.mipmap.pen_small:
                            mPopupListener.setDrawMode(DrawingView.DrawMode.FREE, DrawingView.STROKE_DEFAULT_SIZE - 10);
                            mLineAdapter.setSelected(-1);
                            mLineAdapter.notifyDataSetChanged();
                            break;
                        case R.mipmap.pen_medium:
                            mPopupListener.setDrawMode(DrawingView.DrawMode.FREE, DrawingView.STROKE_DEFAULT_SIZE);
                            mLineAdapter.setSelected(-1);
                            mLineAdapter.notifyDataSetChanged();
                            break;
                        case R.mipmap.pen_large:
                            mPopupListener.setDrawMode(DrawingView.DrawMode.FREE, DrawingView.STROKE_DEFAULT_SIZE + 10);
                            mLineAdapter.setSelected(-1);
                            mLineAdapter.notifyDataSetChanged();
                            break;
                        case R.mipmap.line_small:
                            mPopupListener.setDrawMode(DrawingView.DrawMode.LINES, DrawingView.STROKE_DEFAULT_SIZE - 10);
                            mPenAdapter.setSelected(-1);
                            mPenAdapter.notifyDataSetChanged();
                            break;
                        case R.mipmap.line_medium:
                            mPopupListener.setDrawMode(DrawingView.DrawMode.LINES, DrawingView.STROKE_DEFAULT_SIZE);
                            mPenAdapter.setSelected(-1);
                            mPenAdapter.notifyDataSetChanged();
                            break;
                        case R.mipmap.line_large:
                            mPopupListener.setDrawMode(DrawingView.DrawMode.LINES, DrawingView.STROKE_DEFAULT_SIZE + 10);
                            mPenAdapter.setSelected(-1);
                            mPenAdapter.notifyDataSetChanged();
                            break;
                        case R.mipmap.style_line:
                            mPopupListener.setCurrentEffect(null);
                            break;

                        case R.mipmap.style_dash:
                            mPopupListener.setCurrentEffect(new DashPathEffect(new float[]{50, 50}, 0));
                            break;

                        case R.mipmap.style_dot:
                            mPopupListener.setCurrentEffect(new DashPathEffect(new float[]{0, 40}, 0));
                            break;
                    }
                }
            };

    public PencilPopup(Context context, PopupListener popupListener) {
        super(View.inflate(context, R.layout.popup_pencil, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        mPopupListener = popupListener;

        mPenAdapter = new PencilAdapter(Arrays.asList(
                new PencilAdapter.Item(R.mipmap.pen_small),
                new PencilAdapter.Item(R.mipmap.pen_medium),
                new PencilAdapter.Item(R.mipmap.pen_large)),
                getCurrentPenSelected(),
                mPencilAdapterListener);

        View layout = getContentView();
        RecyclerView rcPen = layout.findViewById(R.id.recycler_pen);
        rcPen.setHasFixedSize(true);
        rcPen.setAdapter(mPenAdapter);


        mLineAdapter = new PencilAdapter(Arrays.asList(
                new PencilAdapter.Item(R.mipmap.line_small),
                new PencilAdapter.Item(R.mipmap.line_medium),
                new PencilAdapter.Item(R.mipmap.line_large)),
                getCurrentLineSelected(),
                mPencilAdapterListener);

        RecyclerView rcLine = layout.findViewById(R.id.recycler_line);
        rcLine.setHasFixedSize(true);
        rcLine.setAdapter(mLineAdapter);


        mEffectAdapter = new PencilAdapter(Arrays.asList(
                new PencilAdapter.Item(R.mipmap.style_line),
                new PencilAdapter.Item(R.mipmap.style_dot),
                new PencilAdapter.Item(R.mipmap.style_dash)),
                popupListener.getCurrentEffect() != null ? 1 : -1,
                mPencilAdapterListener);

        RecyclerView rcStyle = layout.findViewById(R.id.recycler_style);
        rcStyle.setHasFixedSize(true);
        rcStyle.setAdapter(mEffectAdapter);

        setAnimationStyle(android.R.style.Animation_Dialog);
        setFocusable(true);
        setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));
    }

    private int getCurrentPenSelected() {
        if (mPopupListener.getCurrentDrawMode() != DrawingView.DrawMode.FREE) return -1;

        if (mPopupListener.getBrushWidth() > DrawingView.STROKE_DEFAULT_SIZE) return 2;
        else if (mPopupListener.getBrushWidth() == DrawingView.STROKE_DEFAULT_SIZE) return 1;
        else return 0;
    }

    private int getCurrentLineSelected() {
        if (mPopupListener.getCurrentDrawMode() != DrawingView.DrawMode.LINES) return -1;

        if (mPopupListener.getBrushWidth() > DrawingView.STROKE_DEFAULT_SIZE) return 2;
        else if (mPopupListener.getBrushWidth() == DrawingView.STROKE_DEFAULT_SIZE) return 1;
        else return 0;
    }

    public interface PopupListener {
        void setDrawMode(DrawingView.DrawMode mode, float width);

        void setCurrentEffect(PathEffect effect);

        PathEffect getCurrentEffect();

        DrawingView.DrawMode getCurrentDrawMode();

        float getBrushWidth();
    }

}
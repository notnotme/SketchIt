package com.notnotme.sketchup.popup;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.notnotme.sketchup.R;

import java.util.Arrays;

public final class ColorPopup extends PopupWindow {

    private PopupListener mPopupListener;

    public ColorPopup(Context context, PopupListener popupListener) {
        super(View.inflate(context, R.layout.popup_color, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        View layout = getContentView();
        layout.findViewById(R.id.more).setOnClickListener(view -> mPopupListener.moreColor());

        RecyclerView rv = layout.findViewById(R.id.recycler);
        rv.setHasFixedSize(true);
        rv.setAdapter(new ColorAdapter(
                Arrays.asList(context.getResources().getStringArray(R.array.colors_array)), new ColorAdapter.ColorAdapterListener() {
            @Override
            public void onItemClick(int color) {
                mPopupListener.setColor(color);
            }

            @Override
            public int getCurrentColor() {
                return mPopupListener.getCurrentColor();
            }
        }));

        setAnimationStyle(android.R.style.Animation_Dialog);
        setFocusable(true);
        setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));

        mPopupListener = popupListener;
    }

    public interface PopupListener {
        void setColor(int color);

        void moreColor();

        int getCurrentColor();
    }

}

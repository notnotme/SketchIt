package com.notnotme.sketchup.popup;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.notnotme.sketchup.R;

import java.util.Arrays;

public final class ColorPopup extends BasePopup {

    private PopupListener mPopupListener;
    private ColorAdapter mColorAdapter;

    public ColorPopup(Context context, PopupListener popupListener) {
        super(context, R.layout.popup_color);
        mPopupListener = popupListener;

        View layout = getContentView();
        layout.findViewById(R.id.more).setOnClickListener(view -> mPopupListener.moreColor());

        RecyclerView rv = layout.findViewById(R.id.recycler);
        rv.setHasFixedSize(true);

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
                ContextCompat.getColor(context, R.color.palette_12),
                ContextCompat.getColor(context, R.color.palette_13),
                ContextCompat.getColor(context, R.color.palette_14)),
                new ColorAdapter.ColorAdapterListener() {
                    @Override
                    public void onItemClick(int color) {
                        mPopupListener.setColor(color);
                    }

                    @Override
                    public int getCurrentColor() {
                        return mPopupListener.getCurrentColor();
                    }
                });

        rv.setAdapter(mColorAdapter);
    }

    public void notifyColorChanged() {
        mColorAdapter.notifyDataSetChanged();
    }

    public interface PopupListener {
        void setColor(int color);

        void moreColor();

        int getCurrentColor();
    }

}

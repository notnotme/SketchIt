package com.notnotme.sketchup.popup;

import android.content.Context;
import android.view.View;

import com.notnotme.sketchup.R;

public final class MainMenuPopup extends BasePopup {

    private PopupListener mPopupListener;

    public MainMenuPopup(Context context, PopupListener popupListener) {
        super(context, R.layout.popup_plus);
        mPopupListener = popupListener;

        View layout = getContentView();
        layout.findViewById(R.id.btn_new).setOnClickListener(v -> mPopupListener.newSketch());
        layout.findViewById(R.id.btn_save).setOnClickListener(v -> mPopupListener.saveSketch());
        layout.findViewById(R.id.btn_import).setOnClickListener(view -> mPopupListener.importSketch());
        layout.findViewById(R.id.btn_share).setOnClickListener(view -> mPopupListener.shareSketch());
    }

    public interface PopupListener {
        void newSketch();

        void saveSketch();

        void importSketch();

        void shareSketch();
    }

}

package com.notnotme.sketchup.popup;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.notnotme.sketchup.R;

public final class MainMenuPopup extends PopupWindow {

    private PopupListener mPopupListener;

    public MainMenuPopup(Context context, PopupListener popupListener) {
        super(View.inflate(context, R.layout.popup_plus, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        View layout = getContentView();
        layout.findViewById(R.id.btn_new).setOnClickListener(v -> mPopupListener.newSketch());
        layout.findViewById(R.id.btn_save).setOnClickListener(v -> mPopupListener.saveSketch());
        layout.findViewById(R.id.btn_import).setOnClickListener(view -> mPopupListener.importSketch());

        setAnimationStyle(android.R.style.Animation_Dialog);
        setFocusable(true);
        setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));

        mPopupListener = popupListener;
    }

    public interface PopupListener {
        void newSketch();

        void saveSketch();

        void importSketch();
    }

}

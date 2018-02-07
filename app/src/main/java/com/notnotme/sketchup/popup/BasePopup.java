package com.notnotme.sketchup.popup;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

abstract class BasePopup extends PopupWindow {

    BasePopup(Context context, @LayoutRes int layout) {
        super(View.inflate(context, layout, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setAnimationStyle(android.R.style.Animation_Dialog);
        setFocusable(true);
        setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));
    }

}

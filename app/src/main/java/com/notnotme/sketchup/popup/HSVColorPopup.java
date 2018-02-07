package com.notnotme.sketchup.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.notnotme.sketchup.R;
import com.notnotme.sketchup.view.RatioTouchListener;

public final class HSVColorPopup extends BasePopup {

    private final ImageView mColorPreview;
    private final ImageView mColorImage;
    private PopupListener mPopupListener;
    private float mTempHSV[];

    public HSVColorPopup(Context context, PopupListener popupListener) {
        super(context, R.layout.popup_hsv);
        mPopupListener = popupListener;

        View layout = getContentView();
        mColorPreview = layout.findViewById(R.id.color_preview);
        mColorImage = layout.findViewById(R.id.hue_mask);
        ImageView hueImage = layout.findViewById(R.id.hue_color);
        ImageView hueSelector = layout.findViewById(R.id.hue_selector);
        ImageView colorSelector = layout.findViewById(R.id.color_selector);
        Bitmap hueBitmap = ((BitmapDrawable) hueImage.getDrawable()).getBitmap();

        mTempHSV = new float[3];

        layout.findViewById(R.id.hue_container).setOnTouchListener(new RatioTouchListener() {
            @Override
            public boolean onTouch(View view, float x, float y, float ratioX, float ratioY) {
                hueSelector.animate()
                        .y(y - hueSelector.getHeight() / 2)
                        .setDuration(0)
                        .start();

                int bitmapOffsetY = (int) ((hueBitmap.getHeight() - 1) * ratioY);
                int hueValue = hueBitmap.getPixel(hueBitmap.getWidth() / 2, bitmapOffsetY);
                mTempHSV[0] = ratioY * 360.0f;

                mColorImage.setColorFilter(hueValue, PorterDuff.Mode.MULTIPLY);
                mColorPreview.setColorFilter(Color.HSVToColor(mTempHSV), PorterDuff.Mode.SRC);
                return true;
            }
        });

        layout.findViewById(R.id.color_container).setOnTouchListener(new RatioTouchListener() {
            @Override
            public boolean onTouch(View view, float x, float y, float ratioX, float ratioY) {
                colorSelector.animate()
                        .x(x - colorSelector.getWidth() / 2)
                        .y(y - colorSelector.getHeight() / 2)
                        .setDuration(0)
                        .start();

                mTempHSV[2] = ratioX;
                mTempHSV[1] = 1f - ratioY;
                mColorPreview.setColorFilter(Color.HSVToColor(mTempHSV), PorterDuff.Mode.SRC);
                return true;
            }
        });

        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float hueStep = (float) hueBitmap.getHeight() / 360f;
                float offsetY = ((mTempHSV[0] / 360.0f) * hueImage.getHeight()) / hueStep;


                int bitmapOffsetY = (int) ((hueBitmap.getHeight() - 1) * (mTempHSV[0] / 360.0f));
                int hueValue = hueBitmap.getPixel(hueBitmap.getWidth() / 2, bitmapOffsetY);
                mColorImage.setColorFilter(hueValue, PorterDuff.Mode.MULTIPLY);

                hueSelector.animate()
                        .y(offsetY * hueStep - (hueSelector.getHeight() / 2))
                        .alpha(1)
                        .setDuration(0);

                colorSelector.animate()
                        .x((mTempHSV[2] * mColorImage.getWidth()) - (colorSelector.getWidth() / 2))
                        .y(((1f - mTempHSV[1]) * mColorImage.getHeight()) - (colorSelector.getHeight() / 2))
                        .alpha(1)
                        .setDuration(0);
            }
        });

        setOnDismissListener(() -> mPopupListener.setColor(Color.HSVToColor(mTempHSV)));
    }

    public void setColor(int color) {
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), mTempHSV);
        mColorPreview.setColorFilter(Color.HSVToColor(mTempHSV), PorterDuff.Mode.SRC);
    }

    public interface PopupListener {
        void setColor(int color);
    }

}

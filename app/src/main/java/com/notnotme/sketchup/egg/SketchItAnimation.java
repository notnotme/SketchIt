package com.notnotme.sketchup.egg;

import com.notnotme.sketchup.egg.renderer.sprite.AnimatedSprite;
import com.notnotme.sketchup.egg.util.BounceAnimation;

import java.util.concurrent.TimeUnit;

public final class SketchItAnimation extends BounceAnimation<AnimatedSprite> {

    private int mFirstLetterIndex;
    private int mSecondLetterIndex;

    public SketchItAnimation() {
        super(6.0f, 0.3f, TimeUnit.SECONDS.toMillis(1)/2);
        mFirstLetterIndex = 0;
        mSecondLetterIndex = -1;
    }

    @Override
    public void loop() {
        mFirstLetterIndex = (int) (Math.random() * 8);
        mSecondLetterIndex = (int) (Math.random() * 5) == 1 ? (int) (Math.random() * 8) : -1;
    }

    @Override
    public void apply(AnimatedSprite sprite) {
        if (sprite.getFrameIndex() == mFirstLetterIndex || sprite.getFrameIndex() == mSecondLetterIndex) {
            float value = 2.0f + getValue();
            sprite.scaleX = value;
            sprite.scaleY = value;

        }else {
            sprite.scaleX = 2.0f;
            sprite.scaleY = 2.0f;
        }
    }

}

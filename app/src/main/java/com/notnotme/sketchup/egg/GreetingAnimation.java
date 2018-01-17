package com.notnotme.sketchup.egg;

import com.notnotme.sketchup.egg.renderer.sprite.AnimatedSprite;
import com.notnotme.sketchup.egg.util.BounceAnimation;

import java.util.concurrent.TimeUnit;

public final class GreetingAnimation extends BounceAnimation<AnimatedSprite> {

    public GreetingAnimation() {
        super(12.0f, 0.3f, TimeUnit.SECONDS.toMillis(1)/2);
    }

    @Override
    public long getNextStart() {
        return (long) (TimeUnit.SECONDS.toMillis(1) * 1.5f);
    }

    @Override
    public void apply(AnimatedSprite sprite) {
        if (sprite.getFrameIndex() == '\u0003') {
            float value = 2.0f + getValue();
            sprite.scaleX = value;
            sprite.scaleY = value;
            sprite.color.r = 1f;
            sprite.color.g = 0f;
            sprite.color.b = 0f;
            sprite.color.a = 1f;
        }else {
            sprite.scaleX = 2.0f;
            sprite.scaleY = 2.0f;
            sprite.color.r = 1f;
            sprite.color.g = 1f;
            sprite.color.b = 1f;
            sprite.color.a = 1f;
        }
    }

}

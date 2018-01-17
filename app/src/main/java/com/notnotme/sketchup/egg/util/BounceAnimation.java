package com.notnotme.sketchup.egg.util;

/**
 * Mostly from https://gist.github.com/Kraiden/1390f1ac52a04e08b83e2d8b5a408aca
 * @param <T> The object type managed by the class
 */
public abstract class BounceAnimation<T> {

    private long mDuration;
    private long mStartTime;
    private long mNextStart;
    private boolean mStarted;
    private float mEnergy;
    private float mBounce;
    private float mValue;

    public BounceAnimation(float energy, float bounce, long duration) {
        mDuration = duration;
        mEnergy = energy;
        mBounce = bounce;
        mNextStart = 0;
    }

    public void compute(long time) {
        if (!mStarted && mNextStart <= time) {
            mStarted = true;
            mStartTime = time;
            mNextStart = time + getNextStart();
        }

        if (mStarted) {
            float normalizedTime = (float) (time - mStartTime) / mDuration;

            normalizedTime *= 1.1226f;
            if (normalizedTime <= 1.0f) {
                // 2 + (1 + -abs(cos(time * 10 * 1/PI)) + getCurveAdjustment(time)) * mBounce
                mValue = (float) (1d + (-Math.abs(Math.cos(normalizedTime * 3.183098862f)) * getCurveAdjustment(normalizedTime))) * mBounce;
            } else {
                mValue = 0.0f;
                mStarted = false;
                loop();
            }
        }
    }

    private double getCurveAdjustment(double t) {
        return -(2 * (1 - t) * t * mEnergy + t * t) + 1;
    }

    public float getValue() {
        return mValue;
    }

    public long getNextStart() {
        return mDuration;
    }

    public void loop() {
    }

    public abstract void apply(T object);

}

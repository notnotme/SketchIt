package com.notnotme.sketchup.egg.renderer.sprite;

public class AnimatedSprite extends Sprite {

	private int mFrameIndex;
	private UV[] mFrames;

	public AnimatedSprite(int width, int height, UV[] frames) {
		super(width, height);
		mFrames = frames;
		setFrame(0);
	}

	public void setFrame(int idx) {
		if (idx < 0 || idx > mFrames.length) {
			throw new IllegalArgumentException("AnimatedSprite frame is out of bounds");
		}

		mFrameIndex = idx;
		uv.s = mFrames[idx].s;
		uv.t = mFrames[idx].t;
		uv.u = mFrames[idx].u;
		uv.v = mFrames[idx].v;
	}

	public int getFrameIndex() {
		return mFrameIndex;
	}

	public UV[] getFrames() {
		return mFrames;
	}

}

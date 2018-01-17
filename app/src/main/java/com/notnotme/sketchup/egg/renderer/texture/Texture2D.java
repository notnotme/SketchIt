package com.notnotme.sketchup.egg.renderer.texture;

import android.content.Context;
import android.opengl.GLES20;

import com.notnotme.sketchup.egg.renderer.OpenGLRenderer;

public final class Texture2D {

	private boolean mDisposed;
	private int mTexture;
	private int width;
	private int height;

	public Texture2D(Context context, String path) throws Exception {
		int[] texture = OpenGLRenderer.loadTexture(context, path);
		mTexture = texture[0];
		width = texture[1];
		height = texture[2];
	}

	public void bind(int n) {
		if(mDisposed) {
			throw new IllegalStateException("Disposed Texture2D can't be reused");
		}

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0+n);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
	}

	public void dispose() {
		mDisposed = true;
		int[] texture = {mTexture};
		GLES20.glDeleteTextures(1, texture, 0);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}

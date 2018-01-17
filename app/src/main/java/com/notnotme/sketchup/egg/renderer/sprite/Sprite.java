package com.notnotme.sketchup.egg.renderer.sprite;

public class Sprite {

	public float x;
	public float y;
	public int w;
	public int h;
	public float scaleX;
	public float scaleY;
	public float rotation;

	public Color color;
	public UV uv;

	public Sprite(int width, int height) {
		x = y = 0f;
		rotation = 0f;
		scaleX = scaleY = 1f;
		w = width;
		h = height;
		color = new Color();
		uv = new UV();
	}

	public static class Color {
		public float r;
		public float g;
		public float b;
		public float a;

		public Color() {
			r = g = b = a = 1f;
		}
	}

	public static class UV {
		public float s;
		public float t;
		public float u;
		public float v;

		public UV() {
			s = t = 0f;
			u = v = 1f;
		}
	}

}

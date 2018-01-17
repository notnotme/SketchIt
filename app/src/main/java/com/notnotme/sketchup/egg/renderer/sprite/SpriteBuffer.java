package com.notnotme.sketchup.egg.renderer.sprite;

import android.opengl.GLES20;

import com.notnotme.sketchup.egg.renderer.OpenGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class SpriteBuffer {

    private static final int COORDS_PER_VERTEX = 2;
    private static final int TEXTURE_PER_VERTEX = 2;
    private static final int COLOR_PER_VERTEX = 4;
    private static final int ROTATE_PER_VERTEX = 1;
    private static final int SCALE_PER_VERTEX = 2;
    private static final int TRANSLATE_PER_VERTEX = 2;
    private static final int INDICES_PER_SPRITE = 6;
    private static final int COLOR_STRIDE = COLOR_PER_VERTEX * 4;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    private static final int TEXTURE_STRIDE = TEXTURE_PER_VERTEX * 4;
    private static final int TRANSLATE_STRIDE = TRANSLATE_PER_VERTEX * 4;
    private static final int SCALE_STRIDE = SCALE_PER_VERTEX * 4;
    private static final int ROTATE_STRIDE = ROTATE_PER_VERTEX * 4;

    private static final String vertexShaderSource =
                  "uniform mat4 u_projTrans;" +
                  "attribute vec2 vPosition;" +
                  "attribute vec4 vColor;" +
                  "attribute vec2 vTexture0;" +
                  "attribute vec2 vTranslate;" +
                  "attribute vec2 vScale;" +
                  "attribute float vRotate;" +
                  "varying vec4 v_color;" +
                  "varying vec2 v_texCoords;" +
                  "void main() {" +
                  "    v_color     = vColor;" +
                  "    v_texCoords = vTexture0;" +
                  "    mat3 scale_mat = mat3" +
                  "        (0.5*vScale.x,  0.0,            0.0," +
                  "         0.0,           0.5*vScale.y,   0.0," +
                  "         0.0,           0.0,            1.0);" +
                  "    mat3 rotate_mat = mat3" +
                  "        (cos(vRotate), sin(vRotate), 0.0," +
                  "        -sin(vRotate), cos(vRotate), 0.0," +
                  "         0.0,          0.0,          1.0);" +
                  "    mat4 translate_mat = mat4" +
                  "        (1.0,          0.0,          0.0, 0.0," +
                  "         0.0,          1.0,          0.0, 0.0," +
                  "         0.0,          0.0,          1.0, 0.0," +
                  "    vTranslate.x, vTranslate.y, 0.0, 1.0);" +
                  "    vec3 xformed = rotate_mat * scale_mat * vec3(vPosition.xy, 0.0);" +
                  "    gl_Position  = u_projTrans * translate_mat * vec4(xformed, 1.0);" +
                  "}";

    private static final String fragmentShaderSource =
                  "precision mediump float;" //
                + "varying vec2 v_texCoords;" //
                + "varying vec4 v_color;"
                + "uniform sampler2D u_texture;" //
                + "void main()"//
                + "{" //
                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);" //
                + "}";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mColorBuffer;
    private FloatBuffer mRotateBuffer;
    private FloatBuffer mScaleBuffer;
    private FloatBuffer mTranslateBuffer;
    private ShortBuffer mIndicesBuffer;
    private boolean mDisposed;
    private int mCapacity;
    private int mCount;

    private int mGLProgram;
    private int mShaderVertexPosition;
    private int mShaderVertexTexture;
    private int mShaderVertexColor;
    private int mShaderVertexRotation;
    private int mShaderVertexScale;
    private int mShaderVertexTranslate;
    private int mShaderMatrixUniform;
    private int mShaderTextureUniform;

    public SpriteBuffer(int capacity) {
        mCapacity = capacity;

        // prepare pixel shader
        int vertexShader = OpenGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = OpenGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
        mGLProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGLProgram, vertexShader);
        GLES20.glAttachShader(mGLProgram, fragmentShader);
        GLES20.glLinkProgram(mGLProgram);

        mShaderMatrixUniform = GLES20.glGetUniformLocation(mGLProgram, "u_projTrans");
        mShaderTextureUniform = GLES20.glGetUniformLocation(mGLProgram, "u_texture");
        mShaderVertexPosition = GLES20.glGetAttribLocation(mGLProgram, "vPosition");
        mShaderVertexColor = GLES20.glGetAttribLocation(mGLProgram, "vColor");
        mShaderVertexTexture = GLES20.glGetAttribLocation(mGLProgram, "vTexture0");
        mShaderVertexTranslate = GLES20.glGetAttribLocation(mGLProgram, "vTranslate");
        mShaderVertexScale = GLES20.glGetAttribLocation(mGLProgram, "vScale");
        mShaderVertexRotation = GLES20.glGetAttribLocation(mGLProgram, "vRotate");

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer vbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (COORDS_PER_VERTEX * 4) * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();

        // initialize texture byte buffer for shape coordinates
        ByteBuffer tbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (TEXTURE_PER_VERTEX * 4) * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTextureBuffer = tbb.asFloatBuffer();

        // initialize texture byte buffer for shape coordinates
        ByteBuffer cbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (COLOR_PER_VERTEX * 4) * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();

        // initialize texture byte buffer for shape coordinates
        ByteBuffer abb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (ROTATE_PER_VERTEX * 4) * 4);
        abb.order(ByteOrder.nativeOrder());
        mRotateBuffer = abb.asFloatBuffer();

        // initialize texture byte buffer for shape coordinates
        ByteBuffer sbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (SCALE_PER_VERTEX * 4) * 4);
        sbb.order(ByteOrder.nativeOrder());
        mScaleBuffer = sbb.asFloatBuffer();

        // initialize texture byte buffer for shape coordinates
        ByteBuffer pbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (TRANSLATE_PER_VERTEX * 4) * 4);
        pbb.order(ByteOrder.nativeOrder());
        mTranslateBuffer = pbb.asFloatBuffer();

        // initialize byte buffer for the draw list
        ByteBuffer ibb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                capacity * (INDICES_PER_SPRITE * 2));
        ibb.order(ByteOrder.nativeOrder());
        mIndicesBuffer = ibb.asShortBuffer();

        // Prepare the indices buffer (it will never change)
        int size = capacity*INDICES_PER_SPRITE;
        short j = 0;
        for (int i=0; i<size; i+=6, j+=4) {
            mIndicesBuffer.put(j);
            mIndicesBuffer.put((short)(j + 1));
            mIndicesBuffer.put((short)(j + 2));
            mIndicesBuffer.put(j);
            mIndicesBuffer.put((short)(j + 2));
            mIndicesBuffer.put((short)(j + 3));
        }
        mIndicesBuffer.position(0);
    }

    public void dispose() {
        mVertexBuffer.clear();
        mTextureBuffer.clear();
        mColorBuffer.clear();
        mIndicesBuffer.clear();
        mRotateBuffer.clear();
        mScaleBuffer.clear();
        mTranslateBuffer.clear();
        mDisposed = true;
    }

    public void prepare() {
        if (mDisposed) {
            throw new IllegalStateException("Disposed SpriteBuffer can't be reused");
        }

        mCount = 0;
    }

    public void put(Sprite sprite) {
        if (mDisposed) {
            throw new IllegalStateException("Disposed SpriteBuffer can't be reused");
        }

        if (mCount >= mCapacity) {
            throw new IllegalStateException("Please grow the SpriteBatch capacity");
        }

        final float u = sprite.uv.s;
        final float v = sprite.uv.v;
        final float u2 = sprite.uv.u;
        final float v2 = sprite.uv.t;

        float rotation = (float) Math.toRadians(sprite.rotation);

        mVertexBuffer.put(-sprite.w);
        mVertexBuffer.put(-sprite.h);
        mTextureBuffer.put(u2);
        mTextureBuffer.put(v);
        mColorBuffer.put(sprite.color.r);
        mColorBuffer.put(sprite.color.g);
        mColorBuffer.put(sprite.color.b);
        mColorBuffer.put(sprite.color.a);
        mRotateBuffer.put(rotation);
        mScaleBuffer.put(sprite.scaleX);
        mScaleBuffer.put(sprite.scaleY);
        mTranslateBuffer.put(sprite.x);
        mTranslateBuffer.put(sprite.y);

        mVertexBuffer.put(-sprite.w);
        mVertexBuffer.put(sprite.h);
        mTextureBuffer.put(u2);
        mTextureBuffer.put(v2);
        mColorBuffer.put(sprite.color.r);
        mColorBuffer.put(sprite.color.g);
        mColorBuffer.put(sprite.color.b);
        mColorBuffer.put(sprite.color.a);
        mRotateBuffer.put(rotation);
        mScaleBuffer.put(sprite.scaleX);
        mScaleBuffer.put(sprite.scaleY);
        mTranslateBuffer.put(sprite.x);
        mTranslateBuffer.put(sprite.y);

        mVertexBuffer.put(sprite.w);
        mVertexBuffer.put(sprite.h);
        mTextureBuffer.put(u);
        mTextureBuffer.put(v2);
        mColorBuffer.put(sprite.color.r);
        mColorBuffer.put(sprite.color.g);
        mColorBuffer.put(sprite.color.b);
        mColorBuffer.put(sprite.color.a);
        mRotateBuffer.put(rotation);
        mScaleBuffer.put(sprite.scaleX);
        mScaleBuffer.put(sprite.scaleY);
        mTranslateBuffer.put(sprite.x);
        mTranslateBuffer.put(sprite.y);

        mVertexBuffer.put(sprite.w);
        mVertexBuffer.put(-sprite.h);
        mTextureBuffer.put(u);
        mTextureBuffer.put(v);
        mColorBuffer.put(sprite.color.r);
        mColorBuffer.put(sprite.color.g);
        mColorBuffer.put(sprite.color.b);
        mColorBuffer.put(sprite.color.a);
        mRotateBuffer.put(rotation);
        mScaleBuffer.put(sprite.scaleX);
        mScaleBuffer.put(sprite.scaleY);
        mTranslateBuffer.put(sprite.x);
        mTranslateBuffer.put(sprite.y);

        mCount++;
    }

    public void draw(float[] mvp, int texture) {
        if (mDisposed) {
            throw new IllegalStateException("Disposed SpriteBuffer can't be reused");
        }

        mVertexBuffer.position(0);
        mTextureBuffer.position(0);
        mColorBuffer.position(0);
        mRotateBuffer.position(0);
        mScaleBuffer.position(0);
        mTranslateBuffer.position(0);

        GLES20.glUseProgram(mGLProgram);
        GLES20.glUniformMatrix4fv(mShaderMatrixUniform, 1, false, mvp, 0);
        GLES20.glUniform1i(mShaderTextureUniform, texture);

        GLES20.glEnableVertexAttribArray(mShaderVertexPosition);
        GLES20.glEnableVertexAttribArray(mShaderVertexTexture);
        GLES20.glEnableVertexAttribArray(mShaderVertexColor);
        GLES20.glEnableVertexAttribArray(mShaderVertexRotation);
        GLES20.glEnableVertexAttribArray(mShaderVertexScale);
        GLES20.glEnableVertexAttribArray(mShaderVertexTranslate);

        GLES20.glVertexAttribPointer(mShaderVertexPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexTexture, TEXTURE_PER_VERTEX, GLES20.GL_FLOAT, false, TEXTURE_STRIDE, mTextureBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexColor, COLOR_PER_VERTEX, GLES20.GL_FLOAT, false, COLOR_STRIDE, mColorBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexRotation, ROTATE_PER_VERTEX, GLES20.GL_FLOAT, false, ROTATE_STRIDE, mRotateBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexScale, SCALE_PER_VERTEX, GLES20.GL_FLOAT, false, SCALE_STRIDE, mScaleBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexTranslate, TRANSLATE_PER_VERTEX, GLES20.GL_FLOAT, false, TRANSLATE_STRIDE, mTranslateBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mCount * INDICES_PER_SPRITE, GLES20.GL_UNSIGNED_SHORT, mIndicesBuffer);

        GLES20.glDisableVertexAttribArray(mShaderVertexPosition);
        GLES20.glDisableVertexAttribArray(mShaderVertexTexture);
        GLES20.glDisableVertexAttribArray(mShaderVertexColor);
        GLES20.glDisableVertexAttribArray(mShaderVertexRotation);
        GLES20.glDisableVertexAttribArray(mShaderVertexScale);
        GLES20.glDisableVertexAttribArray(mShaderVertexTranslate);
    }

}

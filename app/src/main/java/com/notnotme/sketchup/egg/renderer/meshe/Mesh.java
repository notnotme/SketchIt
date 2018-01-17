package com.notnotme.sketchup.egg.renderer.meshe;

import android.opengl.GLES20;

import com.notnotme.sketchup.egg.renderer.OpenGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

// todo: maybe do a MeshBuffer and Mesh(es?) class instead ?
public final class Mesh {

    private static final int COORDS_PER_VERTEX = 3;
    private static final int TEXTURE_PER_VERTEX = 2;
    private static final int COLOR_PER_VERTEX = 4;
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    private static final int TEXTURE_STRIDE = TEXTURE_PER_VERTEX * 4;
    private static final int COLOR_STRIDE = COLOR_PER_VERTEX * 4;

    private static final String vertexShaderSource =
                      "uniform mat4 u_projTrans;"
                    + "attribute vec4 vColor;"
                    + "attribute vec4 vPosition;"
                    + "attribute vec2 vTexture0;"
                    + "varying vec2 v_texCoords;"
                    + "varying vec4 v_color;"
                    + "void main()"
                    + "{"
                    + "   v_color = vColor;"
                    + "   v_texCoords = vTexture0;"
                    + "   gl_Position =  u_projTrans * vPosition;"
                    + "}";
    private static final String fragmentShaderSource =
                      "precision mediump float;"
                    + "varying vec2 v_texCoords;"
                    + "varying vec4 v_color;"
                    + "uniform sampler2D u_texture;"
                    + "void main()"
                    + "{"
                    + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);"
                    + "}";


    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mColorBuffer;
    private boolean mDisposed;
    private int mCapacity;
    private int mCount;

    private int mGLProgram;
    private int mShaderVertexPosition;
    private int mShaderVertexTexture;
    private int mShaderVertexColor;
    private int mShaderMatrixUniform;
    private int mShaderTextureUniform;

    public Mesh(int capacity) {
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
        mShaderVertexTexture = GLES20.glGetAttribLocation(mGLProgram, "vTexture0");
        mShaderVertexColor = GLES20.glGetAttribLocation(mGLProgram, "vColor");

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer vbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (COORDS_PER_VERTEX * 4));
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.position(0);

        // initialize texture byte buffer for shape coordinates
        ByteBuffer tbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (TEXTURE_PER_VERTEX * 4));
        tbb.order(ByteOrder.nativeOrder());
        mTextureBuffer = tbb.asFloatBuffer();
        mTextureBuffer.position(0);

        // initialize color byte buffer for shape coordinates
        ByteBuffer cbb = ByteBuffer.allocateDirect(
                // capacity * ((# of coordinate values) * 4 bytes per float)
                capacity * (COLOR_PER_VERTEX * 4));

        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.position(0);
    }

    public void dispose() {
        mVertexBuffer.clear();
        mTextureBuffer.clear();
        mColorBuffer.clear();
        mDisposed = true;
    }

    public void prepare() {
        if(mDisposed) {
            throw new IllegalStateException("Disposed Mesh can't be reused");
        }

        mCount = 0;
    }

    public void put(float x, float y, float z, float s, float t, float r, float g, float b, float a) {
        if (mDisposed) {
            throw new IllegalStateException("Disposed Mesh can't be reused");
        }

        if (mCount >= mCapacity) {
            throw new IllegalStateException("Please grow the Mesh capacity");
        }

        mVertexBuffer.put(x);
        mVertexBuffer.put(y);
        mVertexBuffer.put(z);
        mTextureBuffer.put(s);
        mTextureBuffer.put(t);
        mColorBuffer.put(r);
        mColorBuffer.put(g);
        mColorBuffer.put(b);
        mColorBuffer.put(a);

        mCount++;
    }

    public void draw(float[] mvp, int texture) {
        if(mDisposed) {
            throw new IllegalStateException("Disposed Mesh can't be reused");
        }

        mVertexBuffer.position(0);
        mTextureBuffer.position(0);
        mColorBuffer.position(0);

        GLES20.glUseProgram(mGLProgram);
        GLES20.glUniformMatrix4fv(mShaderMatrixUniform, 1, false, mvp, 0);
        GLES20.glUniform1i(mShaderTextureUniform, texture);

        GLES20.glEnableVertexAttribArray(mShaderVertexPosition);
        GLES20.glEnableVertexAttribArray(mShaderVertexTexture);
        GLES20.glEnableVertexAttribArray(mShaderVertexColor);

        GLES20.glVertexAttribPointer(mShaderVertexPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexTexture, TEXTURE_PER_VERTEX, GLES20.GL_FLOAT, false, TEXTURE_STRIDE, mTextureBuffer);
        GLES20.glVertexAttribPointer(mShaderVertexColor, COLOR_PER_VERTEX, GLES20.GL_FLOAT, false, COLOR_STRIDE, mColorBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mCount);

        GLES20.glDisableVertexAttribArray(mShaderVertexPosition);
        GLES20.glDisableVertexAttribArray(mShaderVertexTexture);
        GLES20.glDisableVertexAttribArray(mShaderVertexColor);
    }

}

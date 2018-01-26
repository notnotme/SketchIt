package com.notnotme.sketchup.egg.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.notnotme.sketchup.egg.GreetingAnimation;
import com.notnotme.sketchup.egg.SketchupAnimation;
import com.notnotme.sketchup.egg.renderer.meshe.Mesh;
import com.notnotme.sketchup.egg.renderer.sprite.AnimatedSprite;
import com.notnotme.sketchup.egg.renderer.sprite.Sprite;
import com.notnotme.sketchup.egg.renderer.sprite.SpriteBuffer;
import com.notnotme.sketchup.egg.renderer.texture.Texture2D;
import com.notnotme.sketchup.egg.sound.ModulePlayer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class OpenGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = OpenGLRenderer.class.getSimpleName();

    private final static char[][] INTRO_TEXT = {
            "ÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛ".toCharArray(),
            "Û                              Û".toCharArray(),
            "Û Hola!                        Û".toCharArray(),
            "Û                              Û".toCharArray(),
            "Û It's !!M hidden in a random  Û".toCharArray(),
            "Û android application!         Û".toCharArray(),
            "Û                              Û".toCharArray(),
            "Û I wish you the best for 2018 Û".toCharArray(),
            "Û or 20xx if you're late... XD Û".toCharArray(),
            "Û                              Û".toCharArray(),
            "Û \15 hotwires by clawz          Û".toCharArray(),
            "Û                              Û".toCharArray(),
            "Û                          \03\03\03 Û".toCharArray(),
            "Û                              Û".toCharArray(),
            "ÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛÛ".toCharArray()
    };

    private final static char[] GREETINGS =
            "\03 Adoru \03 zerkman \03 gloky \03 rez \03 phz \03 maracuja \03 mooz \03 fra \03 sebt3 \03 LLB \03 pls \03 sam \03 nebneb \03 JFL \03 maxep \03 latortue \03 Mr Den \03 ZeuPiark \03 wullon \03 p0nce \03 Arthie \03 Zorro \03 flure \03 pinkette \03 Damn I'm sorry there are so many people that I could forgot maybe you :( \03\03\03 KIDS,    DON'T SMOKE WEEDS !! \03\03\03".toCharArray();

    private int mScreenWidth;
    private int mScreenHeight;

    private Context mContext;
    private ModulePlayer mModulePlayer;
    private Texture2D mChessTexture;
    private Texture2D mScanlineTexture;
    private Texture2D mBeamTexture;
    private Texture2D mLogoTexture;
    private Texture2D mFontTexture;

    private SpriteBuffer mSpriteBuffer;
    private Sprite mSpriteScanline;
    private Sprite mSpriteCenterBeam;
    private Sprite mOverlaySprite;
    private Sprite mBottomOverlaySprite;
    private Sprite mCenterOverlaySprite;
    private AnimatedSprite mFontSprite;

    private GreetingAnimation mGreetingAnimation;
    private SketchupAnimation mSketchupAnimation;
    private AnimatedSprite mLogoLetterSprite;

    private Mesh mGroundMesh;
    private float mOffsetGround;
    private float mGreetingsOffset;

    private float[] mOrtho;
    private float[] mProj;
    private float[] mView;
    private float[] mCombined;

    private long mCurrentTime;

    public OpenGLRenderer(Context context, ModulePlayer modulePlayer) {
        mContext = context;
        mModulePlayer = modulePlayer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        try {
            mScanlineTexture = new Texture2D(mContext, "scanline.png");
            mChessTexture = new Texture2D(mContext, "chess.png");
            mBeamTexture = new Texture2D(mContext, "beam.png");
            mLogoTexture = new Texture2D(mContext, "sketchit.png");
            mFontTexture = new Texture2D(mContext, "font.png");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        mSpriteBuffer = new SpriteBuffer(2048);
        mSpriteScanline = new Sprite(0,0);
        mSpriteCenterBeam = new Sprite(0,0);
        mOverlaySprite = new Sprite(0,0);
        mBottomOverlaySprite = new Sprite(0,0);
        mCenterOverlaySprite = new Sprite(0,0);
        mFontSprite = new AnimatedSprite(0, 0, buildFontSpriteCoords());
        mLogoLetterSprite = new AnimatedSprite(0,0, buildLogoSpriteCoords()); // sketchup = 8 letters

        mGroundMesh = new Mesh(6);

        mSketchupAnimation = new SketchupAnimation();
        mGreetingAnimation = new GreetingAnimation();

        mOrtho = new float[16];
        mProj = new float[16];
        mView = new float[16];
        mCombined = new float[16];

        GLES20.glCullFace(GLES20.GL_BACK);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mScreenWidth = width;
        mScreenHeight = height;

        mOverlaySprite.x = width * 0.5f;
        mOverlaySprite.y = height * 0.5f;
        mOverlaySprite.w = mScreenWidth;
        mOverlaySprite.h = mScreenHeight;
        mOverlaySprite.uv.s = 0.5f;
        mOverlaySprite.uv.t = 0.5f;

        mSpriteScanline.x = width * 0.5f;
        mSpriteScanline.y = height * 0.5f;
        mSpriteScanline.w = width;
        mSpriteScanline.h = height;
        mSpriteScanline.color.r = 0f;
        mSpriteScanline.color.g = 0f;
        mSpriteScanline.color.b = 0f;
        mSpriteScanline.color.a = 1f;
        mSpriteScanline.uv.v = height/mScanlineTexture.getHeight();

        mSpriteCenterBeam.y = height * 0.5f;
        mSpriteCenterBeam.x = width * 0.5f;
        mSpriteCenterBeam.w = (int) (width * 2f);
        mSpriteCenterBeam.h = (int) (height * 0.1f);
        mSpriteCenterBeam.rotation = 40.0f;

        mLogoLetterSprite.w = (int) (width * 0.1f);
        mLogoLetterSprite.h = mLogoLetterSprite.w;
        mLogoLetterSprite.y = mScreenHeight / 8;
        mLogoLetterSprite.scaleX = 2.0f;
        mLogoLetterSprite.scaleY = 2.0f;

        mFontSprite.w = mScreenWidth / 34;
        mFontSprite.h = mFontSprite.w;

        mBottomOverlaySprite.w = mScreenWidth;
        mBottomOverlaySprite.h = (int) (4.0f * mFontSprite.h);
        mBottomOverlaySprite.x = mScreenWidth * 0.5f;
        mBottomOverlaySprite.y = mScreenHeight - (mBottomOverlaySprite.h * 0.5f);
        mBottomOverlaySprite.uv.s = 0.5f;
        mBottomOverlaySprite.uv.t = 0.5f;
        mBottomOverlaySprite.color.r = 0.33f;
        mBottomOverlaySprite.color.g = 0.33f;
        mBottomOverlaySprite.color.b = 0.33f;
        mBottomOverlaySprite.color.a = 0.66f;

        mCenterOverlaySprite.w = mScreenWidth - (int) (mFontSprite.w * 3f);
        mCenterOverlaySprite.h = 14 * mFontSprite.h;
        mCenterOverlaySprite.x = mScreenWidth * 0.5f;
        mCenterOverlaySprite.y = mScreenHeight * 0.5f;
        mCenterOverlaySprite.uv.s = 0.5f;
        mCenterOverlaySprite.uv.t = 0.5f;
        mCenterOverlaySprite.color.r = 0.33f;
        mCenterOverlaySprite.color.g = 0.33f;
        mCenterOverlaySprite.color.b = 0.33f;
        mCenterOverlaySprite.color.a = 0.66f;

        mCurrentTime = System.currentTimeMillis();
        mGreetingsOffset = mScreenWidth * 1.25f;

        Matrix.perspectiveM(mProj, 0, 45f, height/width, 0.1f, 100f);
        Matrix.orthoM(mOrtho, 0, 0f,width,height,0f, 0f,1f);
        Log.d(TAG, "onSurfaceChanged: w:" + width + " h:" + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        ModulePlayer.State playerState = mModulePlayer.getState();

        long previousTime = mCurrentTime;
        mCurrentTime = System.currentTimeMillis();
        float elapsed = (mCurrentTime - previousTime) / 1000.0f;

        GLES20.glClearColor(.6f, .7f, .9f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 3D floor in background
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        mChessTexture.bind(0);
        mGroundMesh.prepare();
        mOffsetGround -= elapsed * 2.0f;
        mGroundMesh.put(-.5f,  .5f, 0f,     0f,       -mOffsetGround,     .6f, .7f, .9f, 1f);
        mGroundMesh.put(-.5f, -.5f, 0f,     0f,   125f-mOffsetGround,     .9f, .8f, .9f, 1f);
        mGroundMesh.put( .5f, -.5f, 0f,     125f, 125f-mOffsetGround,     .9f, .8f, .9f, 1f);
        mGroundMesh.put(-.5f,  .5f, 0f,     0f,       -mOffsetGround,     .6f, .7f, .9f, 1f);
        mGroundMesh.put( .5f, -.5f, 0f,     125f, 125f-mOffsetGround,     .9f, .8f, .9f, 1f);
        mGroundMesh.put( .5f,  .5f, 0f,     125f,   0f-mOffsetGround,     .6f, .7f, .9f, 1f);

        Matrix.setIdentityM(mView, 0);
        Matrix.translateM(mView,0, 0f, -0.1f, -20f);
        Matrix.rotateM(mView,0, 90f, 1f, 0f, 0f);
        Matrix.rotateM(mView,0, -25f, 0f, 1f, 0f);
        Matrix.scaleM(mView,0, 25f,50f,0f);
        Matrix.multiplyMM(mCombined,0, mProj,0, mView,0);
        mGroundMesh.draw(mCombined, 0);

        Matrix.setIdentityM(mView, 0);
        Matrix.translateM(mView,0, 0f, 0.1f, -20f);
        Matrix.rotateM(mView,0, 90f, 1f, 0f, 0f);
        Matrix.rotateM(mView,0, -25f, 0f, 1f, 0f);
        Matrix.scaleM(mView,0, 25f,50f,0f);
        Matrix.multiplyMM(mCombined,0, mProj,0, mView,0);
        mGroundMesh.draw(mCombined, 0);

        // 2D beam in the middle
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        mBeamTexture.bind(0);
        mSpriteBuffer.prepare();
        mSpriteCenterBeam.scaleY = 2f;
        mSpriteCenterBeam.color.r = 1f;
        mSpriteCenterBeam.color.g = 1f;
        mSpriteCenterBeam.color.b = 1f;
        mSpriteCenterBeam.color.a = 0.5f;
        mSpriteBuffer.put(mSpriteCenterBeam);
        mSpriteBuffer.draw(mOrtho, 0);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        mSpriteBuffer.prepare();
        mSpriteCenterBeam.color.r = 0.5f;
        mSpriteCenterBeam.color.g = 0.1f;
        mSpriteCenterBeam.color.b = 0.5f;
        mSpriteCenterBeam.color.a = 1f;
        mSpriteCenterBeam.scaleY = 10f;
        mSpriteBuffer.put(mSpriteCenterBeam);
        mSpriteCenterBeam.scaleY = 4f;
        mSpriteBuffer.put(mSpriteCenterBeam);
        mSpriteBuffer.draw(mOrtho, 0);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Bounce letter logo
        mSketchupAnimation.compute(mCurrentTime);
        mLogoTexture.bind(0);
        mSpriteBuffer.prepare();
        mLogoLetterSprite.x = ((mScreenWidth * 0.5f) - ((8*mLogoLetterSprite.w) * 0.5f)) + mLogoLetterSprite.w * 0.5f;
        for (int i=0; i<8; i++) {
            mLogoLetterSprite.setFrame(i);
            mSketchupAnimation.apply(mLogoLetterSprite);
            mSpriteBuffer.put(mLogoLetterSprite);
            mLogoLetterSprite.x += mLogoLetterSprite.w;
        }
        mSpriteBuffer.draw(mOrtho, 0);

        // text overlays
        mChessTexture.bind(0);
        mSpriteBuffer.prepare();
        mSpriteBuffer.put(mCenterOverlaySprite);
        mSpriteBuffer.put(mBottomOverlaySprite);
        mSpriteBuffer.draw(mOrtho, 0);

        // intro text
        mFontTexture.bind(0);
        mSpriteBuffer.prepare();
        mFontSprite.scaleX = 1f;
        mFontSprite.scaleY = 1f;
        int y = 0;
        for (char[] string : INTRO_TEXT) {
            mFontSprite.color.b = 0.5f;
            mFontSprite.color.g = 0.7f + (float) Math.cos(Math.toRadians((y*10) + (45*mOffsetGround))) * 0.3f;
            mFontSprite.x = mFontSprite.w * 2;
            mFontSprite.y = ((mScreenHeight * 0.5f) + (mFontSprite.h * 0.5f)) - ((14 * 0.5f) * mFontSprite.h) + (y*mFontSprite.h);
            for (char c : string) {
                if (c != ' ') {
                    mFontSprite.setFrame(c);
                    mSpriteBuffer.put(mFontSprite);
                }

                mFontSprite.x += mFontSprite.w;
            }
            y++;
        }
        mSpriteBuffer.draw(mOrtho, 0);

        // greetings
        mGreetingAnimation.compute(mCurrentTime);
        mGreetingsOffset -= playerState.pattern >= 1 ? elapsed * 170 : 0;
        mSpriteBuffer.prepare();
        mFontSprite.x = mGreetingsOffset;
        mFontSprite.y = mScreenHeight - mFontSprite.h * 2;
        for (char c : GREETINGS) {
            if (mGreetingsOffset < -(mFontSprite.w * 2) * GREETINGS.length) {
                mGreetingsOffset = mScreenWidth + mFontSprite.w * 2;
            } else if (mFontSprite.x < -mFontSprite.w * 2) {
                mFontSprite.x += mFontSprite.w * 2;
                continue;
            } else if (mFontSprite.x > mScreenWidth + mFontSprite.w * 2) {
                break;
            }

            if (c != ' ') {
                mFontSprite.setFrame(c);
                mGreetingAnimation.apply(mFontSprite);
                mSpriteBuffer.put(mFontSprite);
            }

            mFontSprite.x += mFontSprite.w * 2;
        }
        mFontTexture.bind(0);
        mSpriteBuffer.draw(mOrtho, 0);

        // Scanlines
        mScanlineTexture.bind(0);
        mSpriteBuffer.prepare();
        mSpriteBuffer.put(mSpriteScanline);
        mSpriteBuffer.draw(mOrtho, 0);

        // Overlay at start
        if (playerState.pattern == 0 || (playerState.pattern == 1 && playerState.row <= 10)) {
            if (playerState.pattern == 1 && playerState.row <= 10) {
                mOverlaySprite.color.a -= playerState.row * 0.1f;
            }
            mChessTexture.bind(0);
            mSpriteBuffer.prepare();
            mSpriteBuffer.put(mOverlaySprite);
            mSpriteBuffer.draw(mOrtho, 0);
        }

        /*
        if (BuildConfig.DEBUG) {
            mFontTexture.bind(0);
            mSpriteBuffer.prepare();
            String debug = playerState.pattern + ":" + playerState.row;

            mFontSprite.x = mFontSprite.w;
            mFontSprite.y = mScreenHeight - mFontSprite.h;
            mFontSprite.color.g = 0.0f;
            mFontSprite.color.r = mFontSprite.color.b = mFontSprite.color.a = 1.0f;
            for (char c : debug.toCharArray()) {
                mFontSprite.setFrame(c);
                mSpriteBuffer.put(mFontSprite);
                mFontSprite.x += mFontSprite.w;
            }
            mSpriteBuffer.draw(mOrtho, 0);
        }
        */
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static int[] loadTexture(Context context, String filename) throws Exception {
        int[] ret = new int[3];
        int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0) {
            ret[0] = textureHandle[0];

            Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename));
            ret[1] = bitmap.getWidth();
            ret[2] = bitmap.getHeight();

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        } else {
            throw new Exception("Error loading texture: " + filename);
        }

        return ret;
    }

    /* Logo is 8 letters of 32x32 */
    private Sprite.UV[] buildLogoSpriteCoords() {
        Sprite.UV[] uvs = new Sprite.UV[8];
        float step = 1.0f / (float) 8;

        for (int i=0; i<8; i++) {
            uvs[i] = new Sprite.UV();
            uvs[i].s = (i * step) + step;
            uvs[i].t = 1.0f;
            uvs[i].u = i * step;
            uvs[i].v = 0.0f;
        }

        return uvs;
    }

    /* Font texture must be 16*16 character texture (256 glyphs), of the exact same size each */
    private Sprite.UV[] buildFontSpriteCoords() {
        Sprite.UV[] uvs = new Sprite.UV[16*16];
        float step = 1.0f / 16.0f;

        for (int y=0; y<16; y++) {
            for (int x=0; x<16; x++) {
                int offset = (y*16)+x;
                uvs[offset] = new Sprite.UV();
                uvs[offset].s = (x * step) + step;
                uvs[offset].t = (y * step) + step;
                uvs[offset].u = x * step;
                uvs[offset].v = y * step;
            }
        }

        return uvs;
    }


}

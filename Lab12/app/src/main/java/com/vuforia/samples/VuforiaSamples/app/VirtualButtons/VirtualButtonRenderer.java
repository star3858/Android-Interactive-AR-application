/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.VirtualButtons;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Device;
import com.vuforia.ImageTargetResult;
import com.vuforia.Rectangle;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VirtualButton;
import com.vuforia.VirtualButtonResult;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleAppRenderer;
import com.vuforia.samples.SampleApplication.SampleAppRendererControl;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.CubeObject;
import com.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.vuforia.samples.SampleApplication.utils.LineShaders;
import com.vuforia.samples.SampleApplication.utils.OBJLoader;
import com.vuforia.samples.SampleApplication.utils.OBJParser.ObjParser;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Teapot;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.vuforia.samples.VuforiaSamples.R;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.random;

public class VirtualButtonRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl {
    private static final String LOGTAG = "VirtualButtonRenderer";

    private SampleApplicationSession vuforiaAppSession;
    private SampleAppRenderer mSampleAppRenderer;
    private int NUM = 30;

    private boolean mIsActive = false;

    private VirtualButtons mActivity;

    private Vector<Texture> mTextures;

    private Teapot mTeapot = new Teapot();
    private CubeObject mCube = new CubeObject();
    private OBJLoader[] mobj = new OBJLoader[NUM];//white

    // OpenGL ES 2.0 specific (3D model):
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;

    private int lineOpacityHandle = 0;
    private int lineColorHandle = 0;
    private int mvpMatrixButtonsHandle = 0;

    // OpenGL ES 2.0 specific (Virtual Buttons):
    private int vbShaderProgramID = 0;
    private int vbVertexHandle = 0;

    // Constants:
    static private float kTeapotScale = 0.003f, kCubeScale = 0.002f, kObjScale = 0.015f;

    // Define the coordinates of the virtual buttons to render the area of action,
    // this values are the same as the wood dataset
    static private float RED_VB_BUTTON[] = {-0.10868f, -0.05352f, -0.07575f, -0.06587f};
    static private float BLUE_VB_BUTTON[] = {-0.04528f, -0.05352f, -0.01235f, -0.06587f};
    static private float YELLOW_VB_BUTTON[] = {0.01482f, -0.05352f, 0.04775f, -0.06587f};
    static private float CUSTOM_OBJECT_VB_BUTTON[] = {0.07657f, -0.05352f, 0.10950f, -0.06587f};
    static private float ROT_VB_BUTTON[] = {0.07657f, 0.05352f, 0.10950f, 0.06587f};

    private float angle = 0.0f;
    private float crash_height=0.0f;
    private float crash_angle=0.0f;
    private float cloudmove = 0.0f;
    private float cloudmove2 = 0.0f;
    private float cloudmove3 = -5.0f;
    private float cloudmove4 = -13.0f;
    private float cloudmove5 = -5.0f;
    private float cloudmove6 = -2.0f;
    private float cloudmove7 = -7.0f;
    private float cloudmove8 = -3.0f;

    private float birdmove = 0.0f;
    private float birdmove2 = 0.0f;
    private float fishmove = 0.0f;
    private float fishmove1 = 0.0f;
    private float fishmove2 = 0.0f;
    private float fishmove3 = 0.0f;
    private float water_height = 0.0f;
    public double shipposition_x = 0.0f;
    public double shipposition_y = 0.0f;
    public float treasure_x = 7.0f;
    public float treasure_z = 5.0f;
    public double sharkposition_x = 0.0f;
    public double sharkposition_y = 0.0f;
    public int treasure_count=0;
    public int treasure_max=0;
    private float coin_height=0.0f;
    private float coin_angle=0.0f;
    public float rain_height=0.0f;
    public float coin_x = 0.0f;
    public float coin_z = 0.0f;
    private boolean state_sea=true;
    private float number_sea=0.0f;
    private float rainmove1 = 0.0f;
    private float rainmove2 = 5.0f;
    private float rainmove3 = 3.0f;
    private float rainmove4 = 15.0f;


    private int textureIndex = 0;
    private int objects;
    private int state_color=0;

    private ArrayList[] verticeBuffers = new ArrayList[NUM];
    private ArrayList[] textureBuffers = new ArrayList[NUM];

    //배 움직임
    public float xmove = -3f;
    public float zmove = -1.5f;

    //상태 on/off
    public boolean state_ocean = false;
    public boolean state_crash = false;//충돌하기 전
    public boolean state_shark = true;
    public boolean state_ship_reset=false;
    public boolean state_coin=false;
    public boolean state_rain = false;

    public VirtualButtonRenderer(VirtualButtons activity,
                                 SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;

        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.01f, 5f);
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        // Call function to initialize rendering:
        initRendering();
    }


    public void setActive(boolean active) {
        mIsActive = active;

        if (mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive) return;

        //GLES20.glClearColor(0.45f, 0.45f, 0.45f, 0.5f);
        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();

    }


    private void initRendering() {
        Log.d(LOGTAG, "initRendering");

        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

        // OpenGL setup for Virtual Buttons
        vbShaderProgramID = SampleUtils.createProgramFromShaderSrc(
                LineShaders.LINE_VERTEX_SHADER, LineShaders.LINE_FRAGMENT_SHADER);

        mvpMatrixButtonsHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
                "modelViewProjectionMatrix");
        vbVertexHandle = GLES20.glGetAttribLocation(vbShaderProgramID,
                "vertexPosition");
        lineOpacityHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
                "opacity");
        lineColorHandle = GLES20.glGetUniformLocation(vbShaderProgramID,
                "color");

        ObjParser objParser1 = new ObjParser(mActivity);
        try {
            objParser1.parse(R.raw.testwhite);
        } catch (IOException e) {
        }
        objects = objParser1.getObjectIds().size();
        mobj[0] = new OBJLoader(objParser1);
        verticeBuffers[0] = mobj[0].getBuffers(0);
        textureBuffers[0] = mobj[0].getBuffers(2);
        ObjParser objParser2 = new ObjParser(mActivity);
        try {
            objParser2.parse(R.raw.testred);
        } catch (IOException e) {
        }
        objects = objParser2.getObjectIds().size();
        mobj[1] = new OBJLoader(objParser2);
        verticeBuffers[1] = mobj[1].getBuffers(0);
        textureBuffers[1] = mobj[1].getBuffers(2);

        ObjParser objParser3 = new ObjParser(mActivity);
        try {
            objParser3.parse(R.raw.testgreen);
        } catch (IOException e) {
        }
        objects = objParser3.getObjectIds().size();
        mobj[2] = new OBJLoader(objParser3);
        verticeBuffers[2] = mobj[2].getBuffers(0);
        textureBuffers[2] = mobj[2].getBuffers(2);

        ObjParser objParser4 = new ObjParser(mActivity);
        try {
            objParser4.parse(R.raw.testwindow);
        } catch (IOException e) {
        }
        objects = objParser4.getObjectIds().size();
        mobj[3] = new OBJLoader(objParser4);
        verticeBuffers[3] = mobj[3].getBuffers(0);
        textureBuffers[3] = mobj[3].getBuffers(2);

        ObjParser objParser5 = new ObjParser(mActivity);
        try {
            objParser5.parse(R.raw.testblue);
        } catch (IOException e) {
        }
        objects = objParser5.getObjectIds().size();
        mobj[4] = new OBJLoader(objParser5);
        verticeBuffers[4] = mobj[4].getBuffers(0);
        textureBuffers[4] = mobj[4].getBuffers(2);

        ObjParser objParser6 = new ObjParser(mActivity);
        try {
            objParser6.parse(R.raw.testrock);
        } catch (IOException e) {
        }
        objects = objParser6.getObjectIds().size();
        mobj[5] = new OBJLoader(objParser6);
        verticeBuffers[5] = mobj[5].getBuffers(0);
        textureBuffers[5] = mobj[5].getBuffers(2);

        ObjParser objParser7 = new ObjParser(mActivity);
        try {
            objParser7.parse(R.raw.testplant);
        } catch (IOException e) {
        }
        objects = objParser7.getObjectIds().size();
        mobj[6] = new OBJLoader(objParser7);
        verticeBuffers[6] = mobj[6].getBuffers(0);
        textureBuffers[6] = mobj[6].getBuffers(2);

        ObjParser objParser8 = new ObjParser(mActivity);
        try {
            objParser8.parse(R.raw.sea);
        } catch (IOException e) {
        }
        objects = objParser8.getObjectIds().size();
        mobj[7] = new OBJLoader(objParser8);
        verticeBuffers[7] = mobj[7].getBuffers(0);
        textureBuffers[7] = mobj[7].getBuffers(2);

        ObjParser objParser9 = new ObjParser(mActivity);
        try {
            objParser9.parse(R.raw.cloud);
        } catch (IOException e) {
        }
        objects = objParser9.getObjectIds().size();
        mobj[8] = new OBJLoader(objParser9);
        verticeBuffers[8] = mobj[8].getBuffers(0);
        textureBuffers[8] = mobj[8].getBuffers(2);

        ObjParser objParser10 = new ObjParser(mActivity);
        try {
            objParser10.parse(R.raw.shark1);
        } catch (IOException e) {
        }
        objects = objParser10.getObjectIds().size();
        mobj[9] = new OBJLoader(objParser10);
        verticeBuffers[9] = mobj[9].getBuffers(0);
        textureBuffers[9] = mobj[9].getBuffers(2);

        ObjParser objParser11 = new ObjParser(mActivity);
        try {
            objParser11.parse(R.raw.shark2);
        } catch (IOException e) {
        }
        objects = objParser11.getObjectIds().size();
        mobj[10] = new OBJLoader(objParser11);
        verticeBuffers[10] = mobj[10].getBuffers(0);
        textureBuffers[10] = mobj[10].getBuffers(2);

        ObjParser objParser12 = new ObjParser(mActivity);
        try {
            objParser12.parse(R.raw.ship3_1);
        } catch (IOException e) {
        }
        objects = objParser12.getObjectIds().size();
        mobj[11] = new OBJLoader(objParser12);
        verticeBuffers[11] = mobj[11].getBuffers(0);
        textureBuffers[11] = mobj[11].getBuffers(2);

        ObjParser objParser13 = new ObjParser(mActivity);
        try {
            objParser13.parse(R.raw.ship3_2);
        } catch (IOException e) {
        }
        objects = objParser13.getObjectIds().size();
        mobj[12] = new OBJLoader(objParser13);
        verticeBuffers[12] = mobj[12].getBuffers(0);
        textureBuffers[12] = mobj[12].getBuffers(2);

        ObjParser objParser14 = new ObjParser(mActivity);
        try {
            objParser14.parse(R.raw.seaplant);
        } catch (IOException e) {
        }
        objects = objParser14.getObjectIds().size();
        mobj[13] = new OBJLoader(objParser14);
        verticeBuffers[13] = mobj[13].getBuffers(0);
        textureBuffers[13] = mobj[13].getBuffers(2);

        ObjParser objParser15 = new ObjParser(mActivity);
        try {
            objParser15.parse(R.raw.seaweed);
        } catch (IOException e) {
        }
        objects = objParser15.getObjectIds().size();
        mobj[14] = new OBJLoader(objParser15);
        verticeBuffers[14] = mobj[14].getBuffers(0);
        textureBuffers[14] = mobj[14].getBuffers(2);

        ObjParser objParser16 = new ObjParser(mActivity);
        try {
            objParser16.parse(R.raw.fish1);
        } catch (IOException e) {
        }
        objects = objParser16.getObjectIds().size();
        mobj[15] = new OBJLoader(objParser16);
        verticeBuffers[15] = mobj[15].getBuffers(0);
        textureBuffers[15] = mobj[15].getBuffers(2);

        ObjParser objParser17 = new ObjParser(mActivity);
        try {
            objParser17.parse(R.raw.treasure_case);
        } catch (IOException e) {
        }
        objects = objParser17.getObjectIds().size();
        mobj[16] = new OBJLoader(objParser17);
        verticeBuffers[16] = mobj[16].getBuffers(0);
        textureBuffers[16] = mobj[16].getBuffers(2);

        ObjParser objParser18 = new ObjParser(mActivity);
        try {
            objParser18.parse(R.raw.treasure_gold);
        } catch (IOException e) {
        }
        objects = objParser18.getObjectIds().size();
        mobj[17] = new OBJLoader(objParser18);
        verticeBuffers[17] = mobj[17].getBuffers(0);
        textureBuffers[17] = mobj[17].getBuffers(2);

        ObjParser objParser19 = new ObjParser(mActivity);
        try {
            objParser19.parse(R.raw.coin);
        } catch (IOException e) {
        }
        objects = objParser19.getObjectIds().size();
        mobj[18] = new OBJLoader(objParser19);
        verticeBuffers[18] = mobj[18].getBuffers(0);
        textureBuffers[18] = mobj[18].getBuffers(2);

        ObjParser objParser20 = new ObjParser(mActivity);
        try {
            objParser20.parse(R.raw.bird);
        } catch (IOException e) {
        }
        objects = objParser20.getObjectIds().size();
        mobj[19] = new OBJLoader(objParser20);
        verticeBuffers[19] = mobj[19].getBuffers(0);
        textureBuffers[19] = mobj[19].getBuffers(2);

        ObjParser objParser21 = new ObjParser(mActivity);
        try {
            objParser21.parse(R.raw.fish2);
        } catch (IOException e) {
        }
        objects = objParser21.getObjectIds().size();
        mobj[20] = new OBJLoader(objParser21);
        verticeBuffers[20] = mobj[20].getBuffers(0);
        textureBuffers[20] = mobj[20].getBuffers(2);

        ObjParser objParser22 = new ObjParser(mActivity);
        try {
            objParser22.parse(R.raw.rain1);
        } catch (IOException e) {
        }
        objects = objParser22.getObjectIds().size();
        mobj[21] = new OBJLoader(objParser22);
        verticeBuffers[21] = mobj[21].getBuffers(0);
        textureBuffers[21] = mobj[21].getBuffers(2);

        ObjParser objParser23 = new ObjParser(mActivity);
        try {
            objParser23.parse(R.raw.cloud2);
        } catch (IOException e) {
        }
        objects = objParser23.getObjectIds().size();
        mobj[22] = new OBJLoader(objParser23);
        verticeBuffers[22] = mobj[22].getBuffers(0);
        textureBuffers[22] = mobj[22].getBuffers(2);

        ObjParser objParser24 = new ObjParser(mActivity);
        try {
            objParser24.parse(R.raw.cloud3);
        } catch (IOException e) {
        }
        objects = objParser24.getObjectIds().size();
        mobj[23] = new OBJLoader(objParser24);
        verticeBuffers[23] = mobj[23].getBuffers(0);
        textureBuffers[23] = mobj[23].getBuffers(2);

        ObjParser objParser25 = new ObjParser(mActivity);
        try {
            objParser25.parse(R.raw.cloud4);
        } catch (IOException e) {
        }
        objects = objParser25.getObjectIds().size();
        mobj[24] = new OBJLoader(objParser25);
        verticeBuffers[24] = mobj[24].getBuffers(0);
        textureBuffers[24] = mobj[24].getBuffers(2);

        /*
        ObjParser objParserN = new ObjParser(mActivity);
        try {
            objParserN.parse(R.raw.treasure_case);
        } catch (IOException e) {
        }
        objects = objParserN.getObjectIds().size();
        mobj[M] = new OBJLoader(objParserN);
        verticeBuffers[M] = mobj[M].getBuffers(0);
        textureBuffers[M] = mobj[M].getBuffers(2);
        */


    }



    public void updateRenderingPrimitives() {
        mSampleAppRenderer.updateRenderingPrimitives();
    }


    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix) {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // Did we find any trackables this frame?
        if (state.getNumTrackableResults() > 0) {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(0);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(
                    trackableResult.getPose()).getData();

            // The image target specific result:
            ImageTargetResult imageTargetResult = (ImageTargetResult) trackableResult;

            // Set transformations:
            float[] modelViewProjection = new float[16];
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            // Set the texture used for the teapot model:
            /*int textureIndex = 0;*/

            float vbVertices[] = new float[imageTargetResult
                    .getNumVirtualButtons() * 24];
            short vbCounter = 0;

            // Iterate through this targets virtual buttons:
            for (int i = 0; i < imageTargetResult.getNumVirtualButtons(); ++i) {
                VirtualButtonResult buttonResult = imageTargetResult
                        .getVirtualButtonResult(i);
                VirtualButton button = buttonResult.getVirtualButton();
                int buttonIndex = 0;
                //shark_rotate = true;
                // Run through button name array to find button index
                for (int j = 0; j < VirtualButtons.NUM_BUTTONS; ++j) {
                    if (button.getName().compareTo(mActivity.virtualButtons[j]) == 0) {
                        buttonIndex = j;
                        break;
                    }
                }

                // If the button is pressed, than use this texture:
                if (buttonResult.isPressed()) {
                    if (buttonIndex >= 0 && buttonIndex < 4) {
                        state_color = buttonIndex;
                    }
                }


                // Define the four virtual buttons as Rectangle using the same values as the dataset

                Rectangle vbRectangle[] = new Rectangle[5];
                vbRectangle[0] = new Rectangle(RED_VB_BUTTON[0], RED_VB_BUTTON[1],
                        RED_VB_BUTTON[2], RED_VB_BUTTON[3]);
                vbRectangle[1] = new Rectangle(BLUE_VB_BUTTON[0], BLUE_VB_BUTTON[1],
                        BLUE_VB_BUTTON[2], BLUE_VB_BUTTON[3]);
                vbRectangle[2] = new Rectangle(YELLOW_VB_BUTTON[0], YELLOW_VB_BUTTON[1],
                        YELLOW_VB_BUTTON[2], YELLOW_VB_BUTTON[3]);
                vbRectangle[3] = new Rectangle(CUSTOM_OBJECT_VB_BUTTON[0], CUSTOM_OBJECT_VB_BUTTON[1],
                        CUSTOM_OBJECT_VB_BUTTON[2], CUSTOM_OBJECT_VB_BUTTON[3]);
                vbRectangle[4] = new Rectangle(ROT_VB_BUTTON[0], ROT_VB_BUTTON[1],
                        ROT_VB_BUTTON[2], ROT_VB_BUTTON[3]);


                // We add the vertices to a common array in order to have one
                // single
                // draw call. This is more efficient than having multiple
                // glDrawArray calls
                vbVertices[vbCounter] = vbRectangle[buttonIndex].getLeftTopX();
                vbVertices[vbCounter + 1] = vbRectangle[buttonIndex].getLeftTopY();
                vbVertices[vbCounter + 2] = 0.0f;
                vbVertices[vbCounter + 3] = vbRectangle[buttonIndex].getRightBottomX();
                vbVertices[vbCounter + 4] = vbRectangle[buttonIndex].getLeftTopY();
                vbVertices[vbCounter + 5] = 0.0f;
                vbVertices[vbCounter + 6] = vbRectangle[buttonIndex].getRightBottomX();
                vbVertices[vbCounter + 7] = vbRectangle[buttonIndex].getLeftTopY();
                vbVertices[vbCounter + 8] = 0.0f;
                vbVertices[vbCounter + 9] = vbRectangle[buttonIndex].getRightBottomX();
                vbVertices[vbCounter + 10] = vbRectangle[buttonIndex].getRightBottomY();
                vbVertices[vbCounter + 11] = 0.0f;
                vbVertices[vbCounter + 12] = vbRectangle[buttonIndex].getRightBottomX();
                vbVertices[vbCounter + 13] = vbRectangle[buttonIndex].getRightBottomY();
                vbVertices[vbCounter + 14] = 0.0f;
                vbVertices[vbCounter + 15] = vbRectangle[buttonIndex].getLeftTopX();
                vbVertices[vbCounter + 16] = vbRectangle[buttonIndex].getRightBottomY();
                vbVertices[vbCounter + 17] = 0.0f;
                vbVertices[vbCounter + 18] = vbRectangle[buttonIndex].getLeftTopX();
                vbVertices[vbCounter + 19] = vbRectangle[buttonIndex].getRightBottomY();
                vbVertices[vbCounter + 20] = 0.0f;
                vbVertices[vbCounter + 21] = vbRectangle[buttonIndex].getLeftTopX();
                vbVertices[vbCounter + 22] = vbRectangle[buttonIndex].getLeftTopY();
                vbVertices[vbCounter + 23] = 0.0f;
                vbCounter += 24;

            }

            // We only render if there is something on the array
            if (vbCounter > 0) {
                // Render frame around button
                GLES20.glUseProgram(vbShaderProgramID);

                GLES20.glVertexAttribPointer(vbVertexHandle, 3, GLES20.GL_FLOAT, false, 0, fillBuffer(vbVertices));
                GLES20.glEnableVertexAttribArray(vbVertexHandle);
                GLES20.glUniform1f(lineOpacityHandle, 1.0f);
                GLES20.glUniform3f(lineColorHandle, 1.0f, 1.0f, 1.0f);
                GLES20.glUniformMatrix4fv(mvpMatrixButtonsHandle, 1, false, modelViewProjection, 0);
                GLES20.glDrawArrays(GLES20.GL_LINES, 0, imageTargetResult.getNumVirtualButtons() * 8);
                SampleUtils.checkGLError("VirtualButtons drawButton");

                GLES20.glDisableVertexAttribArray(vbVertexHandle);
            }

            // Assumptions:
            Texture thisTexture = mTextures.get(textureIndex);

            float[] rotationMatrix = new float[16];
            float[] translationMatrix = new float[16];
            float[] modelViewProjectionScaled = new float[16];
            float[] modelViewMatrix_shark = new float[16];
            float[] modelViewMatrix_shark_inverse = new float[16];
            float[] modelViewMatrix_ship = new float[16];
            float[] modelViewMatrix_ship_inverse= new float[16];
            float[] modelViewMatrix_seeplant = new float[16];
            float[] modelViewMatrix_seeplant_inverse = new float[16];
            float[] modelViewMatrix_fish = new float[16];
            float[] modelViewMatrix_fish_inverse = new float[16];
            float[] modelViewMatrix_fish2 = new float[16];
            float[] modelViewMatrix_fish2_inverse = new float[16];

            float[] modelViewMatrix_sea = new float[16];
            float[] modelViewMatrix_sea_inverse = new float[16];
            float[] modelViewMatrix_cloud = new float[16];
            float[] modelViewMatrix_cloud_inverse = new float[16];
            float[] modelViewMatrix_treasure = new float[16];
            float[] modelViewMatrix_treasure_inverse = new float[16];
            float[] modelViewMatrix_coin = new float[16];
            float[] modelViewMatrix_coin_inverse = new float[16];
            float[] modelViewMatrix_bird = new float[16];
            float[] modelViewMatrix_bird_inverse = new float[16];
            float[] modelViewMatrix_rain = new float[16];
            float[] modelViewMatrix_rain_inverse = new float[16];
            //모델뷰

            // Scale 3D model

            /*
            if (textureIndex == 0 || textureIndex == 1) {
                Matrix.scaleM(modelViewMatrix, 0, kTeapotScale, kTeapotScale, kTeapotScale);
            }
            else if (textureIndex == 2) {
                Matrix.scaleM(modelViewMatrix, 0, kCubeScale, kCubeScale, kCubeScale);
            }
            else if (textureIndex == 3) {
            */
            Matrix.scaleM(modelViewMatrix, 0, kObjScale, kObjScale, kObjScale);
            //}

            // Render 3D model
            GLES20.glUseProgram(shaderProgramID);

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            /*
            if (textureIndex == 0 || textureIndex == 1) {
                Matrix.setRotateM(rotationMatrix, 0, angle, 0.0f, 0.0f, 1.0f);
                Matrix.multiplyMM(modelViewMatrix, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, mTeapot.getVertices());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                        GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        thisTexture.mTextureID[0]);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                        mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                        mTeapot.getIndices());
            }
            else if (textureIndex == 2) {
                Matrix.setRotateM(rotationMatrix, 0, angle, 0.0f, 0.0f, 1.0f);
                Matrix.multiplyMM(modelViewMatrix, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, mCube.getVertices());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                        GLES20.GL_FLOAT, false, 0, mCube.getTexCoords());
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        thisTexture.mTextureID[0]);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                        mCube.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                        mCube.getIndices());
            }
            else if (textureIndex == 3) {
            */
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 0.0f, 1.0f);
            Matrix.multiplyMM(modelViewMatrix, 0, modelViewMatrix, 0, rotationMatrix, 0);
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
            Matrix.translateM(translationMatrix,0,0,0,water_height);
            Matrix.multiplyMM(modelViewMatrix, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.multiplyMM(modelViewMatrix, 0, modelViewMatrix, 0, rotationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix, 0);

            //1 testwhite
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[0].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[0].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 1);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[0].getNumObjectVertex(0));

            //2 testred
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[1].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[1].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            if(state_color==0) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 2);//1번째 텍스처
            if(state_color==1) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 22);//1번째 텍스처
            if(state_color==2) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 16);//1번째 텍스처
            if(state_color==3) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 23);//1번째 텍스처
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[1].getNumObjectVertex(0));

            //3 testgreen
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[2].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[2].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 3);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[2].getNumObjectVertex(0));

            //4 testwindow
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[3].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[3].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 4);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[3].getNumObjectVertex(0));

            //5 testblue
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[4].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[4].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 5);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[4].getNumObjectVertex(0));

            //6 testrock
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[5].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[5].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 6);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[5].getNumObjectVertex(0));

            //7 testplant
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[6].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[6].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 7);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[6].getNumObjectVertex(0));

            //14 해초
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,0f,-4f,0);
            Matrix.multiplyMM(modelViewMatrix_seeplant, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.scaleM(modelViewMatrix_seeplant,0,2f,1.0f,2f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_seeplant, 0);

            if(state_ocean&&water_height>0) {
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[13].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[13].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 7);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[13].getNumObjectVertex(0));
            }

            //15 seaweed 산호초
            if(state_ocean&&water_height>0) {
                //1번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0f, -4f, 0);
                Matrix.multiplyMM(modelViewMatrix_seeplant, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_seeplant, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[14].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[14].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 17);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[14].getNumObjectVertex(0));

                //2번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 30, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, 5f, -4f, 5);
                Matrix.multiplyMM(modelViewMatrix_seeplant, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_seeplant, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[14].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[14].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 16);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[14].getNumObjectVertex(0));

                //3번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 30, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, 30f, -4f, -15);
                Matrix.multiplyMM(modelViewMatrix_seeplant, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_seeplant, 0, 0.8f, 0.8f, 0.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_seeplant, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[14].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[14].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 17);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[14].getNumObjectVertex(0));

                //4번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 75, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, 10f, -4f, -15);
                Matrix.multiplyMM(modelViewMatrix_seeplant, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_seeplant, 0, 1.4f, 1.4f, 1.4f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_seeplant, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[14].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[14].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 32);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[14].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_seeplant_inverse, 0, modelViewMatrix_seeplant, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_seeplant_inverse, 0);
            }

            //16 물고기
            if(state_ocean&&water_height>0) {
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 270, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, -fishmove, -1.5f, 0f);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[15].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[15].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 18);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[15].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish_inverse, 0, modelViewMatrix_fish, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 270, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, -(1.2f)*fishmove1, -2f, 15f);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.8f, 0.8f, 0.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[15].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[15].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 18);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[15].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish_inverse, 0, modelViewMatrix_fish, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, -(0.4f)*fishmove2, -2f, -8f);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.8f, 0.7f, 0.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[15].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[15].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 18);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[15].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish_inverse, 0, modelViewMatrix_fish, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, (0.5f)*fishmove2, -2.5f, 5f);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix_fish, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.8f, 0.7f, 0.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[15].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[15].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 18);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[15].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish_inverse, 0, modelViewMatrix_fish, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, 2+(0.5f)*fishmove2, -2.3f, -12);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix_fish, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.8f, 0.7f, 0.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[15].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[15].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 18);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[15].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish_inverse, 0, modelViewMatrix_fish, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
                Matrix.translateM(translationMatrix, 0, -5, -2.3f, -12+fishmove1);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish, 0, modelViewMatrix_fish, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.8f, 0.7f, 0.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[15].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[15].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 18);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[15].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish_inverse, 0, modelViewMatrix_fish, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish_inverse, 0);
            }

            //21 물고기2(니모)
            if(state_ocean&&water_height>0) {
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -5, -4f, 5f);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, fishmove, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0, 0f, -5 + fishmove);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.4f, 0.4f, 0.4f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[20].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[20].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 21);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[20].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish2_inverse, 0, modelViewMatrix_fish2, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 8, -5f, -13f);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, (0.3f)*fishmove, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0, 0f, -6 + (1.1f)*fishmove);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.3f, 0.3f, 0.3f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[20].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[20].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 21);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[20].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish2_inverse, 0, modelViewMatrix_fish2, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2_inverse, 0);

                //파랑물고기
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -5, -3f, 5f);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 180-fishmove1, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0, 0f,  -(0.7f)*fishmove1);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.4f, 0.4f, 0.4f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[20].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[20].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 31);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[20].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish2_inverse, 0, modelViewMatrix_fish2, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 8, -3.5f, -6f);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 190-fishmove1, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0, 0f,  -(0.85f)*fishmove1);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_fish2, 0, modelViewMatrix_fish2, 0, rotationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_fish2, 0, 0.45f, 0.45f, 0.45f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[20].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[20].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 31);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[20].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_fish2_inverse, 0, modelViewMatrix_fish2, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_fish2_inverse, 0);
            }

            //10 상어
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, -angle, 0.0f,
                    1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_shark, 0, modelViewMatrix,
                    0, rotationMatrix, 0);
            Matrix.translateM(translationMatrix,0,12f,-1.8f+rain_height,
                    0);
            Matrix.multiplyMM(modelViewMatrix_shark, 0, modelViewMatrix_shark,
                    0, translationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix,
                    0, modelViewMatrix_shark, 0);

            sharkposition_x = 12*Math.cos(angle*3.1415/180);
            sharkposition_y = 12*Math.sin(angle*3.1415/180);

            if(state_ocean&&water_height>0) {
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[9].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[9].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 10);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[9].getNumObjectVertex(0));
                //11상어 입
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[10].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[10].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 11);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[10].getNumObjectVertex(0));
            }
            Matrix.invertM(modelViewMatrix_shark_inverse,0,modelViewMatrix_shark,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_shark_inverse, 0);

            //12 ship
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,xmove,0.2f-crash_height+rain_height,zmove);
            Matrix.multiplyMM(modelViewMatrix_ship, 0, modelViewMatrix, 0, translationMatrix, 0);
            if(state_crash){
                Matrix.setIdentityM(rotationMatrix,0);
                Matrix.setRotateM(rotationMatrix, 0, -crash_angle, 1.0f, 0.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_ship, 0, modelViewMatrix_ship, 0, rotationMatrix, 0);
            }
            Matrix.scaleM(modelViewMatrix_ship,0,0.5f,0.5f,0.5f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_ship, 0);
            if(xmove>0) shipposition_x=xmove;
            else shipposition_x=xmove;
            shipposition_y=zmove;
           // Log.i(this.getClass().getName(), "배 x : "+shipposition_x+"     배 y :"+shipposition_y);

            if(state_ocean&&water_height>0) {
                //배 몸통
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[11].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[11].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 12);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[11].getNumObjectVertex(0));
                //배 돛
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[12].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[12].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 13);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[12].getNumObjectVertex(0));
            }
            Matrix.invertM(modelViewMatrix_ship_inverse,0,modelViewMatrix_ship,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_ship_inverse, 0);

            //8 sea 바다
            if(water_height>0) {
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0, -1.2f + rain_height, 0);
                Matrix.multiplyMM(modelViewMatrix_sea, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_sea, 0, 1.8f, 1f, 1.8f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_sea, 0);
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendColor(1, 1, 1, 1.0f);
                GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_DST_COLOR);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[7].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[7].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                if (number_sea <= 1) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 24);
                if (1 < number_sea && number_sea <= 2) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 25);
                if (2 < number_sea && number_sea <= 3) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 26);
                if (3 < number_sea&&number_sea<=4) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 27);
                if (4 < number_sea&&number_sea<=5) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 28);
                if (5 < number_sea&&number_sea<=6) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 29);
                if (6 < number_sea) GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 30);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[7].getNumObjectVertex(0));
                GLES20.glDisable(GLES20.GL_BLEND);
                Matrix.invertM(modelViewMatrix_sea_inverse, 0, modelViewMatrix_sea, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_sea_inverse, 0);
            }

            //9 cloud 구름
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,-5,9f,-15+cloudmove);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[8].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[8].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[8].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_cloud_inverse,0,modelViewMatrix_cloud,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

            //9 cloud2
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,8,12f,-13+cloudmove2);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix_cloud, 0, rotationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[22].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[22].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[22].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_cloud_inverse,0,modelViewMatrix_cloud,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,9,10f,-15+cloudmove3);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix_cloud, 0, rotationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[23].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[23].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[23].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_cloud_inverse,0,modelViewMatrix_cloud,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,-15,17f,-18+cloudmove4);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix_cloud, 0, rotationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[24].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[24].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[24].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_cloud_inverse,0,modelViewMatrix_cloud,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

            //새로추가
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,-17,19f,-18+cloudmove5);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix_cloud, 0, rotationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[24].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[24].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[24].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_cloud_inverse,0,modelViewMatrix_cloud,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,-20,19f,-18+cloudmove5);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.setIdentityM(rotationMatrix, 0);
            Matrix.setRotateM(rotationMatrix, 0, 90, 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix_cloud, 0, rotationMatrix, 0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[24].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[24].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[24].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_cloud_inverse,0,modelViewMatrix_cloud,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

            if(state_rain) {
                //5번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -8, 11f, -12 + cloudmove5);
                Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_cloud, 0, 2f, 2f, 2f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[8].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[8].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[8].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_cloud_inverse, 0, modelViewMatrix_cloud, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

                //6번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 6, 12f, -10 + cloudmove6);
                Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_cloud, 0, 2.2f, 1.6f, 2.3f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[8].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[8].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[8].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_cloud_inverse, 0, modelViewMatrix_cloud, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

                //7번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 4, 10f, -8 + cloudmove7);
                Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_cloud, 0, 1.5f, 1.5f, 1.4f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[8].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[8].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[8].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_cloud_inverse, 0, modelViewMatrix_cloud, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);

                //8번째
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -9, 13f, -10 + cloudmove8);
                Matrix.multiplyMM(modelViewMatrix_cloud, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_cloud, 0, 3f, 3f, 3f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[8].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[8].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 9);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[8].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_cloud_inverse, 0, modelViewMatrix_cloud, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_cloud_inverse, 0);
            }
            //17 보물상자
            if(!state_coin&&water_height>0) {
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, treasure_x, 0.6f + rain_height, treasure_z);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_treasure, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_treasure, 0, modelViewMatrix_treasure, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_treasure, 0, 0.5f, 0.5f, 0.5f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_treasure, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[16].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[16].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 4);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[16].getNumObjectVertex(0));

                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[17].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[17].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 19);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[17].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_treasure_inverse, 0, modelViewMatrix_treasure, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_treasure_inverse, 0);
            }

            //19 코인
            if(state_coin) {
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, coin_angle, 0.0f, 1.0f, 0.0f);
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -treasure_x, 1 + coin_height+rain_height,-treasure_z);
                Matrix.multiplyMM(modelViewMatrix_coin, 0, modelViewMatrix, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewMatrix_coin, 0, modelViewMatrix_coin, 0, rotationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_coin, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[18].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[18].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 19);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[18].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_coin_inverse, 0, modelViewMatrix_coin, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_coin_inverse, 0);
            }

            //20  새
            Matrix.setIdentityM(translationMatrix,0);
            Matrix.translateM(translationMatrix,0,0,2f,-birdmove);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix, 0, translationMatrix, 0);
            Matrix.scaleM(modelViewMatrix_bird,0,3f,3f,3f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[19].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[19].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 20);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[19].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_bird_inverse,0,modelViewMatrix_bird,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, 90+(2*birdmove), 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix, 0, rotationMatrix, 0);
            Matrix.translateM(translationMatrix,0,7,4.5f+(0.1f*birdmove),2-birdmove);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix_bird, 0, translationMatrix, 0);
            Matrix.scaleM(modelViewMatrix_bird,0,3f,3f,3f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[19].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[19].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 20);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[19].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_bird_inverse,0,modelViewMatrix_bird,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, 130+(2.5f*birdmove), 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix, 0, rotationMatrix, 0);
            Matrix.translateM(translationMatrix,0,-18,13f-(0.2f*birdmove),4-birdmove);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix_bird, 0, translationMatrix, 0);
            Matrix.scaleM(modelViewMatrix_bird,0,3.5f,3.5f,3.5f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[19].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[19].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 20);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[19].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_bird_inverse,0,modelViewMatrix_bird,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, 75-(2.5f*birdmove), 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix, 0, rotationMatrix, 0);
            Matrix.translateM(translationMatrix,0,20,13f-(0.2f*birdmove),13-birdmove);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix_bird, 0, translationMatrix, 0);
            Matrix.scaleM(modelViewMatrix_bird,0,3.5f,3.5f,3.5f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[19].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[19].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 20);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[19].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_bird_inverse,0,modelViewMatrix_bird,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird_inverse, 0);

            Matrix.setIdentityM(translationMatrix,0);
            Matrix.setIdentityM(rotationMatrix,0);
            Matrix.setRotateM(rotationMatrix, 0, (2.5f*birdmove), 0.0f, 1.0f, 0.0f);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix, 0, rotationMatrix, 0);
            Matrix.translateM(translationMatrix,0,-20,13f+(0.2f*birdmove),23+birdmove);
            Matrix.multiplyMM(modelViewMatrix_bird, 0, modelViewMatrix_bird, 0, translationMatrix, 0);
            Matrix.scaleM(modelViewMatrix_bird,0,2.5f,2.5f,2.5f);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird, 0);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[19].get(0));
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[19].get(0));
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 20);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[19].getNumObjectVertex(0));
            Matrix.invertM(modelViewMatrix_bird_inverse,0,modelViewMatrix_bird,0);
            Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_bird_inverse, 0);

            //22 비내리기
            if(state_rain) {
                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 180, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 0, 13 - rainmove1, 0);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix_rain, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[21].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[21].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 8);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[21].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_rain_inverse, 0, modelViewMatrix_rain, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 200, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -5, 10 - rainmove2, 8);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix_rain, 0, translationMatrix, 0);
                Matrix.scaleM(modelViewMatrix_rain, 0, 3f, 3f, 3f);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[21].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[21].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 8);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[21].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_rain_inverse, 0, modelViewMatrix_rain, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 150, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, 4, 19 - rainmove3, -2);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix_rain, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[21].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[21].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 8);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[21].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_rain_inverse, 0, modelViewMatrix_rain, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain_inverse, 0);

                Matrix.setIdentityM(translationMatrix, 0);
                Matrix.setIdentityM(rotationMatrix, 0);
                Matrix.setRotateM(rotationMatrix, 0, 120, 0.0f, 1.0f, 0.0f);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix, 0, rotationMatrix, 0);
                Matrix.translateM(translationMatrix, 0, -10, 18 - rainmove4, -8);
                Matrix.multiplyMM(modelViewMatrix_rain, 0, modelViewMatrix_rain, 0, translationMatrix, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain, 0);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, (FloatBuffer) verticeBuffers[21].get(0));
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, (FloatBuffer) textureBuffers[21].get(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 8);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionScaled, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mobj[21].getNumObjectVertex(0));
                Matrix.invertM(modelViewMatrix_rain_inverse, 0, modelViewMatrix_rain, 0);
                Matrix.multiplyMM(modelViewProjectionScaled, 0, projectionMatrix, 0, modelViewMatrix_rain_inverse, 0);
            }

            if(number_sea>=6.9f) state_sea=false;
            if(number_sea<=0.1f)state_sea=true;
            if(state_sea)number_sea+=0.5f;
            if(!state_sea)number_sea-=0.5f;

            if(state_crash){//충돌
                crash_angle+=2;
                crash_height+=0.1f;
                if(crash_angle>=90f){
                    crash_angle=0;
                    crash_height=0f;
                    state_ship_reset=true;
                    state_crash=false;
                }
            }
            if(treasure_count>treasure_max){
                treasure_max=treasure_count;
            }
            if(state_ship_reset){//배를 섬으로 초기화
                xmove=-3;
                zmove=-1.5f;
                state_ship_reset=false;
            }
            if(state_rain) {
                rain_height+=0.005f;
                rainmove1 += 1.5f;
                rainmove2 += 1.7f;
                rainmove3 += 1.3f;
                rainmove4 += 1.6f;
                if (rainmove1 >= 20) rainmove1 = 0;
                if (rainmove2 >= 21) rainmove2 = 0;
                if (rainmove3 >= 22) rainmove3 = 0;
                if (rainmove4 >= 23) rainmove4 = 0;
            }
            if(!state_rain)rain_height=0;


            if(xmove>25)xmove=-25;
            if(xmove<-25)xmove=25;
            if(zmove>25)zmove=-25;
            if(zmove<-25)zmove=25;

            if (state_shark) {
                angle += 2.0f;
                if (angle >= 360.0f) {
                    angle -= 360.0f;
                }
            }
            if (state_ocean) {
                if (water_height <= 5.0f)
                water_height += 0.2f;
            }
            else if(state_ocean==false){
                if(water_height >= 0.0f)
                water_height -= 0.2f;
            }
            fishmove+=0.2f;
            if(fishmove>=30) {
                fishmove=-30f;
            }
            fishmove1+=0.3f;
            if(fishmove1>=30) {
                fishmove1=-30f;
            }
            fishmove2+=0.4f;
            if(fishmove2>=30) {
                fishmove2=-30f;
            }
            cloudmove += 0.08f;
            if (cloudmove >= 25.0f) {
                cloudmove = 0.0f;
            }
            cloudmove2 += 0.15f;
            if (cloudmove2 >= 25.0f) {
                cloudmove2 = 0.0f;
            }
            cloudmove3 += 0.18f;
            if (cloudmove3 >= 30.0f) {
                cloudmove3 = -7.0f;
            }
            cloudmove5 += 0.1f;
            if (cloudmove5 >= 30.0f) {
                cloudmove5 = -16.0f;
            }
            cloudmove6 += 0.03f;
            if (cloudmove6 >= 30.0f) {
                cloudmove6 = -8.0f;
            }
            cloudmove7 += 0.04f;
            if (cloudmove7 >= 30.0f) {
                cloudmove7 = -18.0f;
            }
            cloudmove8 += 0.08f;
            if (cloudmove8 >= 30.0f) {
                cloudmove8 = -15.0f;
            }

            //새움직임
            birdmove += 0.1f;
            if (birdmove >= 25.0f) {
                birdmove = -25.0f;
            }
            birdmove2 += 0.1f;
            if (birdmove2 >= 25.0f) {
                birdmove2 = -250.0f;
            }
            if(abs(-treasure_x-shipposition_x)<=2.5&&abs(-treasure_z-shipposition_y)<=2f&&(!state_coin)){//보물상자 먹으면
                treasure_count=treasure_count+1;
                state_coin=true;
            }
            if(state_coin){
                coin_height+=0.03;
                if(coin_height>1.7f){
                    coin_height=0;
                    state_coin=false;
                    treasure_x=(int)(random()*35-20);
                    treasure_z=(int)(random()*35-20);
                }
                coin_angle+=30;
                if(coin_angle>=360)coin_angle -= 360.0f;
            }

            if(abs(sharkposition_x-shipposition_x)<=2.5&&abs(sharkposition_y-shipposition_y)<=2f){//상어와 충돌
       //         Log.i(this.getClass().getName(), "@@@@@@@@@@@충돌@@@@@@@@@@@");
                treasure_count=0;//배가 침몰하면 동전 초기화
                state_crash=true;
            }
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            SampleUtils.checkGLError("VirtualButtons renderFrame");
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();

    }


    private Buffer fillBuffer(float[] array) {
        // Convert to floats because OpenGL doesnt work on doubles, and manually
        // casting each input value would take too much time.
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length); // each
        // float
        // takes 4
        // bytes
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();

        return bb;

    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;
    }

}
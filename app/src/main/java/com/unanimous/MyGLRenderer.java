package com.unanimous;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.unanimous.MyLists.MyDrawingList;
import com.unanimous.MyLists.MyShapeList;
import com.unanimous.Shapes.MySprite;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.support.constraint.Constraints.TAG;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final Context mActivityContext;

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private static float zoom = -4.0f;

    private static MyShapeList myShapeList;
    private static MyDrawingList myDrawingList;

    public static void transpose(float tvalues[]) {
        myDrawingList.transpose(tvalues);
    }
    public static void scale(float svalues[]) { myDrawingList.scale(svalues); }
    public static void rotate(float tvalue) { myDrawingList.rotate(tvalue); }

    public MyGLRenderer(final Context activityContext)
    {
        mActivityContext = activityContext;
    }

    // drawing image examples
    MySprite mySprite;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Make Surface by MAP+PATH
        myShapeList = new MyShapeList();
        myDrawingList = new MyDrawingList(myShapeList);
        System.out.println("make image");
        //mySprite = new MySprite(this.mActivityContext);
    }

    public void cleanAll() {myShapeList.cleanVectors(); myShapeList.cleanPoints();}

    public void cleanVectors() {myShapeList.cleanVectors();}

    public void cleanPoints() {myShapeList.cleanPoints();}

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, zoom,
                0f, 0f, 0f, 0.0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        try {
            myDrawingList.draw(vPMatrix);
        } catch (NoSuchElementException nsee) {
            Log.e("MyGLRenderer ", "NoSuchElementException Occur"+nsee.getMessage());
        }

        //mySprite.Draw(vPMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        float ratio = (float) width/height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Log.e("MyGLRenderer","onSurfaceChanged - "+String.valueOf(ratio));
    }

    // Shaders contain OpenGL Shading Language (GLSL) code that must be compiled
    // prior to using it in the OpenGL ES environment.
    // To compile this code, create a utility method in your renderer class.
    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}

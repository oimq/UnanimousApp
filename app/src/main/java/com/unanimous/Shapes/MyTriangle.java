package com.unanimous.Shapes;

import android.opengl.GLES20;
import android.util.Log;

import com.unanimous.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.unanimous.MyUtils.str;

public class MyTriangle {
    // This matrix member variable provides a hook to manipulate
    // the coordinates of the objects that use this vertex shader
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    // in this case : two triangle
    private final int COORDS_PER_VERTEX_NUM = 3;
    private final int COORDS_NUM = 3;
    private float POINT_SIZE_X = 0.1f;
    private float POINT_SIZE_Y = 0.1f;

    private float triangleCoords[] = new float[COORDS_PER_VERTEX_NUM*COORDS_NUM];

    private float ptrCoords[] = new float[COORDS_NUM];
    private float fixCoords[] = new float[COORDS_NUM];

    // 4 bytes per vertex
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX_NUM;
    private final int vertexStride = COORDS_PER_VERTEX_NUM * 4; // 4 bytes per vertex
    // color for shape : light green
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public MyTriangle(float coords[]) throws Exception {
        // initialize square coordinates
        for (int i = 0; i < coords.length; i++) ptrCoords[i] = fixCoords[i] = coords[i];
        convertPtrToTri();

        Log.e("MyTriangle","Initializing "+
                str(ptrCoords[0])+","+str(ptrCoords[1])+","+str(ptrCoords[2]));

        // initialize vertex byte buffer for shape coordinates
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();

        // put the coordinates in buffer
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void update() {
        vertexBuffer.clear();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);
    }

    // This line coord can be drawn on screen?
    public boolean isCan(float x_min, float x_max, float y_min, float y_max) {
        if ((x_min < ptrCoords[0] && ptrCoords[0] < x_max ) && (y_min < ptrCoords[1] && ptrCoords[1] < y_max ))
            return true;
        return false;
    }

    // Transposition the coordinates
    public void transpose(float dx, float dy, float dz) {
        ptrCoords[0] += dx;
        ptrCoords[1] += dy;
        //ptrCoords[2] += dz;
        convertPtrToTri();
        getRadAndLen();
    }

    public void print() {
        Log.e("MyTriangle", "Point ("
                + str(triangleCoords[0]) + ", " + str(triangleCoords[1]) + ", " + str(triangleCoords[2]) + "), ("
                + str(triangleCoords[3]) + ", " + str(triangleCoords[4]) + ", " + str(triangleCoords[5]) + "), ("
                + str(triangleCoords[6]) + ", " + str(triangleCoords[7]) + ", " + str(triangleCoords[8]) + ")");
        Log.e("MyTriangle ", "rotate, rad : " + str((float) rad));
    }

    // Transposition the coordinates
    public void scale(float dx, float dy, float dz) {
        ptrCoords[0] *= dx;
        ptrCoords[1] *= dy;
        //ptrCoords[2] *= dz;
        POINT_SIZE_X *= dx;
        POINT_SIZE_Y *= dy;
        getRadAndLen();
    }

    private double len = 0.0;
    private double rad = 0.0;
    public void rotate(float radd) {
        // update radians
        rad += radd;
        ptrCoords[0] = (float)(len*Math.cos(rad)); ptrCoords[1] = (float)(len*Math.sin(rad));
        convertPtrToTri();
    }

    public void getRadAndLen() {
        len = Math.sqrt((ptrCoords[0]*ptrCoords[0])+(ptrCoords[1]*ptrCoords[1]));
        try {
            rad = Math.atan((ptrCoords[1]) / (ptrCoords[0]));
            if (ptrCoords[0]<0) rad += Math.PI;
        } catch (ArithmeticException e) {
            rad = ptrCoords[1]>0?-Math.PI/2:Math.PI/2;
        }
    }

    public void convertPtrToTri() {
        triangleCoords[0] = ptrCoords[0];              triangleCoords[1] = ptrCoords[1]+POINT_SIZE_Y; triangleCoords[2] = 0;
        triangleCoords[3] = ptrCoords[0]-POINT_SIZE_X; triangleCoords[4] = ptrCoords[1]-POINT_SIZE_Y; triangleCoords[5] = 0;
        triangleCoords[6] = ptrCoords[0]+POINT_SIZE_X; triangleCoords[7] = ptrCoords[1]-POINT_SIZE_Y; triangleCoords[8] = 0;
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        update();
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX_NUM,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

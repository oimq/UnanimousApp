package com.unanimous.Shapes;

import android.opengl.GLES20;
import android.util.Log;

import com.unanimous.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.unanimous.MyUtils.str;

public class MyLine {
    // This matrix member variable provides a hook to manipulate
    // the coordinates of the objects that use this vertex shader
    private final String vertexShaderCode =
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    private final String fragmentShaderCode =
                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // Buffer for coords
    private final FloatBuffer vertexBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // Number of coordinates per vertex in this array
    // In this case : one line
    static final int COORDS_PER_VERTEX_NUM = 3;
    static final int COORDS_NUM = 2;

    // Set line list
    private float lineCoords[] = new float[COORDS_NUM*COORDS_PER_VERTEX_NUM];
    private float fixCoords[] = new float[COORDS_NUM*COORDS_PER_VERTEX_NUM];
    //private float originalCoords[] = new float[COORDS_NUM*COORDS_PER_VERTEX_NUM];

    // 4 bytes per vertex
    private final int vertexStride = COORDS_PER_VERTEX_NUM * 4;//(bytes)
    // color for shape : light green
    float color[] = { 0.0f, 0.0f, 0.0f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public MyLine(float coords[]) throws Exception {
        //Log.e("MyLine ", "Constructor - "
        //        +"("+String.valueOf(coords[0])+", "+String.valueOf(coords[1])+", "+String.valueOf(coords[2])+"), ("
        //        +"("+String.valueOf(coords[3])+", "+String.valueOf(coords[4])+", "+String.valueOf(coords[5])+")");

        // initialize square coordinates
        if (coords.length != COORDS_PER_VERTEX_NUM * COORDS_NUM)
            throw new Exception("MySquare Initializer - coords num is wrong!");
        for (int i = 0; i < coords.length; i++) lineCoords[i] = coords[i];
        transaction();

        // initialize vertex byte buffer for shape coordinates
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(lineCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        // change bytebuffer to floatbuffer
        vertexBuffer = bb.asFloatBuffer();

        // initialize byte buffer for the draw list - skip

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public float[] getCoords() {
        return lineCoords;
    }

    public void update() {
        vertexBuffer.clear();
        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);
    }

    // This line coord can be drawn on screen?
    public boolean isCan(float x_min, float x_max, float y_min, float y_max) {
        if ((x_min < lineCoords[0] && lineCoords[0] < x_max ) && (y_min < lineCoords[1] && lineCoords[1] < y_max ))
            return true;
        if ((x_min < lineCoords[3] && lineCoords[3] < x_max ) && (y_min < lineCoords[4] && lineCoords[4] < y_max ))
            return true;
        return false;
    }

    // Transposition the coordinates
    public void transpose(float dx, float dy, float dz) {
        lineCoords[0] += dx; lineCoords[3] += dx;
        lineCoords[1] += dy; lineCoords[4] += dy;
        lineCoords[2] += dz; lineCoords[5] += dz;
        transaction();
    }

    public void print() {
        Log.e("MyLine",
                "start (" + str(lineCoords[0]) + ", " + str(lineCoords[1]) + ", " + str(lineCoords[2]) +
                        "), end (" + str(lineCoords[3]) + ", " + str(lineCoords[4]) + ", " + str(lineCoords[5]) + ")");
        Log.e("MyLine ", "rotate, rads : " + str((float) rads) + ", rade : " + str((float) rade));
    }

    // Transposition the coordinates
    public void scale(float dx, float dy, float dz) {
        lineCoords[0] *= dx; lineCoords[3] *= dx;
        lineCoords[1] *= dy; lineCoords[4] *= dy;
        lineCoords[2] *= dz; lineCoords[5] *= dz;
        transaction();
    }

    private double lens = 0.0;
    private double lene = 0.0;
    private double rads = 0.0;
    private double rade = 0.0;
    public void rotate(float radd) {
        // update radians
        rads += radd;
        rade += radd;
        lineCoords[0] = (float)(lens*Math.cos(rads)); lineCoords[3] = (float)(lene*Math.cos(rade));
        lineCoords[1] = (float)(lens*Math.sin(rads)); lineCoords[4] = (float)(lene*Math.sin(rade));
    }

    public void transaction() {
        for (int i = 0; i < lineCoords.length; i++) fixCoords[i] = lineCoords[i];
        lens = Math.sqrt((fixCoords[0]*fixCoords[0])+(fixCoords[1]*fixCoords[1]));
        lene = Math.sqrt((fixCoords[3]*fixCoords[3])+(fixCoords[4]*fixCoords[4]));
        try {
            rads = Math.atan((fixCoords[1]) / (fixCoords[0]));
            if (fixCoords[0]<0) rads += Math.PI;
        } catch (ArithmeticException e) {
            rads = fixCoords[1]>0?-Math.PI/2:Math.PI/2;
        }
        try {
            rade = Math.atan((fixCoords[4]) / (fixCoords[3]));
            if (fixCoords[3]<0) rade += Math.PI;
        } catch (ArithmeticException e) {
            rade = fixCoords[4]>0?-Math.PI/2:Math.PI/2;
        }
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Put the coordinates in buffer
        // edit by park - update
        update();
//        Log.e("MyLine ", "draw - "
//                +"("+String.valueOf(fixCoords[0])+", "+String.valueOf(fixCoords[1])+", "+String.valueOf(fixCoords[2])+"), ("
//                +"("+String.valueOf(fixCoords[3])+", "+String.valueOf(fixCoords[4])+", "+String.valueOf(fixCoords[5])+")");

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX_NUM, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer);

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

        // Draw the line
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, COORDS_NUM);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

package com.unanimous.Shapes;

import android.opengl.GLES20;
import android.util.Log;

import com.unanimous.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.unanimous.MyUtils.str;

public class MySquare {
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

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    // in this case : two triangle
    private final int COORDS_PER_VERTEX_NUM = 4;
    private final int COORDS_NUM = 3;
    private final float POINT_SIZE = 0.05f;

    private float squareCoords[] = new float[COORDS_NUM*COORDS_PER_VERTEX_NUM];

    private float ptrCoords[] = new float[COORDS_NUM];
    private float fixCoords[] = new float[COORDS_NUM];

    // order to draw vertices
    private final short drawOrder[] = {
            0, 1, 2, 0, 2, 3 };

    // 4 bytes per vertex
    private final int vertexStride = COORDS_NUM * 4;
    // color for shape : light green
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public MySquare(float coords[]) throws Exception {
        // initialize square coordinates
        if (coords.length != COORDS_NUM)
            throw new Exception("MySquare Initializer - coords num is wrong!");
        for (int i = 0; i < coords.length; i++) ptrCoords[i] = coords[i];
        transaction();

        // initialize vertex byte buffer for shape coordinates
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();

        // put the coordinates in buffer
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();

        // put the draw orders in buffer
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public float[] getCoords() {
        return ptrCoords;
    }

    public void update() {
        vertexBuffer.clear();
        vertexBuffer.put(squareCoords);
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
        ptrCoords[2] += dz;
        transaction();
    }

    public void print() {
        Log.e("MySquare", "Point (" + str(squareCoords[0]) + ", " + str(squareCoords[1]) + ", " + str(squareCoords[2]) + ")");
        Log.e("MySquare ", "rotate, rad : " + str((float) rad));
    }

    // Transposition the coordinates
    public void scale(float dx, float dy, float dz) {
        ptrCoords[0] *= dx;
        ptrCoords[1] *= dy;
        ptrCoords[2] *= dz;
        transaction();
    }

    private double len = 0.0;
    private double rad = 0.0;
    public void rotate(float radd) {
        // update radians
        rad += radd;
        ptrCoords[0] = (float)(len*Math.cos(rad)); ptrCoords[1] = (float)(len*Math.sin(rad));
        invertPtrToSqr();
    }

    public void transaction() {
        for (int i = 0; i < ptrCoords.length; i++) fixCoords[i] = ptrCoords[i];
        len = Math.sqrt((fixCoords[0]*fixCoords[0])+(fixCoords[1]*fixCoords[1]));
        try {
            rad = Math.atan((fixCoords[1]) / (fixCoords[0]));
            if (fixCoords[0]<0) rad += Math.PI;
        } catch (ArithmeticException e) {
            rad = fixCoords[1]>0?-Math.PI/2:Math.PI/2;
        }
        invertPtrToSqr();
    }

    public void invertPtrToSqr() {
        squareCoords[0] = -0.5f; squareCoords[1] = 0.5f; squareCoords[2] = 0;
        squareCoords[3] = -0.5f; squareCoords[4] = -0.5f; squareCoords[5] = 0;
        squareCoords[6] = 0.5f; squareCoords[7] = -0.5f; squareCoords[8] = 0;
        squareCoords[9] = 0.5f; squareCoords[10] = 0.5f; squareCoords[11] = 0;
//        squareCoords[0] = ptrCoords[0]-POINT_SIZE; squareCoords[1] = ptrCoords[1]+POINT_SIZE; squareCoords[2] = 0;
//        squareCoords[3] = ptrCoords[0]-POINT_SIZE; squareCoords[4] = ptrCoords[1]-POINT_SIZE; squareCoords[5] = 0;
//        squareCoords[6] = ptrCoords[0]+POINT_SIZE; squareCoords[7] = ptrCoords[1]-POINT_SIZE; squareCoords[8] = 0;
//        squareCoords[9] = ptrCoords[0]+POINT_SIZE; squareCoords[10] = ptrCoords[1]+POINT_SIZE; squareCoords[11] = 0;
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        update();

        //Log.e("MyPoint ", "draw - "+"("+String.valueOf(ptrCoords[0])+", "+String.valueOf(ptrCoords[1])+")");

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

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

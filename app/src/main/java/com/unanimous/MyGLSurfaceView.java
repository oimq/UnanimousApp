package com.unanimous;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.unanimous.MyDB.MyDBHandler;

import java.util.ArrayList;

import static com.unanimous.MyUtils.str;

public class MyGLSurfaceView extends GLSurfaceView {
    private boolean debug = true;
    public void debugging(String text) {
        if (debug) {
            Log.e("MyGLSurfaceView", text);
        }
    }

    public static float SCREEN_WIDTH;
    public static float SCREEN_HEIGHT;
    private float bwidth, bheight;

    private final MyGLRenderer renderer;
    private MyMapHandler myMapHandler;

    public MyGLSurfaceView(Context context, MyMapHandler MyMapHandler) {
        super(context);
        // Set maphandler
        this.myMapHandler = MyMapHandler;

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        renderer = new MyGLRenderer(context);
        setRenderer(renderer);

        // Set width and height
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_HEIGHT = size.y;
        SCREEN_WIDTH = size.x;
        bwidth = size.x/2;
        bheight = size.y/2;

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public final static int TOUCH_RELEASE = 1;

    public boolean is_first = true;
    private float ocor[] = new float[3];    // original coordinates
    private float tcor[] = new float[3];    // touch coordinates & distance
    private float mcor[] = new float[3];    // middle coordinates that is touche
    private float otheta = 0.0f;
    private float mtheta = 0.0f;

    // Calculate middle point coordinates of two fingers and square of distance.
    public float[] calcMD(float x0, float y0, float x1, float y1) {
        debugging("Two finger events, x0 : "+str(x0)+", y0 : "+str(y0)+", x1 : "+ str(x1)+", y1 : "+str(y1));
        // find the middle points. and distance
        return new float[]{(x0+x1)/2, (y0+y1)/2, (float)Math.sqrt((x0-x1)*(x0-x1)+(y0-y1)*(y0-y1))};
    }

    // Calculation of vector angle
    public double calcT(float x0, float y0, float x1, float y1) {
        return Math.atan((x0-x1)/(y0-y1));
    }

    private float hcor[] = new float[2];
    private float bcor[] = new float[2];
    private float scor[] = new float[2];

    public float[] get() { return new float[]{scor[0], scor[1]};}

    private float ccor[] = {1.0f, 1.0f};
    private float dcor[] = {0.0f, 0.0f};
    public void reset_move() {
        ccor[0] = 1.0f; ccor[1] = 1.0f;
        dcor[0] = 0.0f; dcor[1] = 0.0f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        debugging("Touch Action - "+String.valueOf(e.getAction()));
        float tcor[] = {0f, 0f, 0f};
        float ttheta = 0f;
        switch (e.getPointerCount()) {
            case 1 :
                hcor[0] = tcor[0] = e.getX();
                hcor[1] = tcor[1] = e.getY();
                bcor[0] = (hcor[0]-bwidth)/bwidth;
                bcor[1] = ((hcor[1]-bheight)/bwidth);
                scor[0] = bcor[0]*ccor[0]*2+dcor[0];
                scor[1] = bcor[1]*ccor[1]*2+dcor[1];
                debugging("One finger events, x : "+String.valueOf(tcor[0])+", y : "+String.valueOf(tcor[1]));
                debugging("----- hx : "+String.valueOf(hcor[0])+", hy : "+String.valueOf(hcor[1]));
                debugging("----- tcor : "+str(tcor[0])+", "+str(tcor[1])+", "+str(tcor[2]));
                debugging("----- ttheta : "+str(ttheta));
                debugging("----- ocor : "+str(ocor[0])+", "+str(ocor[1])+", "+str(ocor[2]));
                debugging("----- otheta : "+str(otheta));
                debugging("##### bcor : "+str(bcor[0])+", "+str(bcor[1]));
                debugging("##### ccor : "+str(ccor[0]*2)+", "+str(ccor[1]*2));
                debugging("##### dcor : "+str(dcor[0])+", "+str(dcor[1]));
                debugging("@@@@@ scor : "+str(scor[0])+", "+str(scor[1]));

                break;
            case 2 :
                tcor = calcMD(e.getX(0), e.getY(0), e.getX(1), e.getY(1));
                ttheta = (float)calcT(e.getX(0), e.getY(0), e.getX(1), e.getY(1));
                if (is_first) {
                    is_first = false;
                    debugging("First coordinates, mcorx : "+str(mcor[0])+", mcory : "+str(mcor[1])+", scale : "+str(mcor[2])+", theta : "+str(ttheta));
                }
                else {
                    // make the move of map
                    float mx = (mcor[0] - tcor[0])/SCREEN_WIDTH*2;
                    float my = (mcor[1] - tcor[1])/SCREEN_HEIGHT*2;
                    float scale = tcor[2]/mcor[2];
                    float rotate = mtheta - ttheta;
                    debugging("mx : "+str(mx)+", my : "+ str(my)+", scale : "+str(scale)+", theta : "+str(ttheta));
                    MyGLRenderer.transpose(new float[]{mx, my, 0});
                    MyGLRenderer.scale(new float[]{scale, scale, 1});
                    //MyGLRenderer.rotate(rotate);
                    requestRender();

                    dcor[0] += mx*ccor[0];
                    dcor[1] += my*ccor[1];
                    ccor[0] /= scale;
                    ccor[1] /= scale;
                }
                mcor[0] = tcor[0];
                mcor[1] = tcor[1];
                mcor[2] = tcor[2];
                mtheta = ttheta;
                break;
        }
        if (e.getAction() == TOUCH_RELEASE) {
            is_first = true;
        }
        return true;
    }
}

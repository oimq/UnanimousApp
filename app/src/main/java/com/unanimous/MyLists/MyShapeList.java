package com.unanimous.MyLists;

import android.util.Log;

import com.unanimous.MyMapHandler;
import com.unanimous.Shapes.MyPoint;
import com.unanimous.Shapes.MyVector;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * This class is that converts a coordinate string
 * read from a list file into a vector or point
 * and converts it to a float list.
 *
 * avaliable shape : Line
 */


public class MyShapeList {
    private boolean debug = true;
    public void debugging(String msg) { if (debug) Log.e("MyShapeList ", msg); }

    ArrayList<MyVector> vectors = new ArrayList<MyVector>();
    ArrayList<MyPoint> points = new ArrayList<MyPoint>();

    public MyShapeList() {
        addVectors(MyMapHandler.getMap_vec());
        addPoints(MyMapHandler.getMap_ptr());
    }

    public void addVectors(ArrayList<float[]> floarr) {
        debugging("Vector number : "+String.valueOf(floarr.size()));
        Iterator<float[]> iter = floarr.iterator();
        float[] cors;
        while (iter.hasNext()) {
            cors = iter.next();
            try {
                vectors.add(new MyVector(new float[]{cors[0], cors[1], 0.0f, cors[2], cors[3], 0.0f}));
                debugging("x1="+cors[0]+", y1="+cors[1]+", x2="+cors[2]+", y2="+cors[3]);
            } catch (Exception e) {
                Log.e("MyShapeList", e.getMessage());
            }
        }
    }

    public void addPoints(ArrayList<float[]> floarr) {
        debugging("Point number : "+String.valueOf(floarr.size()));
        Iterator<float[]> iter = floarr.iterator();
        float[] cors;
        while (iter.hasNext()) {
            cors = iter.next();
            try {
                points.add(new MyPoint(new float[]{cors[0], cors[1], 0.0f}));
                debugging("x="+cors[0]+", y="+cors[1]);
            } catch (Exception e) {
                Log.e("MyShapeList", e.getMessage());
            }
        }
    }

    public void cleanVectors() {
        vectors.clear();
    }

    public void cleanPoints() {
        points.clear();
    }

    public void cleanall() {
        cleanVectors();
        cleanPoints();
    }

    public ArrayList<MyVector> getVectors() {
        return this.vectors;
    }

    public ArrayList<MyPoint> getPoints() { return this.points; }
}

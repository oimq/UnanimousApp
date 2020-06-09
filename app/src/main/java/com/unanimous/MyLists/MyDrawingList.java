package com.unanimous.MyLists;

import com.unanimous.Shapes.MyPoint;
import com.unanimous.Shapes.MyVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyDrawingList {
    // Canvas range
    public final static float CRX[] = {-3.0f, 3.0f};
    public final static float CRY[] = {-2.0f, 2.0f};

    // all Vector(Line) components in myVectors
    MyShapeList myShapeList;
    Iterator<MyVector> iterv;
    Iterator<MyPoint> iterp;

    public MyDrawingList(MyShapeList myList) {
        myShapeList = myList;
        classify();
    }

    // classify vectors which are can or can't
    public void classify() {
        iterv = myShapeList.getVectors().iterator();
        while (iterv.hasNext()) iterv.next().isCan(CRX[0], CRX[1], CRY[0], CRY[1]);
        iterp = myShapeList.getPoints().iterator();
        while (iterp.hasNext()) iterp.next().isCan(CRX[0], CRX[1], CRY[0], CRY[1]);
    }

    public void transpose(float tvalues[]) {
        iterv = myShapeList.getVectors().iterator();
        while (iterv.hasNext()) iterv.next().transpose(tvalues);
        iterp = myShapeList.getPoints().iterator();
        while (iterp.hasNext()) iterp.next().transpose(tvalues);
        classify();
    }

    public void scale(float svalues[]) {
        iterv = myShapeList.getVectors().iterator();
        while (iterv.hasNext()) iterv.next().scale(svalues);
        iterp = myShapeList.getPoints().iterator();
        while (iterp.hasNext()) iterp.next().scale(svalues);
        classify();
    }

    public void rotate(float tvalue) {
        iterv = myShapeList.getVectors().iterator();
        while (iterv.hasNext()) iterv.next().rotate(tvalue);
        iterp = myShapeList.getPoints().iterator();
        while (iterp.hasNext()) iterp.next().rotate(tvalue);
        classify();
    }

    public void draw(float[] mvpMatrix) throws NoSuchElementException {
        iterv = myShapeList.getVectors().iterator();
        while (iterv.hasNext()) iterv.next().draw(mvpMatrix);
        iterp = myShapeList.getPoints().iterator();
        while (iterp.hasNext()) iterp.next().draw(mvpMatrix);
    }
}

package com.unanimous.Shapes;

public class MyVector {
    static final int COORDS_PER_VERTEX_NUM = 3;
    static final int COORDS_NUM = 2;
    private boolean enable = true;

    MyLine myline;

    public MyVector(float coords[]) throws Exception {
        if (coords.length != COORDS_PER_VERTEX_NUM*COORDS_NUM)
            throw new Exception("MyPoint Initializer - coords num is wrong!");
        myline = new MyLine(new float[]{
                coords[0], coords[1], coords[2],
                coords[3], coords[4], coords[5]
        });
    }

    public void transpose(float tvalues[]) {
        myline.transpose(tvalues[0], tvalues[1], tvalues[2]);
    }

    public void scale(float svalues[]) {
        myline.scale(svalues[0], svalues[1], svalues[2]);
    }

    public void rotate(float tvalue) { myline.rotate(tvalue); }

    public void print() {myline.print();}

    public void draw(float[] mvpMatrix) {
        if (enable) myline.draw(mvpMatrix);
    }

    public void isCan(float x_min, float x_max, float y_min, float y_max) {
        if (myline.isCan(x_min, x_max, y_min, y_max))
                this.enable = true;
        else    this.enable = false;
    }
}

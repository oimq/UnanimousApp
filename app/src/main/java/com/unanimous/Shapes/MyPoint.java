package com.unanimous.Shapes;

public class MyPoint {
    static final int COORDS_PER_VERTEX_NUM = 3;
    private boolean enable = true;

    MyTriangle myTriangle;
    public MyPoint(float coords[]) throws Exception {
        if (coords.length != COORDS_PER_VERTEX_NUM)
            throw new Exception("MyPoint Initializer - coords num is wrong!");
        myTriangle = new MyTriangle(new float[]{
                coords[0], coords[1], coords[2]
        });
    }

    public void transpose(float tvalues[]) {
        myTriangle.transpose(tvalues[0], tvalues[1], tvalues[2]);
    }

    public void scale(float svalues[]) {
        myTriangle.scale(svalues[0], svalues[1], svalues[2]);
    }

    public void rotate(float tvalue) { myTriangle.rotate(tvalue); }

    public void print() { myTriangle.print();}

    public void draw(float[] mvpMatrix) { if (enable) myTriangle.draw(mvpMatrix); }

    public void isCan(float x_min, float x_max, float y_min, float y_max) {
        if (myTriangle.isCan(x_min, x_max, y_min, y_max))
                this.enable = true;
        else    this.enable = false;
    }
}

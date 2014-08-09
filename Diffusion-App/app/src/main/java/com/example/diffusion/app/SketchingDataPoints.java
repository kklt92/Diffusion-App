package com.example.diffusion.app;

/**
 * Created by Sam on 11/07/2014.
 */
public class SketchingDataPoints {

    /* Instance variables */
    private float minX, maxX, midX; //min and max hold the coordinate values for the grid, mid is used for plotting points
    private float yPosition; //holds the y value coordinate

    /* Constructor */
    public SketchingDataPoints(float minX, float maxX){
        this.minX = minX; this.maxX = maxX;
        this.midX = (this.minX + this.maxX)/2.0f;
    }//end of constructor

    /* Getters/Setters for the instance variables */
    public void setMinX(float minX){this.minX = minX;}

    public float getMinX(){return this.minX;}

    public void setMaxX(float maxX){this.maxX = maxX;}

    public float getMaxX(){return this.maxX;}

    public void setMidX(float midX){this.midX = midX;}

    public float getMidX(){return this.midX;}

    public void setYPosition(float yPos){this.yPosition = yPos;}

    public float getYPosition(){return this.yPosition;}

}//end of class

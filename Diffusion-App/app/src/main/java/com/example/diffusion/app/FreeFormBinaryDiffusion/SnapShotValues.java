package com.example.diffusion.app.FreeFormBinaryDiffusion;

/**
 * Class which packages a set of concentration values and a colour together
 *
 * Created by Sam on 18/07/2014.
 */
public class SnapShotValues {

    private float[] snapshotValues;
    private int colour;
    //private int timeStamp; - to be used in the future for plotting values

    /* Constructor */
    public SnapShotValues(float[] snapshotValues, int colour){

        this.snapshotValues = snapshotValues; this.colour = colour;

    }//end of constructor

    /* Getters/Setters */

    public void setColour(int colour) {
        this.colour = colour;
    }

    public int getColour() {
        return colour;
    }

    public void setSnapshotValues(float[] snapshotValues) {
        this.snapshotValues = snapshotValues;
    }

    public float[] getSnapshotValues() {
        return snapshotValues;
    }

    public int size(){
        return this.snapshotValues.length;
    }
}//end of SnapShotValues class

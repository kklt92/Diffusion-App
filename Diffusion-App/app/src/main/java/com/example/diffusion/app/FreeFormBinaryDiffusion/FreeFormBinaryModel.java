package com.example.diffusion.app.FreeFormBinaryDiffusion;

import java.util.Arrays;

/**
 * Model class for One Dimensional Diffusion Equation.
 *
 *  NOTE: the values stored in the DataPoint array are actual values -
 *      before they are plotted the values will have to be scaled to the height of the animation view
 *
 * Created by Sam on 14/07/2014.
 */
public class FreeFormBinaryModel {


    private int numberOfGridPoints;
    private int totalGridPoints; //this includes the grid points that act as boundaries
    private int numberOfTimeSteps;
    private String boundaryConditions;

    private float[] initialValues; //these are the values that are originally passed, used for reset operations
    private float[] plottingValues; //passed on creation, set to
    private float[] solutionValues; //holds values that are used in the solution (need to be converted before being plotted)
    private int currentTimeStep; //holds the current time step

    //the below are constants are used in the method to the solution
    private double diffusionCoefficient;
    private double maxDiffusionCoefficient;
    private double deltaX;
    private double deltaT;
    private double deltaTFactor;

    private int animationViewHeight; //used for scaling the concentration values

    private float previousValue; //used in solution method (holds the previous value of the west grid point)

    /* Constructor */
    public FreeFormBinaryModel(float[] plottingValues, int animationViewHeight, String boundaryConditions, double deltaTFactor){

        this.animationViewHeight = animationViewHeight;
        this.boundaryConditions = boundaryConditions;
        this.deltaTFactor = deltaTFactor;
        this.diffusionCoefficient = 0.5;

        this.plottingValues = plottingValues;
        this.initialValues = Arrays.copyOf(plottingValues, plottingValues.length);

        this.numberOfGridPoints = plottingValues.length;
        this.totalGridPoints = this.numberOfGridPoints + 2;

        this.solutionValues = new float[totalGridPoints]; //to include the boundary values

        this.deltaX = 1.0/(numberOfGridPoints-1); //TODO check whether this needs to be total or number of grid points

        this.deltaT = ((deltaX*deltaX)/(2*diffusionCoefficient))*this.deltaTFactor;

        this.maxDiffusionCoefficient = (deltaX*deltaX)/(2*deltaT);

        this.diffusionCoefficient = maxDiffusionCoefficient/2.0;

        setUpSolutionArray();

    }//end of constructor

    /* ++++++++++++++++ Getters/Setters ++++++++++++++++++++++ */

    public void setNumberOfGridPoints(int numberOfGridPoints) {
        this.numberOfGridPoints = numberOfGridPoints;
    }

    public int getNumberOfGridPoints() {
        return numberOfGridPoints;
    }

    public void setNumberOfTimeSteps(int numberOfTimeSteps) {
        this.numberOfTimeSteps = numberOfTimeSteps;
    }

    public float getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(float previousValue) {
        this.previousValue = previousValue;
    }

    public int getNumberOfTimeSteps() {
        return numberOfTimeSteps;
    }

    public int getCurrentTimeStep(){return currentTimeStep;}

    public double getDeltaTFactor() {return this.deltaTFactor;}

    public float[] getSolutionValues(){return this.solutionValues;}

    public void setDeltaTFactor(double d){
        this.deltaT = (this.deltaT/this.deltaTFactor)*d;
        this.deltaTFactor = d;
    }

    public void setBoundaryConditions(String bc) {this.boundaryConditions = bc;}

    public String getBoundaryConditions(){ return this.boundaryConditions; }

    /* Called by the seek-bar in the animation view,  */
    public void setDiffusionCoefficient(double i) {
        this.diffusionCoefficient = ((i*maxDiffusionCoefficient)/100.0);
        if(diffusionCoefficient>=maxDiffusionCoefficient){diffusionCoefficient = maxDiffusionCoefficient-0.01;}
        if(diffusionCoefficient<=0) diffusionCoefficient = 0.01;
    }//end of setDC method


    /* Helper method for the constructor */
    public void setDataPointArray(float[] plottingValues){

        setUpSolutionArray();

    }//end of setDataPointArrayMethod

    public float[] getCurrentSolutionValues(){return this.solutionValues;}
    public float[] getPlottingValues(){return this.plottingValues;}

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    /*
     * Takes into account the boundary conditions, and sets up the array of SketchingDataPoints that will be used
     *
     */
    public void setUpSolutionArray(){

        float totalValue = 0.0f;

        //loop through plotting values, and add to solution values
        for(int i=0; i<plottingValues.length; i++){ //this shouldn't be -2 C

            float scaledValue =  animationViewHeight - plottingValues[i];

            solutionValues[i+1] = scaledValue;
            totalValue += scaledValue;
        }//end of for loop

        //set the boundary conditions to be half of the maximum value

        if(boundaryConditions.equals("Zero Flux")){
            System.out.println("created no flux");
            solutionValues[0] = solutionValues[1];
            solutionValues[solutionValues.length-1] = solutionValues[solutionValues.length-2];

        } else if (boundaryConditions.equals("Constant Value")){
            System.out.println("created constant value");
            solutionValues[0] = solutionValues[1];
            solutionValues[solutionValues.length-1] = solutionValues[solutionValues.length-2];

        } else if(boundaryConditions.equals("Periodic")){
            System.out.println("created periodic");
            solutionValues[0] = solutionValues[solutionValues.length-2];
            solutionValues[solutionValues.length-1] = solutionValues[1];

        }//end of boundary condition if block

        setPreviousValue(solutionValues[0]);
        this.currentTimeStep = 0;

    }//end of setUpSolutionArray method

    /*
     * Calculates the next set of grid point values to be plotted
     * Returns a boolean stating whether the values have been modified
     */
    public void solutionOneStep(){

            float pV = 0f;
            float currentValue = 0f;

            //perform the calculations here
            for(int i=1; i<solutionValues.length-1; i++){

                pV = solutionValues[i]; //will be used in the next step of the calculation
                currentValue = solutionValues[i];
                solutionValues[i] = (float)(((diffusionCoefficient * deltaT)/(deltaX*deltaX)) *
                        (getPreviousValue() + solutionValues[i+1] - (2*currentValue)) + currentValue);
                setPreviousValue(pV);
            }//end of for loop


        if(boundaryConditions.equals("Zero Flux")){

            solutionValues[0] = solutionValues[1];
            solutionValues[solutionValues.length-1] = solutionValues[solutionValues.length-2];

        } else if (boundaryConditions.equals("Constant Value")){

            //boundary values not updates

        } else if(boundaryConditions.equals("Periodic")){

            solutionValues[0] = solutionValues[solutionValues.length-2];
            solutionValues[solutionValues.length-1] = solutionValues[1];

        }//end of boundary condition if block


        setPreviousValue(solutionValues[0]);

        updatePlottingValues();

        this.currentTimeStep++;
        if(currentTimeStep%50==0){
            //check that these values are fine, run some oth
            System.out.println("diffusion coefficient: " + diffusionCoefficient + ", delta T factor: " + deltaTFactor + ", delta T: " + deltaT);
        }

    }//end of solutionOneStep

    /* Helper method for solutionOneStep
     *    Loops through the solution array, and updates the plotting values ready for the
     *    next image in the animation
     */
    public void updatePlottingValues(){

        for(int i=0; i<plottingValues.length; i++){

            plottingValues[i] = animationViewHeight - solutionValues[i+1]  ;

        }//end of for loop

    }//end of updatePlottingValues method

    /* Called when the animation needs to be restarted */
    public void restartAnimation(){

        this.plottingValues = Arrays.copyOf(this.initialValues, initialValues.length);
        this.currentTimeStep = 0;
        setUpSolutionArray();

    }//end of restartAnimation method

}//end of Model class

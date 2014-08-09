package com.example.diffusion.app.FreeFormTernaryDiffusion;

import java.util.Arrays;

/**
 * Contains the logic used for the ternary fee diffusion animations
 *
 * Created by Sam on 06/08/2014.
 */
public class TernaryFreeFormModel {

 /* Instance Variables */

    //These contain the coordinates from the sketching view
    private float[] species1InitialValues;
    private float[] species2InitialValues;

    //contains the molar fractions - set up initially from the above, and then updated during each solution step
    private double[] species1MolarFractions, species2MolarFractions, species3MolarFractions;

    private double[] species1ChemicalPotential, species2ChemicalPotential;

    private float[] species1PlottingValues, species2PlottingValues;

    private int viewHeight; //used for converting between coordinates and molar fraction values
    private int totalNumberOfParticles; //per grid point
    private int molarFractionArrayLengths;

    //values used in the calculations
    private double l12, l13, l23;
    private int temperature;
    private double m1, m2;
    private double deltaX, deltaT;
    private double previousMolarFraction1, previousMolarFraction2;

    private String boundaryConditions; //start with zero flux, and implement others in the future

    /* Constructor */
    public TernaryFreeFormModel(float[] values1, float[] values2, int viewHeight){

        this.species1PlottingValues = values1;
        this.species2PlottingValues = values2;
        this.viewHeight = viewHeight;
        this.totalNumberOfParticles = viewHeight*2;
        this.molarFractionArrayLengths = species1PlottingValues.length + 2;
        this.boundaryConditions = "Constant Value";

        System.out.println("******** number of particles: " + totalNumberOfParticles + " *************");


        this.species1InitialValues = Arrays.copyOf(species1PlottingValues, species1PlottingValues.length);
        this.species2InitialValues = Arrays.copyOf(species2PlottingValues, species2PlottingValues.length);

        this.l12 = -1000.0; this.l13 = -1000.0; this.l23 = -1000.0;
        this.temperature = 100;
        this.m1 = 1.0; this.m2 = 1.0;

        this.deltaX = 0.125;
        this.deltaT = 0.00000045d;

        setUpSolutionArrays();

    } //end of constructor

    /* Helper method for constructor - sets up the double[]s that will be used in solution values */
    public void setUpSolutionArrays(){

        //create the arrays, size = initialValues + 2 to allow for the boundaries
        this.species1MolarFractions = new double[molarFractionArrayLengths];
        this.species2MolarFractions = new double[molarFractionArrayLengths];
        this.species3MolarFractions = new double[molarFractionArrayLengths];

        this.species1ChemicalPotential = new double[molarFractionArrayLengths];
        this.species2ChemicalPotential = new double[molarFractionArrayLengths];

        //fill the arrays with the initial values
        for(int i=0; i< species1PlottingValues.length; i++){

            //TODO change this for now to see the effect
            double x1 = (viewHeight - species1PlottingValues[i])/totalNumberOfParticles;
            double x2 = (viewHeight - species2PlottingValues[i])/totalNumberOfParticles;
            double x3 = 1 - (x1 + x2);

            species1MolarFractions[i+1] = x1;
            species2MolarFractions[i+1] = x2;
            species3MolarFractions[i+1] = x3;

        }//end of for loop

        //add the boundary conditions
        if(boundaryConditions.equals("Constant Value")){ //change to constant value for now

            species1MolarFractions[0] = species1MolarFractions[1];
            species1MolarFractions[molarFractionArrayLengths-1] = species1MolarFractions[molarFractionArrayLengths-2];

            species2MolarFractions[0] = species2MolarFractions [1];
            species2MolarFractions[molarFractionArrayLengths-1] = species2MolarFractions[molarFractionArrayLengths-2];

            species3MolarFractions[0] = species3MolarFractions[1];
            species3MolarFractions[molarFractionArrayLengths-1] = species3MolarFractions[molarFractionArrayLengths-2];

        } else{
            //add in periodic and constant value here
        }

        previousMolarFraction1 = species1MolarFractions[0];
        previousMolarFraction2 = species2MolarFractions[0];

    }//end of setUpSolutionArrays method


    /* Getters/Setters */
    public void setPreviousMolarFraction1(double i){
        this.previousMolarFraction1 = i;
    }

    public void setPreviousMolarFraction2(double i){
        this.previousMolarFraction2 = i;
    }

    public float[] getSpecies1PlottingValues() {
        return species1PlottingValues;
    }

    public float[] getSpecies2PlottingValues() {
        return species2PlottingValues;
    }

    /* +++++++++++++++ */


    /* Updates all values in the arrays for one time step */
    public void solutionOneStep(){

        System.out.println("solution model one step has been called");

        double RT = 8.31541 * temperature;

        //first update the chemical potential arrays to hold the correct values
        for(int i=1; i<species1ChemicalPotential.length-1; i++){

            double x1 = species1MolarFractions[i]; double x2 = species2MolarFractions[i]; double x3 = species3MolarFractions[i];

            //need to stop the values becoming negative
            if(x1<=0)x1=0.1; if(x2<=0)x2=0.1; if(x3<=0)x3=0.1;


            species1ChemicalPotential[i] = (RT*x1*Math.log(x1)) + (x2*(x2+x3)*l12) + (x3*(x2+x3)*l13) - (x2*x3*l23);
            species2ChemicalPotential[i] = (RT*x2*Math.log(x2)) + (x1*(x1+x3)*l12) - (x1*x3*l13) + (x3*(x1+x3)*l23);

        }//end of for loop


        //then update the molar fraction arrays
        for(int i=1; i<species1ChemicalPotential.length-1; i++){

            double placeHolderPrevious1 = species1MolarFractions[i];
            double placeHolderPrevious2 = species2MolarFractions[i];

            double currentValue1 = species1MolarFractions[i];

            species1MolarFractions[i] = currentValue1 + (m1*deltaT)*(((species1MolarFractions[i+1]+currentValue1)/2)*((species1ChemicalPotential[i+1]-species1ChemicalPotential[i])/(deltaX*deltaX))
                                                                   - ((currentValue1+previousMolarFraction1)/2)*((species1ChemicalPotential[i]-species1ChemicalPotential[i-1])/(deltaX*deltaX)));

            setPreviousMolarFraction1(placeHolderPrevious1);

            double currentValue2 = species2MolarFractions[i];

           species2MolarFractions[i] = currentValue2 + (m2*deltaT)*(((species2MolarFractions[i+1]+currentValue2)/2)*((species2ChemicalPotential[i+1]-species2ChemicalPotential[i])/(deltaX*deltaX))
                                                                    - ((currentValue2 + previousMolarFraction2)/2)*((species2ChemicalPotential[i]-species2ChemicalPotential[i-1])/(deltaX*deltaX)));

            setPreviousMolarFraction2(placeHolderPrevious2);

            //need to update the species three molar values

        }//end of for loop

        //test updating of the 3 - quick fix then comment out
        for(int i=0; i<species3MolarFractions.length; i++){

            species3MolarFractions[i] = 1 - (species1MolarFractions[i] + species2MolarFractions[i]);

        }//end of for loop

        //update the boundary conditions
        if(boundaryConditions.equals("Zero Flux")){

            species1MolarFractions[0] = species1MolarFractions[1];
            species1MolarFractions[molarFractionArrayLengths-1] = species1MolarFractions[molarFractionArrayLengths-2];

            species2MolarFractions[0] = species2MolarFractions [1];
            species2MolarFractions[molarFractionArrayLengths-1] = species2MolarFractions[molarFractionArrayLengths-2];

            species3MolarFractions[0] = species3MolarFractions[1];
            species3MolarFractions[molarFractionArrayLengths-1] = species3MolarFractions[molarFractionArrayLengths-2];

        }else if(boundaryConditions.equals("Constant Value")){
            //constant value boundary conditions do not get updated during the run through the solution
        }//end of if/else boundary condition block

        //call method to set values that will be plotted
        setPlottingValues();
        printAllValues(); //call method that is used for debugging
    }//end of solutionOneStep method

    public void setPlottingValues(){

        for(int i=0; i<species1PlottingValues.length; i++){

            //TODO the below will need changing i think
            species1PlottingValues[i] = viewHeight - ((float)(species1MolarFractions[i+1]*totalNumberOfParticles));
            species2PlottingValues[i] = viewHeight - ((float)(species2MolarFractions[i+1]*totalNumberOfParticles));

        }//end of for loop

    }//end of setPlottingValues

    /* Called when the animation reset button is pressed */
    public void restartAnimation(){


    }//end of restartAnimation method


    /* Print method used for debugging */
    public void printAllValues(){

        //print out the molar fractions
        String spec1 = "Spec1: [";
        for(int i=0; i<species1MolarFractions.length; i++){
            spec1+= species1MolarFractions[i] + ", ";
        } spec1 += "]";
        System.out.println(spec1);

        String spec2 = "Spec2: [";
        for(int i=0; i<species2MolarFractions.length; i++){
            spec2+= species2MolarFractions[i] + ", ";
        } spec2 += "]";
        System.out.println(spec2);

        String spec3 = "Spec3: [";
        for(int i=0; i<species3MolarFractions.length; i++){
            spec3+= species3MolarFractions[i] + ", ";
        } spec3 += "]";
        System.out.println(spec3);

    }//end of printAllValues method


}//end of model class

package com.example.diffusion.app.FreeFormBinaryDiffusion;

/**
 * An object of this type is used to package parameters for storage/retrieval from
 *   the database
 *
 * Created by Sam on 25/07/2014.
 */
public class ParametersForDatabaseStorage {

    /* Instance Variables - these are stored in the database */
//    TODO for now, only basic information is stored as a test, in future, we will store the image, and the concentration values

    private int uniqueID;
    private String filename;
    private int numberOfGridPoints;
    private String boundaryConditions;
    private double deltaTValue;
    private float[] concentrationValues;
    private String concentrationValuesString;

    /* Constructors */

    //used for passing data into the database
    public ParametersForDatabaseStorage(String filename, int numberOfGridPoints, String boundaryConditions,
                                        double deltaTValue, float[] initialValues) {
        this.filename = filename;
        this.numberOfGridPoints = numberOfGridPoints;
        this.boundaryConditions = boundaryConditions;
        this.deltaTValue = deltaTValue;
        this.concentrationValues = initialValues;
        this.concentrationValuesString = getStringFromValues(this.concentrationValues);
    }//end of constructor

    //used when data is retrieved from the database
    public ParametersForDatabaseStorage(int uniqueID,String filename, int numberOfGridPoints,
                                        String boundaryConditions, double deltaTValue, String initialValuesString){
        this.uniqueID = uniqueID;
        this.filename = filename;
        this.numberOfGridPoints = numberOfGridPoints;
        this.boundaryConditions = boundaryConditions;
        this.deltaTValue = deltaTValue;
        this.concentrationValuesString = initialValuesString;
        this.concentrationValues = getValuesFromString(this.concentrationValuesString);
    }//end of constructor

    /* Getters/Setters */

    public double getDeltaTValue() {
        return deltaTValue;
    }

    public void setDeltaTValue(double deltaTValue) {
        this.deltaTValue = deltaTValue;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getNumberOfGridPoints() {
        return numberOfGridPoints;
    }

    public void setNumberOfGridPoints(int numberOfGridPoints) {
        this.numberOfGridPoints = numberOfGridPoints;
    }

    public String getBoundaryConditions() {
        return boundaryConditions;
    }

    public void setBoundaryConditions(String boundaryConditions) {
        this.boundaryConditions = boundaryConditions;
    }

    public float[] getConcentrationValues() {
        return concentrationValues;
    }

    public void setConcentrationValues(float[] concentrationValues) {
        this.concentrationValues = concentrationValues;
    }

    public String getConcentrationValuesString() {
        return concentrationValuesString;
    }

    public void setConcentrationValuesString(String concentrationValuesString) {
        this.concentrationValuesString = concentrationValuesString;
    }

    public String toString(){
        return "(ID: " + uniqueID + ", Name: " + filename + ", GP: " +
                numberOfGridPoints + ", BC: " + boundaryConditions + ", Del T: " + deltaTValue +
                "\n" + arrayValuesToString() + "\n";
    }

    public String arrayValuesToString(){
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<concentrationValues.length; i++){
            sb.append(concentrationValues[i] + ", ");
        }
        return sb.toString();
    }

    public static String getStringFromValues(float[] values){

        String separator = ",";
        StringBuffer s = new StringBuffer();

        //loop through the array and convert to a string
        for(int i=0; i<values.length; i++){

            if(i<values.length-1){

                s.append(values[i] + separator);

            }//end of if statement
            else s.append(values[i]);
        }//end of for loop

        return s.toString();

    }//end of get string from values method

    public static float[] getValuesFromString(String values){

        String[] splitValues = values.split(",");
        float[] returnValues = new float[splitValues.length];
        for(int i=0; i<returnValues.length; i++){
            returnValues[i] = Float.parseFloat(splitValues[i]);
        }
        return returnValues;
    }//end of getValues from string method

}//end of class

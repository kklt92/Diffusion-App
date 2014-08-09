package com.example.diffusion.app.FreeFormBinaryDiffusion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Handler class for the database which holds the free form data
 *
 * Created by Sam on 25/07/2014.
 */
public class FreeFormValuesDataBaseHandler extends SQLiteOpenHelper {

    //Database name and version, stored as final variables
    private static final String DATABASE_NAME = "free_form_database3.db";
    private static final int DATABASE_VERSION = 3;

    //final variables that will be used in queries - need to be added to in the future
    private static final String TABLE_NAME = "free_form_data2";
    private static final String UNIQUE_ID = "unique_id";
    private static final String ROW_NAME = "row_name";
    private static final String GRID_POINTS = "grid_points";
    private static final String BOUNDARY_CONDITIONS = "boundary_conditions";
    private static final String DELTA_T_FACTOR = "delta_t_factor";
    private static final String CONCENTRATION_VALUES = "concentration_values";
    private static final String[] COLUMNS = {UNIQUE_ID, ROW_NAME, GRID_POINTS, BOUNDARY_CONDITIONS, DELTA_T_FACTOR, CONCENTRATION_VALUES};


    /* Default constructor */
    public FreeFormValuesDataBaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }//end of default constructor


    @Override
    public void onCreate(SQLiteDatabase db){

        //the string used in the table creation:
        String CREATE_TABLE_COMMAND = "CREATE TABLE " + TABLE_NAME + " (" +
                                       UNIQUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + //this might need changing in the future
                                       ROW_NAME + " TEXT NOT NULL UNIQUE, " +
                                       GRID_POINTS + " INTEGER, " +
                                       BOUNDARY_CONDITIONS + " TEXT, " +
                                       DELTA_T_FACTOR + " REAL, " +
                                       CONCENTRATION_VALUES + " TEXT" + ");";
        System.out.println("THE FUCKING DATABASE HAS BEEN CREATED FUCK FUCK FUCK FUCK ");
        Log.w("1", "FUCKING DATABASE HAS BEEN CREATED");


        //create the database
        db.execSQL(CREATE_TABLE_COMMAND);

    }//end of onCreate method

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        //test the below with a system print out during testing, to see change between old and new numbers
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }//end of onUpgrade method

    /* Below are the methods for adding a new row to the table, and retrieving previous rows */

    //adds a new row to the table
    public void addNewEntry(ParametersForDatabaseStorage pm){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //add the passed values to the content values
        contentValues.put(ROW_NAME, pm.getFilename());
        contentValues.put(GRID_POINTS, pm.getNumberOfGridPoints());
        contentValues.put(BOUNDARY_CONDITIONS, pm.getBoundaryConditions());
        contentValues.put(DELTA_T_FACTOR, pm.getDeltaTValue());
        contentValues.put(CONCENTRATION_VALUES, pm.getConcentrationValuesString());

        //add the new values to the database and close it
      // try{
           db.insertOrThrow(TABLE_NAME, null, contentValues);
     //  }catch(Exception e){//change this to catch an sql exception



      // }//end of try catch block


        db.close();

    }//end of addNewEntry

    //returns an entry already in the table (REF: http://hmkcode.com/android-simple-sqlite-database-tutorial/)
    public ParametersForDatabaseStorage getEntry(int id){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, COLUMNS, UNIQUE_ID + "=?",
                                    new String[] {String.valueOf(id)}, null, null, null, null);

        if(cursor != null){
            cursor.moveToFirst();
        }//end of if statement

        //create the new return object
        ParametersForDatabaseStorage pm = new ParametersForDatabaseStorage( Integer.parseInt(cursor.getString(0)),
                                                                            cursor.getString(1),  //filename
                                                                           Integer.parseInt(cursor.getString(2)), //grid points
                                                                            cursor.getString(3), //boundary conditions
                                                                            Double.parseDouble(cursor.getString(4)), //delta t factor
                                                                                cursor.getString(5));
        db.close();
        return pm;

    }//end of getEntry for id number

    public ParametersForDatabaseStorage getEntry(String filename){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, ROW_NAME + "=?",
                           new String[] {filename},null, null, null, null );

        if(cursor!=null){
            cursor.moveToFirst();
        }

        //create the new return object
        ParametersForDatabaseStorage pm = new ParametersForDatabaseStorage(
                Integer.parseInt(cursor.getString(0)), //id number
                cursor.getString(1),  //filename
                Integer.parseInt(cursor.getString(2)), //grid points
                cursor.getString(3), //boundary conditions
                Double.parseDouble(cursor.getString(4)), //delta t factor
                cursor.getString(5));
        db.close();
        return pm;


    }//end of get entry for string filename


    //gets all current saved data from the database
    public ArrayList<ParametersForDatabaseStorage> getAllSavedData(){

        ArrayList<ParametersForDatabaseStorage> pmList = new ArrayList<ParametersForDatabaseStorage>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        //add the rows to the array list
        if(cursor.moveToFirst()){

            do{

                int uniqueID = Integer.parseInt(cursor.getString(0));
                String filename = cursor.getString(1);
                int numberOfGridPoints = Integer.parseInt(cursor.getString(2));
                String boundaryConditions = cursor.getString(3);
                double deltaTFactor = Double.parseDouble(cursor.getString(4));
                String concentrationValues = cursor.getString(5);
                ParametersForDatabaseStorage pm = new ParametersForDatabaseStorage(uniqueID, filename, numberOfGridPoints, boundaryConditions, deltaTFactor, concentrationValues);
                pmList.add(pm);

            }while(cursor.moveToNext());

        }//end of if statement
        db.close();
        return pmList;

    }//end of get all method

    /* Returns an array list containing all of the file names */
    public ArrayList<String> getAllNames(){

        ArrayList<String> a = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " +UNIQUE_ID + ", " + ROW_NAME + " FROM " + TABLE_NAME +
                                  " ORDER BY " + UNIQUE_ID , null);
        if(cursor.moveToFirst()){

            do{
                String id = cursor.getColumnName(0);
                String filename = cursor.getString(1);
                a.add(filename);

            }while(cursor.moveToNext());

        }//end of if statement

        db.close();
        return a;

    }//end of getAllNames method

    public void deleteAll(){

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }//end of delete all method

}//end of class


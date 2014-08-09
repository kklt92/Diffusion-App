package com.example.diffusion.app.FreeFormBinaryDiffusion;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.example.diffusion.app.R;

public class FreeFormBinaryActivity extends FragmentActivity
    implements FreeFormBinarySketchingFragment.BinarySketchFragmentListener
             {

    private FreeFormBinarySketchingFragment sketchingFragment;
    private FreeFormBinaryModel diffusionModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_dimensional_diffusion);

        if(savedInstanceState==null){

            sketchingFragment = new FreeFormBinarySketchingFragment();
            LinearLayout fragmentContainer = (LinearLayout) findViewById(R.id.fragContainer);
            LinearLayout ll = new LinearLayout(this); ll.setId(12345);

            getSupportFragmentManager().beginTransaction().add(ll.getId(), sketchingFragment, "Sketching_Fragment_Tag").commit();
            fragmentContainer.addView(ll);

        }//end of if
        else{
            //handle a reload here - to be done this week
        }//end of else

    }//end of onCreate method


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.one_dimensional_diffusion, menu);
        return true;
    }//end of method

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }//end of method


    /*
     * Called from the sketching fragment when the animation button is clicked
     *
     * Moves to the Animation Fragment, and passes the to ar
     */
    public void binaryAnimateValues(float[] initialValues, float[] xValues,
                                    int sketchAreaHeight, int sketchAreaWidth,
                                    boolean linesOn){

        LinearLayout fragmentContainer = (LinearLayout) findViewById(R.id.fragContainer);
        LinearLayout ll = new LinearLayout(this); ll.setId(12345);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(ll.getId(), FreeFormBinaryAnimationFragment.newInstance(initialValues, xValues,
                sketchAreaHeight, sketchAreaWidth,
                linesOn), "ANIMATION_FRAGMENT_TAG");
        ft.addToBackStack(null);
        ft.commit();
        fragmentContainer.addView(ll);


    }//end of binaryAnimateValues method



    /* Called from sketching fragment, when loading a previous value */
    @Override
    public void loadBinaryAnimationFragment(String filename){

        //get the rest of the information from the database regarding the filename
        FreeFormValuesDataBaseHandler db = new FreeFormValuesDataBaseHandler(this);
        ParametersForDatabaseStorage pm = db.getEntry(filename);
        db.close();
        if(pm!=null){

            /* load animation fragment with the values from pm */
            float[] initialValues = pm.getConcentrationValues();


            int numberOfGridPoints = initialValues.length;
            int sketchViewWidth = ((int)(750/numberOfGridPoints))*numberOfGridPoints;
            int sketchViewHeight = sketchingFragment.getSketchViewHeight();

            //generate x values;
            float[] xValues = new float[numberOfGridPoints];
            float runningTotal = 0;
            float spaceBetweenPoints = sketchViewWidth/numberOfGridPoints;
            for(int i=0; i<xValues.length; i++){

                float value = (float)(((runningTotal*2) + spaceBetweenPoints)/2.0);
                xValues[i] = value;
                runningTotal += spaceBetweenPoints;
            }//end of for loop



            //open up the new activity
            LinearLayout ll = new LinearLayout(this); ll.setId(12345);
            LinearLayout fragmentContainer = (LinearLayout) findViewById(R.id.fragContainer);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(ll.getId(), FreeFormBinaryAnimationFragment.newInstance(initialValues, xValues,
                    sketchViewHeight, sketchViewWidth,
                    false));
            ft.addToBackStack(null);
            ft.commit();
            fragmentContainer.addView(ll);

        }//end of if statement

    }//end of loadBinaryAnimationFragment



}//end of Activity class



package com.example.diffusion.app.FreeFormTernaryDiffusion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

/**
 * Created by Sam on 07/08/2014.
 */
public class TernaryAnimationView extends View {

    /* Instance Variables */
    //drawing path
    private Path sketchingPath;
    //drawing and canvas paint
    private Paint sketchingPaint, linePaint, canvasPaint;
    //initial colour
    private int sketchingColour, lineColour;

    //canvas
    private Canvas sketchCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    //the size of the points on the grid
    private float pointSize, lineSize, snapshotSize;

    private float[] species1InitialValues, species2InitialValues;
    private float[] species1PlottingValues, species2PlottingValues;
    private float[] xValues;


    /* Constructor */
    public TernaryAnimationView(Context context, AttributeSet atts){
        super(context, atts);
        setUpView();
    }//end of constructor

    /* Helper method for the constructor */
    public void setUpView(){

        sketchingPath = new Path();//check if this is used
        sketchingPaint = new Paint();

        pointSize = 5;
        snapshotSize = 3;

        sketchingColour = Color.BLUE;
        sketchingPaint.setColor(sketchingColour);
        sketchingPaint.setAntiAlias(true);
        sketchingPaint.setStrokeWidth(pointSize);
        sketchingPaint.setStyle(Paint.Style.STROKE);
        sketchingPaint.setStrokeJoin(Paint.Join.ROUND);
        sketchingPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        //code that handles snapshots will go here

    }//end of setUpView method

    /* Getters/Setters */
    public void setSpecies1InitialValues(float[] array){ this.species1InitialValues = array; }
    public void setSpecies2InitialValues(float[] array){ this.species2InitialValues = array; }

    public void setSpecies1PlottingValues(float[] array){ this.species1PlottingValues = array; }
    public void setSpecies2PlottingValues(float[] array){ this.species2PlottingValues = array; }

    public void setXValues(float[] array){ this.xValues = array; }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        if(w>0 && h>0){
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            sketchCanvas = new Canvas(canvasBitmap);
        }
    }//end of onSizeChanged method

    @Override
    public void onDraw(Canvas canvas){

        canvasBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888); //see what effect this has
        sketchCanvas = new Canvas(canvasBitmap);
        //the above might not be needed so test deleting it once working (with all other features)

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(sketchingPath, sketchingPaint);

        //add in the stuff about lines tomorrow after points working
        for(int i=0; i<species1PlottingValues.length; i++){

            canvas.drawPoint( xValues[i],species1PlottingValues[i], sketchingPaint);
            sketchingPaint.setColor(Color.RED);
            canvas.drawPoint( xValues[i],species2PlottingValues[i], sketchingPaint);
            sketchingPaint.setColor(Color.BLUE);

        }//end of for loop

    }//end of onDraw method

    /* Called from aniamtion fragment when a redraw is required */
    public void updateView(){
        //reset the sketching canvas
        sketchCanvas.drawColor(Color.WHITE);

        invalidate();
    }//end of updateView method

    /* Called from animation fragment when the animation needs to be restarted */
    public void restartAnimation(){

        species1PlottingValues = Arrays.copyOf(species1InitialValues, species1InitialValues.length);
        species2PlottingValues = Arrays.copyOf(species2InitialValues, species2InitialValues.length);
        sketchCanvas.drawColor(Color.WHITE);
        invalidate();

    }//end of restartAnimation method

}//end of view class

package com.example.diffusion.app.FreeFormTernaryDiffusion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.diffusion.app.SketchingDataPoints;

/**
 * Help from: http://code.tutsplus.com/tutorials/android-sdk-drawing-with-opacity--mobile-19682
 *
 * Created by Sam on 30/07/2014.
 */
public class TernaryFreeFormSketchingView extends View {

    /* Instance Variables */
    private Path sketchingPath;
    private Paint sketchingPaint, linePaint, canvasPaint;
    private int sketchingColour, lineColour;
    private Canvas sketchingCanvas;
    private Bitmap canvasBitmap;

    private float pointSize, lineSize;

    private int numberOfGridPoints;
    private float spaceBetweenPoints;
    private SketchingDataPoints[] blueDataPointArray;
    private SketchingDataPoints[] redDataPointArray;
    private String currentlySketching;
    private int redAlphaValue, blueAlphaValue;
    private boolean redDataPointArraysInitialised, blueDataPointArraysInitialised;


    /* Default Constructor */
    public TernaryFreeFormSketchingView(Context context, AttributeSet atts){
        super(context, atts);
        setUpView();
    }//end of default constructor

    /* Helper method for Constructor - instantiates the instance variables */
    public void setUpView(){

        sketchingPath = new Path();
        sketchingPaint = new Paint();
        pointSize = 5;

        sketchingColour = Color.BLUE;
        sketchingPaint.setColor(sketchingColour);
        sketchingPaint.setAntiAlias(true);
        sketchingPaint.setStrokeWidth(pointSize);
        sketchingPaint.setStyle(Paint.Style.STROKE);
        sketchingPaint.setStrokeJoin(Paint.Join.ROUND);
        sketchingPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        lineColour = -7829368;
        linePaint = new Paint();
        linePaint.setColor(lineColour);
        lineSize = 2f;
        linePaint.setStrokeWidth(lineSize);
        linePaint.setAlpha(100);

        blueDataPointArraysInitialised = false;
        redDataPointArraysInitialised = false;
        currentlySketching = "blue";
        blueAlphaValue = 255; redAlphaValue = 100;

    }//end of set up view method


    /* Getters/Setters */
    public void setNumberOfGridPoints(int n){
        this.numberOfGridPoints = n;
    }

    public void setCurrentlySketching(String colour){
        this.currentlySketching = colour;
        if(currentlySketching.equals("Blue")){
            sketchingPaint.setColor(Color.BLUE);
            blueAlphaValue = 255;
            redAlphaValue = 100;
            invalidate();
        }
        else if(currentlySketching.equals("Red")){
            sketchingPaint.setColor(Color.RED);
            redAlphaValue = 255;
            blueAlphaValue = 100;
            invalidate();
        }
    }//end of setCurrentlySketching method

    public void setBlueDataPointArray(SketchingDataPoints[] array){
        this.blueDataPointArray = array;
    }

    public void setRedDataPointArray(SketchingDataPoints[] array){
        this.redDataPointArray = array;
    }

    public SketchingDataPoints[] getBlueDataPointArray(){ return this.blueDataPointArray; }
    public SketchingDataPoints[] getRedDataPointArray(){ return this.redDataPointArray; }

    /* Override methods for the View Class */

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){

        super.onSizeChanged(w, h, oldw, oldh);

        //initialize the canvas bitmap, and apply this bitmap to the canvas that will hold the drawing
        if(w>0 && h>0){
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            sketchingCanvas = new Canvas(canvasBitmap);
        }

    }//end of onSizeChanged method

    @Override
    public void onDraw(Canvas canvas){

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(sketchingPath, sketchingPaint);

        //for loop plots the grid lines onto the graph area
        for (int i = 0; i < canvas.getWidth(); i += (canvas.getWidth() / numberOfGridPoints)) { //change this to delta x at some point in the future
            canvas.drawLine(i, 0, i, canvas.getHeight(), linePaint); //changed that to see the effect it has
        }//end of for loop for drawing lines

        if(!blueDataPointArraysInitialised) { initialiseBlueDataPointArrays(); }
        if(!redDataPointArraysInitialised) { initialiseRedDataPointArray(); }

        printPoints(blueDataPointArray, Color.BLUE, blueAlphaValue, canvas);
        printPoints(redDataPointArray, Color.RED, redAlphaValue, canvas);

    }//end of onDraw method

    public void printPoints(SketchingDataPoints[] array, int colour, int alpha, Canvas canvas){
        sketchingPaint.setColor(colour);
        sketchingPaint.setAlpha(alpha);

        for(int i=0; i<blueDataPointArray.length; i++){
            canvas.drawPoint(array[i].getMidX(), array[i].getYPosition(), sketchingPaint);
        }
    }//end of printPoints method

    public void initialiseBlueDataPointArrays(){

        blueDataPointArray = new SketchingDataPoints[numberOfGridPoints];
        spaceBetweenPoints = sketchingCanvas.getWidth()/numberOfGridPoints;
        float runningTotal = 0;

        for(int i=0; i<blueDataPointArray.length; i++){
            blueDataPointArray[i] = new SketchingDataPoints(runningTotal, runningTotal+spaceBetweenPoints);
            blueDataPointArray[i].setYPosition(sketchingCanvas.getHeight()*0.25f);

            runningTotal += spaceBetweenPoints;
        }//end of for loop

        blueDataPointArraysInitialised = true;

    }//end of initialise blue data point array method

    public void initialiseRedDataPointArray(){

        redDataPointArray = new SketchingDataPoints[numberOfGridPoints];
        spaceBetweenPoints = sketchingCanvas.getWidth()/numberOfGridPoints;
        float runningTotal = 0;

        for(int i=0; i<redDataPointArray.length; i++){
            redDataPointArray[i] = new SketchingDataPoints(runningTotal, runningTotal+spaceBetweenPoints);
            redDataPointArray[i].setYPosition(sketchingCanvas.getHeight()*0.75f);

            runningTotal += spaceBetweenPoints;
        }//end of for loop

        redDataPointArraysInitialised = true;

    }//end of initialise red data point array method

    @Override
    public boolean onTouchEvent(MotionEvent event){

        final int action = event.getAction();
        float touchX = event.getX();
        float touchY = event.getY();
        float minLine = 0;
        float maxLine = sketchingCanvas.getHeight();

        switch(action){

            case MotionEvent.ACTION_DOWN:
                addToScreen(touchX, touchY);
           //     break;

            case MotionEvent.ACTION_MOVE:
                if(touchY >= minLine && touchY <=maxLine){


                    //get historical coordinates for fuller range
                    for (int j = 0; j < event.getHistorySize(); j++) {
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            float x = event.getHistoricalX(i, j);
                            float y = event.getHistoricalY(i, j);

                            addToScreen(x, y);
                        }
                    }//end of for loop for historical coordinates

                }//end of if
               // break;

            case MotionEvent.ACTION_UP:
//                sketchingCanvas.drawPath(sketchingPath, sketchingPaint);
//                sketchingPath.reset();
                addToScreen(touchX, touchY);
                break;

            default: return false;


        }//end of switch statement

    return true;

    }//end of onTouchEvent


    /* Helper method for the onTouch event - adds new input to screen */
    public void addToScreen(float x, float y){

        //find out which point of the screen the x and y positions belong to:
        SketchingDataPoints d;
        for(int i=0; i<blueDataPointArray.length; i++){

            if(currentlySketching.equals("Blue")) {
                d = blueDataPointArray[i];
            } else{
                d = redDataPointArray[i];
            }

            if(x>=d.getMinX() && x<=d.getMaxX()){
                //draw over the old point in white paint if it exists
                try{

                    sketchingPaint.setColor(Color.WHITE);
                    sketchingPaint.setStrokeWidth(7);
                    sketchingCanvas.drawPoint(d.getMidX(), d.getYPosition(), sketchingPaint);

                    sketchingPaint.setColor(sketchingColour);
                    sketchingPaint.setStrokeWidth(pointSize);

                }catch(NullPointerException npe) {}

                //paint in the new position,  and replace the new value in the array
                d.setYPosition(y);

            }//end of if statement

        }//end of for loop

        //call for the screen to be redrawn here
        invalidate();

    }//end of addToScreen method

    /* Reset methods that are called from the sketching fragment */
    public void resetRedDataPoints(){
        sketchingCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.redDataPointArraysInitialised = false;
        invalidate();
    }

    public void resetBlueDataPoints(){
        sketchingCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.blueDataPointArraysInitialised = false;
        invalidate();
    }

    public void resetBothArrays(){
        sketchingCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.blueDataPointArraysInitialised = false;
        this.redDataPointArraysInitialised = false;
        invalidate();
    }
}//end of class

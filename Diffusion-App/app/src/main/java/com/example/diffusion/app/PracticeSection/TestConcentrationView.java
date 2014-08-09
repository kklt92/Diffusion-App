package com.example.diffusion.app.PracticeSection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.diffusion.app.SketchingDataPoints;

/**
 * This is a test class, which will be used for creating an initial graph
 *   with axis on, that the user can draw to
 *
 * Created by Sam on 09/08/2014.
 */
public class TestConcentrationView extends View{

    /* Instance Variables*/
    private int numberOfGridPoints;
    //drawing path
    private Path sketchPath;
    //drawing and canvas paint, plus paint used for various sections of the view class
    private Paint sketchPaint, linePaint, canvasPaint, axisPaint;
    //initial colours for the sections of the view
    private int sketchingColour, lineColour, axisColour;

    //canvas
    private Canvas sketchCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    //the size of the points on the grid
    private float pointSize, lineSize, axisLineSize;

    private float spaceBetweenPoints;
    private SketchingDataPoints[] dataPointArray; //collected in this view and passed back to the fragment on complete
    private boolean dataPointArrayInitialised;
    private boolean linesOn;
    private int viewWidth;
    private int viewHeight;

    //define the area in which touch inputs can be registered
    private float minXPosition, maxXPosition, minYPosition, maxYPosition;
    private float sketchingWidth, sketchingHeight;



    /* Default Constructor */
    public TestConcentrationView(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpView();
    }//end of constructor

    /* Helper method for constructor */
    public void setUpView(){

        //initialise the instance variables
        sketchPath = new Path();//check if this is used
        sketchPaint = new Paint();

        pointSize = 5; lineSize=2; axisLineSize=2;

        //set the properties for the sketching (investigate the effects of these)
        sketchingColour = Color.BLACK;
        sketchPaint.setColor(sketchingColour);
        sketchPaint.setAntiAlias(true);
        sketchPaint.setStrokeWidth(pointSize);
        sketchPaint.setStyle(Paint.Style.STROKE);
        sketchPaint.setStrokeJoin(Paint.Join.ROUND);
        sketchPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG); //used for painting the grid

        //set the properties for the lines on the grid
        lineColour = -7829368;
        linePaint = new Paint();
        linePaint.setColor(lineColour);
        lineSize = 2f;
        linePaint.setStrokeWidth(lineSize);
        linePaint.setAlpha(100);


        //set the properties for adding in the axis
        axisColour = Color.BLACK;
        axisPaint = new Paint();
        axisPaint.setColor(axisColour);
        axisPaint.setAntiAlias(true);
        axisPaint.setStrokeWidth(axisLineSize);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeJoin(Paint.Join.ROUND);
        axisPaint.setStrokeCap(Paint.Cap.ROUND);

        dataPointArrayInitialised = false;

    }//end of setUpView method


    /* Getters/Setters */
    public void setNumberOfGridPoints(int n){

        this.numberOfGridPoints = n;

        //set up the arrays to hold the lines that will be drawn on the axis here

    }//end of setNumberOfGridPoints


    /* Rest of Methods */
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){

        super.onSizeChanged(w, h, oldw, oldh);

        //initialize the canvas bitmap, and apply this bitmap to the canvas that will hold the drawing
        if(w>0 && h>0){
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            sketchCanvas = new Canvas(canvasBitmap);
            this.viewHeight = this.getHeight();
            this.viewWidth = this.getLayoutParams().width;
        }

    }//end of onSizeChanged method

    @Override
    public void onDraw(Canvas canvas){

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(sketchPath, sketchPaint);

        //copy some of the code from the other onDraw (for setting up the arrays etc.)

        //draw a border onto the canvas
        canvas.drawRect(0f, 0f, viewWidth, viewHeight, axisPaint);

        //add the axis lines to the canvas
            //set the values for the y axis:
            float startYPosition = viewHeight*0.05f;
            float endYPosition = viewHeight*0.92f;
            float xValue = viewWidth*0.05f;
        canvas.drawLine(xValue, startYPosition, xValue, endYPosition, axisPaint);

            //set the xValues
            float startXPosition = viewWidth*0.05f;
            float endXPosition = viewWidth*0.95f;
            float yValue = viewHeight*0.92f;
        canvas.drawLine(startXPosition, yValue, endXPosition, yValue, axisPaint);

        //set the boundaries for the graph area that can be drawn to
        this.minXPosition = startXPosition;
        this.maxXPosition = endXPosition;
        this.minYPosition = startYPosition;
        this.maxYPosition = endYPosition;
        this.sketchingHeight = maxYPosition - minYPosition;
        this.sketchingWidth= maxXPosition - minYPosition;

        if(!dataPointArrayInitialised) initialiseDataPointArray();

        //for loop plots the grid lines onto the graph area
        for (float i = startXPosition; i < sketchingWidth+startXPosition; i += (sketchingWidth / numberOfGridPoints)) { //change this to delta x at some point in the future
            canvas.drawLine(i, startYPosition, i, endYPosition, linePaint);
        }//end of for loop for drawing lines


        //draw the data points to the screen
        for (int i = 0; i < dataPointArray.length; i++) {
            canvas.drawPoint(dataPointArray[i].getMidX(), dataPointArray[i].getYPosition(), sketchPaint);
        }//end of for loop for drawing points

        //add the text to the axis
        axisPaint.setStrokeWidth(0.65f);
        canvas.drawText("Grid Points", maxXPosition-50, maxYPosition+25, axisPaint);
        canvas.save(); //test deleting this to see if it makes any different
        canvas.rotate(270);
        canvas.drawText("Concentration", -120, 25, axisPaint);
//        canvas.drawPoint(-80,20, sketchPaint);
        canvas.restore();
        axisPaint.setStrokeWidth(axisLineSize);

        //add the little arrows to the end of the graph
        canvas.drawLine(minXPosition, minYPosition, minXPosition+10, minYPosition+10, axisPaint);
        canvas.drawLine(minXPosition, minYPosition, minXPosition-10, minYPosition+10, axisPaint);
        canvas.drawLine(maxXPosition, maxYPosition, maxXPosition-10, maxYPosition-10, axisPaint);
        canvas.drawLine(maxXPosition, maxYPosition, maxXPosition-10, maxYPosition+10, axisPaint);

    }//end of onDraw method

    /* Sets up the data point array */
    public void initialiseDataPointArray(){

        dataPointArray = new SketchingDataPoints[numberOfGridPoints];
        this.spaceBetweenPoints = (sketchingWidth)/numberOfGridPoints;

        float runningTotal = minXPosition;

        //populate the array with data points
        for (int i = 0; i < dataPointArray.length; i++) {

            dataPointArray[i] = new SketchingDataPoints(runningTotal, runningTotal + spaceBetweenPoints);
            dataPointArray[i].setYPosition((maxYPosition-minYPosition)/2.0f);

            runningTotal += spaceBetweenPoints;
        }//end of for loop

        dataPointArrayInitialised = true;

    }//end of initialiseDataPointArray

    @Override
    public boolean onTouchEvent(MotionEvent event){

        final int action = event.getAction();

        float touchX = event.getX();
        float touchY = event.getY();

        switch (action){

            case MotionEvent.ACTION_DOWN: //finger touching the screen
                sketchPath.moveTo(touchX, touchY);

            case MotionEvent.ACTION_MOVE: //finger moving across the screen


                    addToScreen(touchX, touchY);

                    //use historical coordinates to fill in gaps
                    for (int j = 0; j < event.getHistorySize(); j++) {
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            float x = event.getHistoricalX(i, j);
                            float y = event.getHistoricalY(i, j);

                            addToScreen(x, y);
                        }
                    }//end of for loop for historical coordinates
                    break;


            case MotionEvent.ACTION_UP: //finger being lifted from the screen

                sketchCanvas.drawPath(sketchPath, sketchPaint);
                sketchPath.reset();
                break;

            default:

                return false; //return false for an unrecognised action

        }//end of switch statement

        return true;

    }//end of onTouchEvent method

    /* Helper method for receiving touch input and drawing them to the screen */
    public void addToScreen(float x, float y){

        //find out which point of the screen the x and y positions belong to:
        SketchingDataPoints d;
        for(int i=0; i<dataPointArray.length; i++){
            d = dataPointArray[i];

            if(x>=d.getMinX() && x<=d.getMaxX()){
                //draw over the old point in white paint if it exists
                try{

                    sketchPaint.setColor(Color.WHITE);
                    sketchPaint.setStrokeWidth(7);
                    sketchCanvas.drawPoint(d.getMidX(), d.getYPosition(), sketchPaint);

                    sketchPaint.setColor(sketchingColour);
                    sketchPaint.setStrokeWidth(pointSize);

                }catch(NullPointerException npe) {}

                //add the current y value to the correct data point
                if(y>=minYPosition && y<=maxYPosition) {
                    d.setYPosition(y);
                } else if(y<minYPosition){
                    d.setYPosition(minYPosition);
                } else if(y>maxYPosition){
                    d.setYPosition(maxYPosition);
                }

            }//end of outer if statement

        }//end of for loop
        invalidate();

    }//end of addToScreen method

}//end of class

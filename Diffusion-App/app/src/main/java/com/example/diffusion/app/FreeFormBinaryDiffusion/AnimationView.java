package com.example.diffusion.app.FreeFormBinaryDiffusion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Sam on 15/07/2014.
 */
public class AnimationView extends View {

    /* Instance Variables */
    //drawing path
    private Path sketchPath;
    //drawing and canvas paint
    private Paint sketchPaint, linePaint, canvasPaint;
    //initial colour
    private int sketchingColour, lineColour;

    //canvas
    private Canvas sketchCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    //the size of the points on the grid
    private float pointSize, lineSize, snapshotSize;

    private float[] initialValues, plottingValues, xValues;


    private ArrayList<SnapShotValues> snapshotValues;
    private int[] colours;
    private int positionInColors, numberOfSnapShots;
    private boolean snapShotTaken;
    private boolean linesOn;

    /* Default Constructor */
    public AnimationView(Context context, AttributeSet atts){
        super(context, atts);
        setUpView();
    }//end of constructor

    /* Helper method for the constructor */
    public void setUpView(){

        sketchPath = new Path();//check if this is used
        sketchPaint = new Paint();

        pointSize = 5;
        snapshotSize = 3;

        //set the properties for the sketching (investigate the effects of these)
        sketchingColour = Color.BLACK;
        sketchPaint.setColor(sketchingColour);
        sketchPaint.setAntiAlias(true);
        sketchPaint.setStrokeWidth(pointSize);
        sketchPaint.setStyle(Paint.Style.STROKE);
        sketchPaint.setStrokeJoin(Paint.Join.ROUND);
        sketchPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG); //used for painting the grid

        //set the properties for the grid
        lineColour = -7829368;
        linePaint = new Paint();
        linePaint.setColor(lineColour);
        lineSize = 2f;
        linePaint.setStrokeWidth(lineSize);
        linePaint.setAlpha(100);

        snapshotValues = new ArrayList<SnapShotValues>();
        colours = new int[11];
        colours[0] = Color.parseColor("#FF0000");
        colours[1] = Color.parseColor("#FF6600");
        colours[2] = Color.parseColor("#FF9900");
        colours[3] = Color.parseColor("#FFCC00");
        colours[4] = Color.parseColor("#669900");
        colours[5] = Color.parseColor("#66FF33");
        colours[6] = Color.parseColor("#33CCFF");
        colours[7] = Color.parseColor("#0066FF");
        colours[8] = Color.parseColor("#9933FF");
        colours[9] = Color.parseColor("#CC00CC");
        colours[10] = Color.parseColor("#FF3399");

        positionInColors = 0;
        numberOfSnapShots = 0;
        snapShotTaken = false;

    }//end of helper method

    /* +++++++++ Getters/Setters ++++++++++++++++++ */
    public void setPlottingValues(float[] values) { this.plottingValues = values; }

    public void setXValues(float[] values){
        this.xValues = values;
    }

    public void setInitialValues(float[] values) { this.initialValues = values.clone(); }

    public float[] getInitialValues(){ return this.initialValues; }

    public void setSnapShotTaken(boolean b) { this.snapShotTaken = b; }

    public void setLinesOn(boolean b) {
        this.linesOn = b;
        invalidate();
    }

    public SnapShotValues getLatestSnapShot(){
        return this.snapshotValues.get(snapshotValues.size()-1);
    }

    public Bitmap getCanvasBitmap(){  //it might be that the bitmap is blank - need to
        return canvasBitmap;
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++ */

    /* Override Methods for the View Class */
     /* Code for handling setting up drawing surface */
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){ //find out when this method gets called when a view gets created
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);

        //initialize the canvas bitmap, and apply this bitmap to the canvas that will hold the drawing
        if(w>0 && h>0){
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            sketchCanvas = new Canvas(canvasBitmap);
        }


    }//end of onSizeChanged method

    @Override
    public void onDraw(Canvas canvas){
        canvasBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888); //see what effect this has
        sketchCanvas = new Canvas(canvasBitmap);

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(sketchPath, sketchPaint);

        //for loop plots the grid lines onto the graph area
//        for (int i = 0; i < canvas.getWidth(); i += (canvas.getWidth() / plottingValues.length)) { //change this to delta x at some point in the future
//            canvas.drawLine(i, 0, i, canvas.getHeight(), linePaint); //changed that to see the effect it has
//        }//end of for  loop

      if(!linesOn){
          //test drawing the points
          for(int i=0; i<plottingValues.length; i++){
              canvas.drawPoint(xValues[i], plottingValues[i], sketchPaint);
          }

          if(snapShotTaken){

              for(int i=0; i<snapshotValues.size(); i++){

                  SnapShotValues ssv = snapshotValues.get(i);
                  sketchPaint.setColor(ssv.getColour());
                  sketchPaint.setStrokeWidth(snapshotSize);

                  for(int j=0; j<ssv.size(); j++){
                      canvas.drawPoint(xValues[j], ssv.getSnapshotValues()[j], sketchPaint);
                  }//end of inner for loop

              }//end of for loop

              sketchPaint.setColor(sketchingColour);
              sketchPaint.setStrokeWidth(pointSize);

          }//end of if snapshot taken

          sketchPaint.setColor(sketchingColour);
      }
        else if(linesOn){

          for(int i=1; i<plottingValues.length; i++){
              canvas.drawLine(
                      xValues[i-1], plottingValues[i-1],
                      xValues[i], plottingValues[i], sketchPaint
              );
          }//end of for loop

          if(snapShotTaken){

              for(int i=0; i<snapshotValues.size(); i++){
                  SnapShotValues ssv = snapshotValues.get(i);
                  sketchPaint.setColor(ssv.getColour());
                  sketchPaint.setStrokeWidth(snapshotSize);

                  for(int j=1; j<ssv.size(); j++){
                      canvas.drawLine(
                              xValues[j-1], ssv.getSnapshotValues()[j-1],
                              xValues[j], ssv.getSnapshotValues()[j], sketchPaint
                      );
                  }
              }

              sketchPaint.setColor(sketchingColour);
              sketchPaint.setStrokeWidth(pointSize);
          }//end of if snapshot taken


      }//end of if/else block


    }//end of onDraw method

    /* Test method for updating the screen with new values */
    public void updateView(){

        //reset the sketching canvas
        sketchCanvas.drawColor(Color.WHITE);

        invalidate();

    }//end of updateView method

    /* Called when a snapshot is taken */
    public void createSnapShot(){

        if(!snapShotTaken) {
            snapShotTaken = true;
        }

        int color = colours[positionInColors];
        if(positionInColors==colours.length-1) positionInColors = 0;
        else positionInColors++;

        snapshotValues.add(new SnapShotValues(plottingValues.clone(), color));
        numberOfSnapShots++;
        invalidate();

    }//end of snapShotTaken method

    /* Called when the animation needs to reset */
    public void restartAnimation(float[] values){
        setPlottingValues(values);
        sketchCanvas.drawColor(Color.WHITE);
        snapshotValues.clear();
        snapShotTaken = false;
        positionInColors = 0;
        invalidate();
    }//end of restart animation method

}//end of class

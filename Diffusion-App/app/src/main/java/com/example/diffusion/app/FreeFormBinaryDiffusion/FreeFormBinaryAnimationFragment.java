package com.example.diffusion.app.FreeFormBinaryDiffusion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.diffusion.app.AnimatedGifEncoder;
import com.example.diffusion.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import java.util.Arrays;

/**
 * Class which handles the animations of the diffusion activity
 *
 * Created by Sam on 15/07/2014.
 */
public class FreeFormBinaryAnimationFragment extends Fragment implements View.OnClickListener{

    /* Instance Variables */
    private AnimationView mAnimationView;
    private Button  mSnapShotButton;
    private ImageButton mPlayButton, mPauseButton;
    private ToggleButton mLinesToggleButton;
    private LinearLayout buttonLayout;
    private SeekBar mTemperatureSeekBar;


    private FreeFormBinaryModel mDiffusionModel;
    private float[] initialValues, xValues, plottingValues;
    private int animationViewHeight, animationViewWidth;

    private boolean animationPlaying;
    private int playing; //set to 1 for playing, and 0 for paused
    private boolean linesOn;

    private PlayThread pt;
    //TODO add in the gif thread here as well

    private String boundaryConditions;
    private ArrayList<String> boundaryConditionsArray;
    private int positionInBCArray;
    private double deltaTFactor;

    private boolean creatingGif;

    /* Getters/Setters */
    public int getPlaying(){return this.playing;}

    public void setPlottingValues(float[] v) { this.plottingValues = v; }

    private RadioGroup mBoundaryGroup;


    /* Static method for creating a new instance of the class */
    public static FreeFormBinaryAnimationFragment newInstance(float[] initialValues, float[] xValues,
                                                              int animationViewHeight, int animationViewWidth,
                                                              boolean linesOn){

        FreeFormBinaryAnimationFragment fragment = new FreeFormBinaryAnimationFragment();

        //attach the arguments to return fragment
        Bundle args = new Bundle();
        args.putFloatArray("initial_values", initialValues);
        args.putFloatArray("x_values", xValues);
        args.putInt("animation_view_height", animationViewHeight);
        args.putInt("animation_view_width", animationViewWidth);
        args.putBoolean("animation_lines_on", linesOn);

        fragment.setArguments(args);

        return fragment;

    }//end of newInstance method

    /* Blank constructor needed for the newInstance method */
    public FreeFormBinaryAnimationFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getArguments()!=null) {

            //associated the arguments with the instance variables
            this.initialValues = getArguments().getFloatArray("initial_values");
            setPlottingValues(Arrays.copyOf(initialValues, initialValues.length));

            this.xValues = getArguments().getFloatArray("x_values");
            this.animationViewHeight = getArguments().getInt("animation_view_height");
            this.animationViewWidth = getArguments().getInt("animation_view_width");
            this.linesOn = getArguments().getBoolean("animation_lines_on");


            this.animationPlaying = false;
            this.boundaryConditions = "Constant Value";
            this.deltaTFactor = 0.8;
            this.boundaryConditionsArray = new ArrayList<String>();
            this.boundaryConditionsArray.add("Constant Value"); this.boundaryConditionsArray.add("Zero Flux");
            this.boundaryConditionsArray.add("Periodic"); this.positionInBCArray = 0;
            this.creatingGif = false;
            //test code below for moving functionality to the action bar
            setHasOptionsMenu(true);

        }//end of if statement

        else{
            //use for reloading in here
        }

    }//end of onCreate method

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){

        menuInflater.inflate(R.menu.binary_animation_menu, menu);

    }//end of onCreateOptionsMenu method

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        switch(menuItem.getItemId()){

            case R.id.action_create_gif:
                if(!creatingGif){
                    createGif();
                    return true;
                }else{
                    finishGif();
                    return true;
                }

            case R.id.action_change_deltaT:
                changeParameters();
                return true;


            case R.id.action_restart_animation:
                restartAnimation();
                return true;


            case R.id.action_save_data:
                saveCurrentData();
                return true;


            case R.id.action_snapshot:
                saveCurrentAnimationView();
                return true;

            case R.id.action_switch_to_lines:
                if(linesOn) linesOn = false;
                else linesOn = true;
                mAnimationView.setLinesOn(linesOn);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }//end of switch statement

    }//end of method


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_one_dimensional_animation, container, false);

        //set up the AnimationView
        mAnimationView = (AnimationView) v.findViewById(R.id.animation_view);
        mAnimationView.setPlottingValues(this.plottingValues);
        mAnimationView.setInitialValues(this.initialValues);
        mAnimationView.getLayoutParams().height = this.animationViewHeight;
        mAnimationView.getLayoutParams().width = this.animationViewWidth;
        mAnimationView.setXValues(this.xValues);
        mAnimationView.setLinesOn(this.linesOn);

        buttonLayout = (LinearLayout) v.findViewById(R.id.animation_play_button_layout);

        mPlayButton = new ImageButton(this.getActivity());
        mPlayButton.setImageResource(R.drawable.play_button);
        mPlayButton.setId(12345);
        mPlayButton.setOnClickListener(this);

        buttonLayout.addView(mPlayButton);

        mPauseButton = (ImageButton) new ImageButton(this.getActivity());
        mPauseButton.setImageResource(R.drawable.pause_button);
        mPauseButton.setId(54321);
        mPauseButton.setOnClickListener(this);

        buttonLayout.addView(mPauseButton);
        mPauseButton.setVisibility(View.GONE);

        mSnapShotButton = new Button(this.getActivity());
        mSnapShotButton.setText("Snapshot");
        mSnapShotButton.setOnClickListener(this);
        mSnapShotButton.setId(89);
        buttonLayout.addView(mSnapShotButton);

        mDiffusionModel = new FreeFormBinaryModel(this.plottingValues, this.animationViewHeight, boundaryConditions, deltaTFactor);

        mTemperatureSeekBar = (SeekBar) v.findViewById(R.id.animation_temperature_seek_bar);
        mTemperatureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                mDiffusionModel.setDiffusionCoefficient(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        mTemperatureSeekBar.setRotation(270);


        mBoundaryGroup = (RadioGroup) v.findViewById(R.id.boundary_condition_radio_group);
        mBoundaryGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radio_boundary_constant:
                        mDiffusionModel.setBoundaryConditions("Constant Value");
                        restartAnimation();
                        break;

                    case R.id.radio_boundary_periodic:
                        mDiffusionModel.setBoundaryConditions("Periodic");
                        restartAnimation();
                        break;

                    case R.id.radio_boundary_zero:
                        mDiffusionModel.setBoundaryConditions("Zero Flux");
                        restartAnimation();
                        break;
                }
            }
        });

        return v;

    }//end of onCreateView method

    @Override
    public void onClick(View v){

        switch(v.getId()){

            case 12345: //play button
                playAnimation();
                break;

            case 54321: //pause button
                pauseAnimation();
                break;

            case 89: //snapshot button (adds lines to the screen)
                mAnimationView.createSnapShot();
                break;

        }//end of switch statement

    }//end of onClick method

    /*
     * Inner Thread class, used for running the animations
     */
    class PlayThread extends Thread { //TODO used for quick navigation

        public Handler handler = new Handler();

        public void run() {

            if(animationPlaying) {

                switch (getPlaying()) {
                    case 0: //0 is for paused
                        handler.postDelayed(this, 1);
                        break;
                    case 1: //1 is for playing
                        mDiffusionModel.solutionOneStep();
                        plottingValues = mDiffusionModel.getPlottingValues();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAnimationView.setPlottingValues(plottingValues);
                                mAnimationView.updateView();

                            }//end of inner run method
                        });
                        handler.postDelayed(this, 1);
                        break;

                }//end of switch
            }//end of if statement

        }//end of run method

    }//end of inner PlayThread class

    /* Code which calls the runnable and plays the animation */
    public void playAnimation(){

        if(!animationPlaying){
            pt = new PlayThread();
            mPlayButton.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.VISIBLE);
            this.playing = 1;
            this.animationPlaying = true;
            pt.start();
        }

    }//end of playAnimation method

    /* Called when the pause button is pressed */
    public void pauseAnimation(){

        if(playing==0){
            this.playing = 1;
            mPauseButton.setImageResource(R.drawable.pause_button);
        }
        else {
            this.playing = 0;
            mPauseButton.setImageResource(R.drawable.play_button);
        }

    }//end of pauseAnimation method

    /* Called when the restart animation is pressed */
    public void restartAnimation(){

        if(animationPlaying) {
            this.animationPlaying = false;
            mTemperatureSeekBar.setProgress(50);
            pt = null;
            mAnimationView.restartAnimation(Arrays.copyOf(initialValues, initialValues.length));
            mDiffusionModel.restartAnimation();
            mPauseButton.setImageResource(R.drawable.pause_button); playing = 1;
            mPauseButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.VISIBLE);
        }//end of if

    }//end of restart animation method

    /* Called when the snap shot button is pressed */
    public void addSnapShotToTextView(){

        int time =  mDiffusionModel.getCurrentTimeStep();
        SnapShotValues ssv = mAnimationView.getLatestSnapShot();
        int colour = ssv.getColour();


    }//end of add snap shot to text view method

    /* Called when the save data button is pressed */
    public void saveCurrentData(){

        //open up a dialogue to ask for the filename
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Save");
        builder.setMessage("Enter File Name:");
        final EditText et = new EditText(this.getActivity());
        builder.setView(et);

        // attempt at solution, from http://stackoverflow.com/questions/9053685/android-sqlite-saving-string-arr
        //   and http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);
        final AlertDialog dialogue = builder.create();

        //open up a connection to the database
        final FreeFormValuesDataBaseHandler db = new FreeFormValuesDataBaseHandler(this.getActivity());

        dialogue.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {


                //get the positive and negative buttons, and override the onClick listeners for them

                Button saveButton = dialogue.getButton(AlertDialog.BUTTON_POSITIVE);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String filename = et.getText().toString();
                        int numberOfGridPoints = mDiffusionModel.getNumberOfGridPoints();
                        String boundaryConditions = mDiffusionModel.getBoundaryConditions(); //will also get this from the model in the future
                        double deltaTFactor = mDiffusionModel.getDeltaTFactor();

                        ParametersForDatabaseStorage pm = new ParametersForDatabaseStorage(filename, numberOfGridPoints, boundaryConditions, deltaTFactor, initialValues);
                        try{
                            db.addNewEntry(pm);

                            String toastText1 = filename + " saved";
                            Toast successToast = Toast.makeText(getActivity().getBaseContext(), toastText1, Toast.LENGTH_LONG);
                            successToast.setGravity(Gravity.TOP, 0, 250);
                            successToast.show();

                            db.close();
                            dialogue.dismiss();
                        }catch (Exception e){
                            //make a toast saying trying a new name

                            String toastText2 = "Error: " + filename + " already in use.\nPlease enter different name";

                            Toast errorToast = Toast.makeText(getActivity().getBaseContext(), toastText2, Toast.LENGTH_LONG);
                            errorToast.setGravity(Gravity.TOP, 0, 250);
                            errorToast.show();

                        }//end of try catch block

                    }//end of onClick method for the positive button
                });

                Button cancelButton = dialogue.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.close();
                        dialogue.dismiss();
                    }
                });

            }//end of onShow method
        });


        dialogue.show();


    }//end of save current data method

    /* Opens a dialog for the parameters to be changed in
     *  Code from:
     *     attempt at solution, from http://stackoverflow.com/questions/9053685/android-sqlite-saving-string-arr
     *     and http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
     */
    public void changeParameters(){

        AlertDialog.Builder dialogue = new AlertDialog.Builder(this.getActivity());
        dialogue.setTitle("Parameters");
        dialogue.setMessage("Change Parameters:");

        LinearLayout dialogueLayout = new LinearLayout(this.getActivity());
        dialogueLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout bcLL = new LinearLayout(this.getActivity());

        TextView bcTV = new TextView(this.getActivity()); bcTV.setText("Boundary Condition: ");

        Spinner boundaryConditionSpinner = new Spinner(this.getActivity()); //check if this is the correct was to create a spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, boundaryConditionsArray);
        boundaryConditionSpinner.setAdapter(adapter);
        boundaryConditionSpinner.setSelection(positionInBCArray);
        boundaryConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                positionInBCArray = i;
                boundaryConditions = boundaryConditionsArray.get(positionInBCArray);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        bcLL.addView(bcTV); bcLL.addView(boundaryConditionSpinner);

        LinearLayout deltaTLL = new LinearLayout(this.getActivity());
        deltaTLL.setOrientation(LinearLayout.HORIZONTAL);

        TextView deltaTTextView = new TextView(this.getActivity());
        deltaTTextView.setText("Delta T factor: ");
        final EditText deltaTEditText = new EditText(this.getActivity());
        deltaTEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        deltaTEditText.setText("" + deltaTFactor);

        deltaTLL.addView(deltaTTextView); deltaTLL.addView(deltaTEditText);

        dialogue.setPositiveButton("Commit Changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                deltaTFactor = Double.parseDouble(deltaTEditText.getText().toString());

                //update the values in the model
                mDiffusionModel.setDeltaTFactor(deltaTFactor);
                mDiffusionModel.setBoundaryConditions(boundaryConditions);
                restartAnimation();
                if(deltaTFactor>0.8){
                    Toast warningToast = Toast.makeText(getActivity().getBaseContext(), "Warning: May be unstable!", Toast.LENGTH_LONG);
                    warningToast.setGravity(Gravity.TOP, 0, 250);
                    warningToast.show();
                }
            }
        });

        dialogue.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dialogue.dismiss();
            }
        });

        dialogueLayout.addView(bcLL); dialogueLayout.addView(deltaTLL);
        dialogue.setView(dialogueLayout);
        dialogue.show();

    }//end of changeParameters method

    @Override
    public void onDetach(){
        super.onDetach();
        System.out.println("ON DETACH CALLED");
        //when detaching stop the animation from playing - may have to store the current place in
        // bundle state in the future
        this.playing = 0;
    }//end of onDetach method

    private GifThread gifThread;
    private String filename;

    /*
     * Creates a Gfycat gif link
     *    Makes use of the AnimatedGifEncoder class (see class for reference), and an answer from:
     *     http://stackoverflow.com/questions/16331437/how-to-create-an-animated-gif-from-jpegs-in-android-development
     *     which details how to create the gif file
     *    Code which creates the gif on a thread written by myself
     */
    public void createGif(){

        System.out.println("starting gif");

        creatingGif = true;

        final AlertDialog.Builder nameDialogue= new AlertDialog.Builder(this.getActivity());
        nameDialogue.setTitle("Create Gif");
        nameDialogue.setMessage("Enter Gif Name:");
        final EditText et = new EditText(this.getActivity());
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        nameDialogue.setView(et);
        nameDialogue.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filename = et.getText().toString();
                if(filename.length()>3 && filename.substring(filename.length()-3).equals(".gif")){
                    //what needs to happen here
                }else filename = filename + ".gif";

                gifThread = new GifThread();
                gifThread.start();

            }//end of onClick method
        });

        nameDialogue.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {



            }
        });

        nameDialogue.show();

    }//end of createGfycat link method

    /*
     * Creates a gif and stores it to the device, and offers the option of creating a gfycat link
     *  help from: http://stackoverflow.com/questions/2801116/converting-a-view-to-bitmap-without-displaying-it-in-android
     */
    public void finishGif(){

        System.out.println("finish gif");

        creatingGif = false;

        final byte[] byteArray = gifThread.returnByteArray(); //the problem might be here, so i can try moving this around if i need to
        gifThread = null; //ready for garbage collection
        System.out.println("Got the byte array, size: " + byteArray.length);


        FileOutputStream outputStream = null;
        try{ //the below will need modifying but stick with it for now
            File f = Environment.getExternalStorageDirectory();
            File directory = new File(f.getAbsolutePath() + "/gifdir");
            directory.mkdirs();
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(byteArray);
            outputStream.close();
            System.out.println("the try block has been finished");
        }catch (Exception e){
            System.out.println("exception: " + e);
        }//end of try/catch block

        /* Code for creating gfycat link below - come back to in the future */
//        AlertDialog.Builder gfycatBuilder = new AlertDialog.Builder(this.getActivity());
//        gfycatBuilder.setTitle("Gif Created");
//        gfycatBuilder.setMessage("Do you want to generate a Gfycat link?");
//        gfycatBuilder.setPositiveButton("Generate", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//             GenerateGfycatLinkThread gThread = new GenerateGfycatLinkThread(byteArray);
//             gThread.start();
//
//            }//end of onClick method
//        });
//
//        gfycatBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                //option to open the gif in here
//            }
//        });
//
//        gfycatBuilder.show();
    }//end of finishCreatingGfycatLink method

//    class GenerateGfycatLinkThread extends Thread{
//
//        private byte[] byteArray;
//        public GenerateGfycatLinkThread(byte[] array){
//            this.byteArray = array;
//        }
//
//        @Override
//        public void run(){
//
//            HttpClient client = new DefaultHttpClient();
//            HttpPost post = new HttpPost("https://gifaffe.s3.amazonaws.com/");
//            ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
//
//            list.add(new BasicNameValuePair("key", "SamFarmerTest"));
//            list.add(new BasicNameValuePair("file", byteArray.toString())); //this probably wont work, if it doesnt, dont worry about it
//            list.add(new BasicNameValuePair("acl", "private"));
//            list.add(new BasicNameValuePair("AWSAccessKeyId", "AKIAIT4VU4B7G2LQYKZQ"));
//            list.add(new BasicNameValuePair("policy", "eyAiZXhwaXJhdGlvbiI6ICIyMDIwLTEyLTAxVDEyOjAwOjAwLjAwMFoiLAogICAgICAgICAgICAiY29uZGl0aW9ucyI6IFsKICAgICAgICAgICAgeyJidWNrZXQiOiAiZ2lmYWZmZSJ9LAogICAgICAgICAgICBbInN0YXJ0cy13aXRoIiwgIiRrZXkiLCAiIl0sCiAgICAgICAgICAgIHsiYWNsIjogInByaXZhdGUifSwKCSAgICB7InN1Y2Nlc3NfYWN0aW9uX3N0YXR1cyI6ICIyMDAifSwKICAgICAgICAgICAgWyJzdGFydHMtd2l0aCIsICIkQ29udGVudC1UeXBlIiwgIiJdLAogICAgICAgICAgICBbImNvbnRlbnQtbGVuZ3RoLXJhbmdlIiwgMCwgNTI0Mjg4MDAwXQogICAgICAgICAgICBdCiAgICAgICAgICB9"));
//            list.add(new BasicNameValuePair("success_action_status","200"));
//            list.add(new BasicNameValuePair("signature", "mk9t/U/wRN4/uU01mXfeTe2Kcoc="));
//            list.add(new BasicNameValuePair("Content-Type", "image/gif"));
//
//            try{
//                post.setEntity(new UrlEncodedFormEntity(list));
//            }catch(UnsupportedEncodingException e){
//                System.out.println("Exception: " + e);
//            }
//            try {
//                HttpResponse response = client.execute(post);
//                // write response to log
//                Log.d("Http Post Response:", response.toString()); //delete this after testing it
//            } catch (ClientProtocolException e) {
//                // Log exception
//                e.printStackTrace();
//            } catch (IOException e) {
//                // Log exception
//                e.printStackTrace();
//            }
//
//        }//end of run method
//
//    }//end of gfycat thread

    /*
     * Inner class - test thread used for creating the gif, and storing it on the device
     */
    class GifThread extends Thread {

        Handler handler = new Handler();
        AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean gifEncoderStarted = false;

        public void run() {

            if(creatingGif) {//rename this file
                if(!gifEncoderStarted){
                    animatedGifEncoder.start(byteArrayOutputStream);
                    gifEncoderStarted = true;
                }//end of if statement
                mDiffusionModel.solutionOneStep();
                plottingValues = mDiffusionModel.getPlottingValues();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAnimationView.setPlottingValues(plottingValues);
                        mAnimationView.updateView();
                        Bitmap b = Bitmap.createBitmap(mAnimationView.getWidth(), mAnimationView.getHeight(), Bitmap.Config.ARGB_8888); //might have to make a copy of this, not sure
                        Canvas c = new Canvas(b);
                        mAnimationView.layout(mAnimationView.getLeft(), mAnimationView.getTop(), mAnimationView.getRight(), mAnimationView.getBottom());
                        mAnimationView.draw(c);
                        animatedGifEncoder.addFrame(b);

                    }//end of runOnUi run
                });
                handler.postDelayed(this, 1);
            }
        }//end of run method

    public byte[] returnByteArray(){
        animatedGifEncoder.finish();
        return byteArrayOutputStream.toByteArray();
    }

    }//end of GifThread class

    public void saveCurrentAnimationView(){

        final AlertDialog.Builder nameDialogue= new AlertDialog.Builder(this.getActivity());
        nameDialogue.setTitle("Save Snapshot");
        nameDialogue.setMessage("Enter Image Name:");
        final EditText et = new EditText(this.getActivity());
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        nameDialogue.setView(et);
        nameDialogue.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filename = et.getText().toString();
                filename = filename + ".png";
                Bitmap b = Bitmap.createBitmap(mAnimationView.getWidth(), mAnimationView.getHeight(), Bitmap.Config.ARGB_8888); //might have to make a copy of this, not sure
                Canvas c = new Canvas(b);
                mAnimationView.layout(mAnimationView.getLeft(), mAnimationView.getTop(), mAnimationView.getRight(), mAnimationView.getBottom());
                mAnimationView.draw(c);

                FileOutputStream outputStream = null;
                try{ //the below will need modifying but stick with it for now
                    File f = Environment.getExternalStorageDirectory();
                    File directory = new File(f.getAbsolutePath() + "/picdir");
                    directory.mkdirs();
                    File file = new File(directory, filename);
                    outputStream = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    System.out.println("the try block has been finished");
                }catch (Exception e){
                    System.out.println("exception: " + e);
                }//end of try/catch block

                //get a copy of the image from the view


            }//end of onClick method
        });

        nameDialogue.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        nameDialogue.show();


    }//end of saveCurrentAnimationView


}//end of animation fragment class

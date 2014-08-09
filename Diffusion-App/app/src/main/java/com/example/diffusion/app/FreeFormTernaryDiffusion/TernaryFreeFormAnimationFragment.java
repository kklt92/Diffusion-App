package com.example.diffusion.app.FreeFormTernaryDiffusion;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.diffusion.app.R;

import java.util.Arrays;
import android.os.Handler;

/*
 * Fragment which handles the animations for the ternary free form section of the app
 *
 */
public class TernaryFreeFormAnimationFragment extends Fragment implements View.OnClickListener {

    /* Instance Variables */
    private TernaryAnimationView mTernaryAnimationView;
    private TernaryFreeFormModel mTernaryFreeFormModel;

    //variables for the buttons on the view
    private Button mPlayButton, mRestartButton;

    //variables that get passed on creation
    private float[] species1InitialValues, species2InitialValues, xValues;
    private float[] species1PlottingValues, species2PlottingValues;
    private int animationViewHeight, animationViewWidth;

    //other variables
    private String boundaryConditions;
    //TODO add in the variables that will be used controlling parameters in the model

    private PlayThread pt;
    private boolean animationPlaying;
    private int playingOrPaused; //==0 for paused, ==1 for playing

    /*
     * Static method used for creating a new instance of the app
     */
    public static TernaryFreeFormAnimationFragment newInstance(float[] species1InitialValues, float[] species2InitialValues,
                                                               float[] xValues, int animationViewHeight, int animationViewWidth) {
        TernaryFreeFormAnimationFragment fragment = new TernaryFreeFormAnimationFragment();
        Bundle args = new Bundle();

        args.putFloatArray("species_1_values", species1InitialValues);
        args.putFloatArray("species_2_values", species2InitialValues);
        args.putFloatArray("x_values", xValues);
        args.putInt("animation_view_height", animationViewHeight);
        args.putInt("animation_view_width", animationViewWidth);

        //add in any other things here

        fragment.setArguments(args);
        return fragment;
    }
    public TernaryFreeFormAnimationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            //get the parameters from the passed argument
            this.species1InitialValues = getArguments().getFloatArray("species_1_values");
            this.species2InitialValues = getArguments().getFloatArray("species_2_values");
            this.species1PlottingValues = Arrays.copyOf(species1InitialValues, species1InitialValues.length);
            this.species2PlottingValues = Arrays.copyOf(species2InitialValues, species2InitialValues.length);
            this.xValues = getArguments().getFloatArray("x_values");
            this.animationViewHeight = getArguments().getInt("animation_view_height");
            this.animationViewWidth = getArguments().getInt("animation_view_width");

            this.animationPlaying = false;
            this.boundaryConditions = "Constant Value";
        }//end of if statement
    }//end of onCreate method

    /* Sets up the view class */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ternary_free_form_animation, container, false);

        mTernaryAnimationView = (TernaryAnimationView) v.findViewById(R.id.ternary_animation_view);
        mTernaryAnimationView.getLayoutParams().height = this.animationViewHeight;
        mTernaryAnimationView.getLayoutParams().width = this.animationViewWidth;
        mTernaryAnimationView.setSpecies1InitialValues(Arrays.copyOf(species1InitialValues, species1InitialValues.length));
        mTernaryAnimationView.setSpecies1PlottingValues(Arrays.copyOf(species1InitialValues, species1InitialValues.length));
        mTernaryAnimationView.setSpecies2InitialValues(Arrays.copyOf(species2InitialValues, species2InitialValues.length));
        mTernaryAnimationView.setSpecies2PlottingValues(Arrays.copyOf(species2InitialValues, species2InitialValues.length));
        mTernaryAnimationView.setXValues(xValues);

        mTernaryFreeFormModel = new TernaryFreeFormModel(species1PlottingValues, species2InitialValues, animationViewHeight);

        mPlayButton = (Button) v.findViewById(R.id.ternary_animation_play);
        mPlayButton.setOnClickListener(this);

        mRestartButton = (Button) v.findViewById(R.id.ternary_animation_restart);
        mRestartButton.setOnClickListener(this);

        return v;
    }//end of onCreateView method

    @Override
    public void onClick(View v){

        switch (v.getId()){

            case R.id.ternary_animation_play:
                if(!animationPlaying){
                    playAnimation();
                } else{
                    pauseAnimation();
                }

                break;

            case R.id.ternary_animation_restart:
                restartAnimation();
                break;

        }//end of switch statement

    }//end of onClick method


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {

            //variables in here i think

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        playingOrPaused = 0;
        animationPlaying = false;
    }//end of onDetach method

    public void playAnimation(){

        this.animationPlaying = true;
        playingOrPaused = 1;
        pt = new PlayThread();
        pt.start();
//        mTernaryFreeFormModel.solutionOneStep();
//        species1PlottingValues = mTernaryFreeFormModel.getSpecies1PlottingValues();
//        species2PlottingValues = mTernaryFreeFormModel.getSpecies2PlottingValues();
//        mTernaryAnimationView.setSpecies1PlottingValues(species1PlottingValues);
//        mTernaryAnimationView.setSpecies2PlottingValues(species2PlottingValues);
//        mTernaryAnimationView.updateView();
//        String spec1 = "[";
//        for(int i=0; i<species1PlottingValues.length; i++){
//            spec1 += species1PlottingValues[i] + ",";
//        } spec1 += "]";
//        System.out.println("spec1: " + spec1);
//        String spec2="[";
//        for(int i=0; i<species2PlottingValues.length; i++){
//            spec2 += species2PlottingValues[i] + ",";
//        } spec2 += "]\n";
//
//        System.out.println("spec2: "+spec2);
    }//end of playAnimation method

    public void pauseAnimation(){

        if(playingOrPaused==0){
            playingOrPaused = 1;
        } else if(playingOrPaused==1){
            playingOrPaused = 0;
        }

    }//end of pauseAnimation method

    public void restartAnimation(){

        if(animationPlaying){

            animationPlaying = false;
            pt = null;
            mTernaryAnimationView.restartAnimation();
            mTernaryFreeFormModel.restartAnimation();

        }//end of if statement

    }//end of restartAnimation method


    /* Thread which runs the model and handles the animations */
    class PlayThread extends Thread{

        public Handler handler = new Handler();

        @Override
        public void run(){

            if(animationPlaying){

                switch(playingOrPaused){

                    case 0: //paused
                        handler.postDelayed(this, 1);
                        break;

                    case 1: //playing

                        mTernaryFreeFormModel.solutionOneStep();
                        species1PlottingValues = mTernaryFreeFormModel.getSpecies1PlottingValues();
                        species2PlottingValues = mTernaryFreeFormModel.getSpecies2PlottingValues();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("***** animation updating *****");
                                mTernaryAnimationView.setSpecies1PlottingValues(species1PlottingValues);
                                mTernaryAnimationView.setSpecies2PlottingValues(species2PlottingValues);
                                mTernaryAnimationView.updateView();

                            }//end of inner run method
                        });
                        handler.postDelayed(this, 1);
                        break;

                }//end of switch statement

            }//end of if


        }//end of run method

    }//end of inner class PlayThread



}//end of class

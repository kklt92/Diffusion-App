package com.example.diffusion.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.diffusion.app.FreeFormBinaryDiffusion.FreeFormBinaryActivity;
import com.example.diffusion.app.FreeFormTernaryDiffusion.TernaryFreeFormActivity;
import com.example.diffusion.app.PracticeSection.PracticeGraphActivity;


public class MainActivity extends Activity implements View.OnClickListener{

    /* Instance Variables */
    private TextView homeScreenTextView;
    private Button binaryDiffusionButton, ternaryDiffusionButton, practiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiate the instance variables
        homeScreenTextView = (TextView) findViewById(R.id.homeScreenTextView);

        binaryDiffusionButton = (Button) findViewById(R.id.binary_difF_btn);
        binaryDiffusionButton.setOnClickListener(this);

        ternaryDiffusionButton = (Button) findViewById(R.id.ternary_diff_btn);
        ternaryDiffusionButton.setOnClickListener(this);

        practiceButton = (Button) findViewById(R.id.practice_btn);
        practiceButton.setOnClickListener(this);


    }//end of onCreate method


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }//end of onCreateOptionsMenu method

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
    }//end of onOptionsItemSelected method

    @Override
    public void onClick(View v){

        switch(v.getId()){

            case R.id.binary_difF_btn:
                Intent intent1 = new Intent(this, FreeFormBinaryActivity.class);
                startActivity(intent1);
                break;

            case R.id.ternary_diff_btn:
                Intent intent2 = new Intent(this, TernaryFreeFormActivity.class);
                startActivity(intent2);
                break;

            case R.id.practice_btn:
                Intent intent3 = new Intent(this, PracticeGraphActivity.class);
                startActivity(intent3);
                break;

        }//end of switch statement

    }//end of onClick method

}//end of MainActivity class

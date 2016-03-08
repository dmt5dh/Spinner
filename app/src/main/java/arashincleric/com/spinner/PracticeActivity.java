package arashincleric.com.spinner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PracticeActivity extends AbstractTaskActivity implements
        SinglePracticeFragment.OnSinglePracticeFragmentInteractionListener,
        TaskFragment.OnTaskFragmentInteractionListener{

    private FragmentManager fragmentManager;
    private Fragment mContent;

    private ArrayList<Wheel> wheelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        wheelList = new ArrayList<Wheel>();
        wheelList.add(new Wheel(new float[]{180, 180}, new int[]{1,2}, new int[]{Color.RED, Color.BLUE}));
        wheelList.add(new Wheel(new float[]{90,90,90,90}, new int[]{4, 5, 6, 7}, new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW}));

        super.userID = getIntent().getStringExtra("USERID");

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = SinglePracticeFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

        super.setupUI(findViewById(android.R.id.content));
    }

    @Override
    public Wheel getWheelFromListSingle(){
        return wheelList.get(0);
    }

    @Override
    public void nextScreenSingle(){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = TaskFragment.newInstance();
        transaction.replace(R.id.fragmentContainer, mContent).commit();

        TextView instructionView = (TextView)findViewById(R.id.instructionText);
        instructionView.setText(getResources().getString(R.string.practice_instructions_double));
    }

    @Override
    public void fullScreenSingle(){
        super.turnFullScreen();
    }

    @Override
    public void logEventSingle(String action, String result, String outcome){
        try{
            super.logEvent(Calendar.getInstance(), "Practice", action, result, outcome);
        }
        catch(Exception e){
            Log.e("ERROR", "Error logging " + result);
        }
    }

    @Override
    public Wheel getWheelFromList(){
        Wheel w = wheelList.get(0);
        if(wheelList.size() > 1){
            wheelList.remove(0);
        }
        return w;
    }

    @Override
    public void nextScreen(boolean isLeftSelected){
        Intent intent = new Intent(PracticeActivity.this, TaskActivity.class);
        intent.putExtra("USERID", userID);
        startActivity(intent);
        finish();
    }

    @Override
    public void logEventTask(String action, String result, String outcome){
        try{
            super.logEvent(Calendar.getInstance(), "Practice" , action, result, outcome);
        }
        catch(Exception e){
            Log.e("ERROR", "Error logging " + result);
        }
    }

    @Override
    public void fullScreen(){
        super.turnFullScreen();
    }
}

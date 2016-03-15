package arashincleric.com.spinner;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

        wheelList = super.initializeWheelList(true);
//        wheelList.add(new Wheel(new float[]{180, 180}, new int[]{1,2}, new int[]{Color.RED, Color.BLUE}));
//        wheelList.add(new Wheel(new float[]{90,90,90,90}, new int[]{4, 5, 6, 7}, new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW}));

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
        Wheel w = wheelList.get(0);
        if(wheelList.size() > 1){
            wheelList.remove(0);
        }
        return w;
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
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        transaction.remove(mContent).commit();
        fragmentManager.executePendingTransactions();

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

    @Override
    public void showConfirmation(){
        TaskFragment currentTask = (TaskFragment)mContent;
        final boolean isLeftSelected = currentTask.isLeftSelectBtnChecked();
        boolean isRightSelected = currentTask.isRightSelectBtnChecked();

        if (!isLeftSelected && !isRightSelected) {
            logEventTask("Clicked continue", "Practice task not resolved", "-");
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_checks)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            turnFullScreen();
                        }
                    })
                    .setNeutralButton(R.string.cancel_btn, null)
                    .show();
        } else {
            //LOG: log confirmation screen click
            logEventTask("Clicked continue", "Practice task resolution", "-");
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_wheel_msg_practice2)
                    .setPositiveButton(R.string.confirm_wheel_msg_practice_yes, new DialogInterface.OnClickListener() { //LOG: log confirm click
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logEventTask("Confirm continue", "Practice task resolution", "-");
                            nextScreen(isLeftSelected);
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            turnFullScreen();
                        }
                    })
                    .setNegativeButton(R.string.confirm_wheel_msg_practice_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //LOG: log cancel click
                            logEventTask("Cancel continue", "Practice task resolution", "-");
                        }
                    })
                    .show();
        }
    }
}

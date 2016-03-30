package arashincleric.com.spinner;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class TaskActivity extends AbstractTaskActivity implements TaskFragment.OnTaskFragmentInteractionListener,
        StartTaskFragment.OnStartTaskFragmentInteractionListener{

    /** PARAMETERS TO CHANGE**/
    boolean randomizeList = false;

    /**PARAMETERS TO CHANGE**/

    private FragmentManager fragmentManager;
    private Fragment mContent;
    private ArrayList<Wheel> wheelList;
    private ArrayList<Boolean> selected;
    private int curWheelIndex;
    private TextView gameNumberText;

    protected int stageNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        wheelList = super.initializeWheelList(false);


        curWheelIndex = 0;

        selected = new ArrayList<Boolean>();
        if(randomizeList){
            long seed = System.nanoTime();
            Collections.shuffle(wheelList, new Random(seed));
        }

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = StartTaskFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

        stageNum = 1;
        gameNumberText = (TextView)findViewById(R.id.gameNumberText);

        super.userID = getIntent().getStringExtra("USERID");
        createUserParamsFile();

        super.setupUI(findViewById(android.R.id.content));
    }

    /**
     * Store the user parameters in a file
     */
    protected void createUserParamsFile(){
        //Check external storage available
        if(!isExternalStorageWritable()){
            Toast.makeText(this, "External Storage not writable. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        int currentAPIVersion = Build.VERSION.SDK_INT;
        File root;
        if(currentAPIVersion >= Build.VERSION_CODES.KITKAT){ //Get API version because external storage is different depending
            File[] dirs = ContextCompat.getExternalFilesDirs(this, null);
            root = dirs[0];
        }
        else{
            root = Environment.getExternalStorageDirectory();
        }

        super.fileRoot = new File(root + "/spinnerData");
        //Check if file directory exists. If not, create it and check if it was created.
        super.filePathParams = new File(super.fileRoot + "/UserParams");
        if(!super.filePathParams.exists()){
            boolean makeDir = super.filePathParams.mkdirs(); //Can't use this to check because it is false for both error and dir exists
        }
        if(!super.filePathParams.exists() || !super.filePathParams.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }
        String paramsFileName = super.userID + dateTimeFormat.format(Calendar.getInstance().getTime()
        );

        String userParamsFileName = paramsFileName + "_PARAMS.txt";

        //Create event logs
        super.userParamsFile = new File(super.filePathParams, userParamsFileName);
        if(!super.userParamsFile.exists()){
            try{
                FileOutputStream paramLogFileStream = new FileOutputStream(super.userParamsFile);
                for(int i = 0; i < wheelList.size(); i = i + 2){
                    StringBuilder column = new StringBuilder(); //first column

                    StringBuilder colorColumn = new StringBuilder();
                    colorColumn.append("\tColor\t");

                    StringBuilder priceColumn = new StringBuilder();
                    priceColumn.append("\tPrice\t");

                    StringBuilder probColumn = new StringBuilder();
                    probColumn.append("\tProbability\t");

                    column.append("Task " + ((i / 2) + 1) + "\t"
                            + "\t"
                            + "Left");
                    Wheel leftWheel = wheelList.get(i);
                    for(int j = 0; j < leftWheel.getSiezeValue_degree(); j++){ //iterate through sections to allocate space
                        column.append("\t");
                        colorColumn.append(leftWheel.getColorNames()[j] + "\t");
                        priceColumn.append(leftWheel.getScores()[j] + "\t");
                        probColumn.append((leftWheel.getValue_degree()[j] / 360) + "\t");
                    }

                    column.append("Right");
                    Wheel rightWheel = wheelList.get(i + 1);
                    for(int j = 0; j < rightWheel.getSiezeValue_degree(); j++){
                        column.append("\t");
                        colorColumn.append(rightWheel.getColorNames()[j] + "\t");
                        priceColumn.append(rightWheel.getScores()[j] + "\t");
                        probColumn.append((rightWheel.getValue_degree()[j] / 360) + "\t");
                    }
                    column.append("\n");
                    colorColumn.append("\n");
                    priceColumn.append("\n");
                    probColumn.append("\n");
                    column.append(colorColumn).append(priceColumn).append(probColumn);
                    paramLogFileStream.write(column.toString().getBytes());
                }
                paramLogFileStream.close();
            } catch (Exception e){
                Toast.makeText(this, "Error creating event file. Exiting...", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed(){

    }

    @Override
    public Wheel getWheelFromList(){
        if(curWheelIndex < wheelList.size()){
            Wheel w = wheelList.get(curWheelIndex);
            selected.add(false);
            curWheelIndex++;
            return w;
        }
        else{
            return null;
        }
    }

    @Override
    public void nextScreen(boolean isLeftSelected){
        if(isLeftSelected){
            selected.set(curWheelIndex - 2, true);
        }
        else{
            selected.set(curWheelIndex - 1, true);
        }

        if(curWheelIndex < wheelList.size()){
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.disallowAddToBackStack();
//            transaction.detach(mContent);
            mContent = TaskFragment.newInstance();
//            transaction.attach(mContent).commit();
            transaction.replace(R.id.fragmentContainer, mContent).commit();
            fragmentManager.executePendingTransactions();
//            turnFullScreen();
            stageNum++;
            gameNumberText.setText(Integer.toString(stageNum));
        }
        else{
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.disallowAddToBackStack();
            transaction.remove(mContent).commit();
            fragmentManager.executePendingTransactions();

            Intent intent = new Intent(TaskActivity.this, WheelListActivity.class);
            for(int i = 0; i < wheelList.size(); i++){
                if(selected.get(i)){
                    Wheel tmp = wheelList.get(i);
                    tmp.setChosen(true);
                    wheelList.set(i, tmp);
                }
            }
            intent.putParcelableArrayListExtra("WHEELLIST", wheelList);
            intent.putExtra("USERID", userID);

            startActivity(intent);
            finish();
        }
    }

    @Override
    public void fullScreen(){
        super.turnFullScreen();
    }

    @Override
    public void logEventTask(String action, String result, String outcome){
        try{
            super.logEvent(Calendar.getInstance(), "Task " + stageNum, action, result, outcome);
        }
        catch(Exception e){
            Log.e("ERROR", "Error logging " + result);
        }
    }

    @Override
    public void showConfirmation(){
        TaskFragment currentTask = (TaskFragment)mContent;
        final boolean isLeftSelected = currentTask.isLeftSelectBtnChecked();
        boolean isRightSelected = currentTask.isRightSelectBtnChecked();

        if (!isLeftSelected && !isRightSelected) {
            logEventTask("Clicked continue", "Task not resolved", "-");
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
            logEventTask("Clicked continue", "Task resolution", "-");
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_wheel_msg)
                    .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() { //LOG: log confirm click
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logEventTask("Confirm continue", "Task resolution", "-");
                            nextScreen(isLeftSelected);
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            turnFullScreen();
                        }
                    })
                    .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //LOG: log cancel click
                            logEventTask("Cancel continue", "Task resolution", "-");
                        }
                    })
                    .show();
        }
    }

    @Override
    public void startTasks(){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = TaskFragment.newInstance();
        transaction.replace(R.id.fragmentContainer, mContent).commit();

        gameNumberText.setText(Integer.toString(stageNum));
    }

}

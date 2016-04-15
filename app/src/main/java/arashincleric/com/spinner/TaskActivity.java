package arashincleric.com.spinner;

import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

public class TaskActivity extends AbstractTaskActivity implements TaskFragment.OnTaskFragmentInteractionListener,
        StartTaskFragment.OnStartTaskFragmentInteractionListener{

    /** PARAMETERS TO CHANGE**/
    boolean randomizeList = false;

    /**PARAMETERS TO CHANGE**/

    private FragmentManager fragmentManager;
    private Fragment mContent;
    private ArrayList<WheelTuple> wheelList;
    private ArrayList<Boolean> selected;
    private int curWheelIndex; //To keep track of which spinners used
    private int curWheelTupleIndex; //To keep track of what has been chosen
    private TextView gameNumberText;

    protected int stageNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        wheelList = initializeWheelTupleList();


        curWheelIndex = 0;
        curWheelTupleIndex = 0;

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


    public ArrayList<WheelTuple> initializeWheelTupleList(){
        //Read wheel data from JSON
        InputStream is;
        is = getResources().openRawResource(R.raw.task);

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try{
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1){
                writer.write(buffer, 0, n);
            }
            is.close();
        }
        catch (IOException e){
            Log.e("ERROR", "Error loading wheels");
        }

        ArrayList<WheelTuple> wheelList = new ArrayList<WheelTuple>();
        String json = writer.toString();
        try{
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0; i < jsonArray.length(); i = i+ 2 ){
                JSONObject jsonWheelLeft = jsonArray.getJSONObject(i).getJSONObject("spinner"); //Get first(left) spinner
                JSONArray sectionsJsonArrayLeft = jsonWheelLeft.getJSONArray("sections");
                JSONArray priceJsonArrayLeft = jsonWheelLeft.getJSONArray("pricelist");
                JSONArray colorJsonListLeft = jsonWheelLeft.getJSONArray("colorlist");

                float[] sectionsArrayLeft = new float[sectionsJsonArrayLeft.length()];
                int[] priceArrayLeft = new int[sectionsJsonArrayLeft.length()];
                String[] colorArrayLeft = new String[sectionsJsonArrayLeft.length()];
                for(int j = 0; j < sectionsJsonArrayLeft.length(); j++){
                    sectionsArrayLeft[j] = (float)((int)sectionsJsonArrayLeft.get(j) / 1.0); //To convert int to float
                    priceArrayLeft[j] = (int)priceJsonArrayLeft.get(j);
                    colorArrayLeft[j] = (String)colorJsonListLeft.get(j);
                }

                JSONObject jsonWheelRight = jsonArray.getJSONObject(i + 1).getJSONObject("spinner"); //Get second(right) spinner
                JSONArray sectionsJsonArrayRight = jsonWheelRight.getJSONArray("sections");
                JSONArray priceJsonArrayRight = jsonWheelRight.getJSONArray("pricelist");
                JSONArray colorJsonListRight = jsonWheelRight.getJSONArray("colorlist");

                float[] sectionsArrayRight = new float[sectionsJsonArrayRight.length()];
                int[] priceArrayRight = new int[sectionsJsonArrayRight.length()];
                String[] colorArrayRight = new String[sectionsJsonArrayRight.length()];
                for(int j = 0; j < sectionsJsonArrayRight.length(); j++){
                    sectionsArrayRight[j] = (float)((int)sectionsJsonArrayRight.get(j) / 1.0); //To convert int to float
                    priceArrayRight[j] = (int)priceJsonArrayRight.get(j);
                    colorArrayRight[j] = (String)colorJsonListRight.get(j);
                }

                Wheel lWheel = new Wheel(this, sectionsArrayLeft, priceArrayLeft, colorArrayLeft);
                Wheel rWheel = new Wheel(this,sectionsArrayRight, priceArrayRight, colorArrayRight);

                wheelList.add(new WheelTuple(lWheel, rWheel));
            }
        }
        catch (JSONException e){
            Log.e("ERROR", "Error loading wheels...json");
        }

        return wheelList;
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
                for(int i = 0; i < wheelList.size(); i++){
                    StringBuilder column = new StringBuilder(); //first column

                    StringBuilder colorColumn = new StringBuilder();
                    colorColumn.append("\tColor\t");

                    StringBuilder priceColumn = new StringBuilder();
                    priceColumn.append("\tPrice\t");

                    StringBuilder probColumn = new StringBuilder();
                    probColumn.append("\tProbability\t");

                    column.append("Task " + (i + 1) + "\t"
                            + "\t"
                            + "Left");
                    Wheel leftWheel = wheelList.get(i).left;
                    for(int j = 0; j < leftWheel.getSiezeValue_degree(); j++){ //iterate through sections to allocate space
                        column.append("\t");
                        colorColumn.append(leftWheel.getColorNames()[j] + "\t");
                        priceColumn.append(leftWheel.getScores()[j] + "\t");
                        probColumn.append((leftWheel.getValue_degree()[j] / 360) + "\t");
                    }

                    column.append("Right");
                    Wheel rightWheel = wheelList.get(i).right;
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
    public Wheel getWheelFromList(boolean isLeft){
        if(curWheelIndex < wheelList.size()){
            Wheel w;
            if(isLeft){
                w = wheelList.get(curWheelIndex).left;
            }
            else{
                w = wheelList.get(curWheelIndex).right;
                curWheelIndex++; //Advance from here, because if we get the right spinner we move on to next screen
            }
            selected.add(false); //Default to unselected
            curWheelTupleIndex++;
            return w;
        }
        else{
            return null;
        }
    }

    @Override
    public void nextScreen(boolean isLeftSelected){
        if(isLeftSelected){ //Check which was selected and set as true
            selected.set(curWheelTupleIndex - 2, true);
        }
        else{
            selected.set(curWheelTupleIndex - 1, true);
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
            for(int i = 0; i < selected.size(); i++){ //Check which ones were selected and mark accordingly... should've just marked accordingly when we switched screens...
                if(selected.get(i)){
                    Wheel tmp;
                    WheelTuple tmpTuple = wheelList.get(i / 2);
                    if( i % 2 == 0){ // looking at left
                        tmp = wheelList.get(i / 2).left;
                        tmp.setChosen(true);
                        tmpTuple.left = tmp;
                    }
                    else{ //looking at right
                        tmp = wheelList.get(i / 2).right;
                        tmp.setChosen(true);
                        tmpTuple.right = tmp;
                    }
                    wheelList.set(i / 2, tmpTuple);
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
            logEventTask("Clicked continue without choice", "Task not resolved", "-");
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_checks)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            turnFullScreen();
                        }
                    })
                    .setNeutralButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logEventTask("Cancelled without choice", "Task not resolved", "-");
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            //LOG: log confirmation screen click
            logEventTask("Clicked continue with choice", "Task resolution", "-");
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
                            logEventTask("Cancelled with choice", "Task resolution", "-");
                        }
                    })
                    .setCancelable(false)
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

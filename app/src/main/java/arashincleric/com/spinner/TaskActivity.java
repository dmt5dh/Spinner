package arashincleric.com.spinner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class TaskActivity extends FragmentActivity implements TaskFragment.OnTaskFragmentInteractionListener{

    /** PARAMETERS TO CHANGE**/
    boolean randomizeList = true;

    /**PARAMETERS TO CHANGE**/

    private FragmentManager fragmentManager;
    private Fragment mContent;
    private ArrayList<Wheel> wheelList;
    private ArrayList<Boolean> selected;
    private int curWheelIndex;

    //File stuff here
    private File filePath;
    private File eventLogFile;
    public final static String EVENT_DATA_COLUMNS =
            "TabletID\tUserID\tDate\tTime\tStage\tAction\tResult\tOutcome\n";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    private String userID;

    private int stageNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        wheelList = new ArrayList<Wheel>();
        wheelList.add(new Wheel(new float[]{30,60,270}, new int[]{1, 2, 3}, new int[]{Color.RED, Color.BLUE, Color.GREEN}));
        wheelList.add(new Wheel(new float[]{10,90,260}, new int[]{4, 5, 6}, new int[]{Color.RED, Color.BLUE, Color.GREEN}));
        wheelList.add(new Wheel(new float[]{30,60,90, 90, 90}, new int[]{9, 10, 11, 12,13}, new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN}));
        wheelList.add(new Wheel(new float[]{180,180}, new int[]{1, 2}, new int[]{Color.RED, Color.BLUE}));

        curWheelIndex = 0;

        selected = new ArrayList<Boolean>();
        if(randomizeList){
            long seed = System.nanoTime();
            Collections.shuffle(wheelList, new Random(seed));
        }

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = TaskFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

        stageNum = 1;

        userID = getIntent().getStringExtra("USERID");

        setupUI(findViewById(android.R.id.content));

        createLogFiles();
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
            turnFullScreen();
            stageNum++;
        }
        else{
            Intent intent = new Intent(TaskActivity.this, WheelListActivity.class);
            for(int i = 0; i < wheelList.size(); i++){
                if(selected.get(i)){
                    Wheel tmp = wheelList.get(i);
                    tmp.setChosen(true);
                    wheelList.set(i, tmp);
                }
            }
            intent.putParcelableArrayListExtra("WHEELLIST", wheelList);
            //TODO: easier to use a boolean array than hashmap?
            startActivity(intent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        turnFullScreen(); //Hide status and nav bars
    }

    /**
     * Method to hid status/nav bars
     */
    public void turnFullScreen(){
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else{
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * Put a listener on every view to hide softkeyboard if edittext not chosen
     * @param view The view to set up
     */
    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    turnFullScreen();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    /** Checks if external storage is available for read and write **/
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if log files present, if not create them.
     */
    private void createLogFiles(){

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

        //Check if file directory exists. If not, create it and check if it was created.
        filePath = new File(root + "/LogData");
        if(!filePath.exists()){
            boolean makeDir = filePath.mkdirs(); //Can't use this to check because it is false for both error and dir exists
        }
        if(!filePath.exists() || !filePath.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }
        String logFileName = "Test";

        String eventLogFileName = logFileName + "_DATA.txt";
//        String userLogFileName = logFileName + "_USERS.txt";

        //Create event logs
        eventLogFile = new File(filePath, eventLogFileName);
        if(!eventLogFile.exists()){
            try{
                FileOutputStream eventLogFileStream = new FileOutputStream(eventLogFile);
                eventLogFileStream.write(EVENT_DATA_COLUMNS.getBytes());
                eventLogFileStream.close();
            } catch (Exception e){
                Toast.makeText(this, "Error creating event file. Exiting...", Toast.LENGTH_LONG).show();
                finish();
            }
        }

//        //Create user logs
//        userLogFile = new File(filePath, userLogFileName);
//        if(!userLogFile.exists()){
//            try{
//                FileOutputStream userLogFileStream = new FileOutputStream(userLogFile);
//                userLogFileStream.write(USER_DATA_COLUMNS.getBytes());
//                userLogFileStream.close();
//            } catch(Exception e){
//                Toast.makeText(this, "Error creating user file. Exiting...", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
    }

    /**
     * Method to write to a file
     * @param f file to write to
     * @param dataToSave String data to write
     * @throws Exception
     */
    public void writeToFile(File f, String dataToSave) throws Exception{
        FileOutputStream eventLogFileStream = new FileOutputStream(f, true);
        eventLogFileStream.write(dataToSave.getBytes());
        eventLogFileStream.close();
    }

    /**
     * Save the every event that occurred
     * @param now time event happened
     * @param action what action occurred
     * @param result details on action
     * @param outcome if applicable, get score
     * @throws Exception
     */
    public void logEvent(Calendar now, String action, String result, String outcome) throws Exception{

        String dataToSave = "TabletIDHOLDER\t"
                + userID + "\t"
                + dateFormat.format(now.getTime()) + "\t"
                + timeFormat.format(now.getTime()) + "\t"
                + stageNum + "\t"
                + action + "\t"
                + result + "\t"
                + outcome + "\n";

        writeToFile(eventLogFile, dataToSave);
    }

}

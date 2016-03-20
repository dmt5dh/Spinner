package arashincleric.com.spinner;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public abstract class AbstractTaskActivity extends FragmentActivity {

    //CHANGE TABLETID HERE
    public static final String tabletId = "tabletID";
    public static final String versionId = "v1";
    //CHANGE TABLETID ABOVE

    //File stuff here
    protected File fileRoot;
    protected File filePathLog;
    protected File filePathParams;
    protected File eventLogFile;
    protected File userParamsFile;
    public final static String EVENT_DATA_COLUMNS =
            "TabletID\tUserID\tDate\tTime\tStage\tAction\tResult\tOutcome\n";
    protected final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    protected final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    protected final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.US);
    protected String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createLogFiles();
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
    protected void createLogFiles(){

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

        fileRoot = new File(root + "/spinnerData");
        //Check if file directory exists. If not, create it and check if it was created.
        filePathLog = new File(fileRoot + "/LogData");
        if(!filePathLog.exists()){
            boolean makeDir = filePathLog.mkdirs(); //Can't use this to check because it is false for both error and dir exists
        }
        if(!filePathLog.exists() || !filePathLog.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        String eventLogFileName = tabletId + "_DATA_" + versionId + ".txt";
//        String userLogFileName = logFileName + "_USERS.txt";

        //Create event logs
        eventLogFile = new File(filePathLog, eventLogFileName);
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
    }

    /**
     * Method to write to a file
     * @param f file to write to
     * @param dataToSave String data to write
     * @throws Exception
     */
    public synchronized void writeToFile(File f, String dataToSave) throws Exception{
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
    public void logEvent(Calendar now, String stage, String action, String result, String outcome) throws Exception{

        String dataToSave = tabletId + "\t"
                + userID + "\t"
                + dateFormat.format(now.getTime()) + "\t"
                + timeFormat.format(now.getTime()) + "\t"
                + stage + "\t"
                + action + "\t"
                + result + "\t"
                + outcome + "\n";

        writeToFile(eventLogFile, dataToSave);
    }

    public ArrayList<Wheel> initializeWheelList(boolean isPractice){
        //Read wheel data from JSON
        InputStream is;
        if(isPractice){
            is = getResources().openRawResource(R.raw.practice);
        }
        else{
            is = getResources().openRawResource(R.raw.task);
        }
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

        ArrayList<Wheel> wheelList = new ArrayList<Wheel>();
        String json = writer.toString();
        try{
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonWheel = jsonArray.getJSONObject(i).getJSONObject("wheel");
                JSONArray sectionsJsonArray = jsonWheel.getJSONArray("sections");
                JSONArray priceJsonArray = jsonWheel.getJSONArray("pricelist");
                JSONArray colorJsonList = jsonWheel.getJSONArray("colorlist");

                float[] sectionsArray = new float[sectionsJsonArray.length()];
                int[] priceArray = new int[sectionsJsonArray.length()];
                String[] colorArray = new String[sectionsJsonArray.length()];
                for(int j = 0; j < sectionsJsonArray.length(); j++){
                    sectionsArray[j] = (float)((int)sectionsJsonArray.get(j) / 1.0); //To convert int to float
                    priceArray[j] = (int)priceJsonArray.get(j);
                    colorArray[j] = (String)colorJsonList.get(j);
                }
                wheelList.add(new Wheel(this, sectionsArray, priceArray, colorArray));
            }
        }
        catch (JSONException e){
            Log.e("ERROR", "Error loading wheels...json");
        }

        return wheelList;
    }
}

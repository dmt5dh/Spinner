package arashincleric.com.spinner;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class WheelListActivity extends ListActivity {

    private WheelListAdapter adapter;
    private ArrayList<WheelTuple> wheelArrayList;

    //File stuff here
    protected File filePath;
    protected File eventLogFile;
    public final static String EVENT_DATA_COLUMNS =
            "TabletID\tUserID\tDate\tTime\tStage\tAction\tResult\tOutcome\n";
    protected final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    protected final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    protected String userID;

    Button chooseRanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ArrayList<WheelTuple> tempList = getIntent().getParcelableArrayListExtra("WHEELLIST");
        wheelArrayList = new ArrayList<WheelTuple>();
        for(int i = 0; i < tempList.size(); i++){
            wheelArrayList.add(tempList.get(i));
        }

        adapter = new WheelListAdapter(this, wheelArrayList);
        getListView().setAdapter(adapter);

        boolean b = getListView().hasFocusable();

        chooseRanBtn = (Button)findViewById(R.id.chooseRandomBtn);
        chooseRanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(WheelListActivity.this)
                        .setMessage(R.string.confirm_list_sel)
                        .setNegativeButton(R.string.cancel_btn, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                turnFullScreen();
                            }
                        })
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //LOG: log selection here
                                Random random = new Random();
                                int gameSelection = random.nextInt(wheelArrayList.size());
                                startFinalScreen(gameSelection);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });

        userID = getIntent().getStringExtra("USERID");
        setupUI(findViewById(android.R.id.content));
        turnFullScreen();

        createLogFiles();

        //LOG: summary show
        try{
            logEvent(Calendar.getInstance(), "Summary", "Show", "-", "-");
        }
        catch(Exception e){
            Log.e("ERROR", "Error logging summary showing");
        }
    }

    @Override
    public void onBackPressed(){

    }

    public void startFinalScreen(int gameSelectionIndex){
        try {
            logEvent(Calendar.getInstance(), "Summary", "Game selection", "Game " + (gameSelectionIndex + 1) + " selected", "-");
            Intent intent = new Intent(WheelListActivity.this, FinalGameActivity.class);
            WheelTuple wheelTuple = wheelArrayList.get(gameSelectionIndex);
            ArrayList<Wheel> wheelArrayToSend = new ArrayList<Wheel>();
            wheelArrayToSend.add(wheelTuple.left); //Left will always be first
            wheelArrayToSend.add(wheelTuple.right);
            intent.putParcelableArrayListExtra("WHEELLIST", wheelArrayToSend);
            intent.putExtra("USERNAME", userID);
            intent.putExtra("GAMENUM", gameSelectionIndex + 1);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("ERROR", "Error logging summary showing");
        }
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

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    turnFullScreen();
                    return false;
                }

            });

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

        String dataToSave = AbstractTaskActivity.tabletId + "\t"
                + userID + "\t"
                + dateFormat.format(now.getTime()) + "\t"
                + timeFormat.format(now.getTime()) + "\t"
                + stage + "\t"
                + action + "\t"
                + result + "\t"
                + outcome + "\n";

        writeToFile(eventLogFile, dataToSave);
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
        //Check if file directory exists. If not, create it and check if it was created.
        filePath = new File(root + "/spinnerData/LogData");
        if(!filePath.exists()){
            boolean makeDir = filePath.mkdirs(); //Can't use this to check because it is false for both error and dir exists
        }
        if(!filePath.exists() || !filePath.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        String eventLogFileName = AbstractTaskActivity.tabletId + "_DATA_" + AbstractTaskActivity.versionId + ".txt";
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
    }
}

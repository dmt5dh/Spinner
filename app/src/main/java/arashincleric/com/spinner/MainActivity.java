package arashincleric.com.spinner;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AbstractTaskActivity {

    private String userEmail;

    private EditText nameEntry;
    private EditText idEntry;
    private EditText adminEntry;
    private EditText locationEntry;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        nameEntry = (EditText) findViewById(R.id.nameEntryInput);
        idEntry = (EditText) findViewById(R.id.idEntryInput);
        adminEntry = (EditText) findViewById(R.id.adminEntryInput);
        locationEntry = (EditText) findViewById(R.id.locationEntryInput);
        locationEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if(result == EditorInfo.IME_ACTION_DONE || result == EditorInfo.IME_ACTION_NEXT){
                    turnFullScreen();
                }
                return false;
            }
        });

        submitBtn = (Button) findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() { //Attach even to button
            @Override
            public void onClick(View v) {
                int validated = validateEntries();
                if(validated == 0){
                    try{
                        logEvent(Calendar.getInstance(), "Login",
                                "Entered information",
                                "Name: " + nameEntry.getText().toString() + ", "
                                        + "Admin: " + adminEntry.getText().toString() + ", "
                                        + "Location: " + locationEntry.getText().toString(),
                                "-");
                    }
                    catch (Exception e){
                        Log.e("ERROR", "Error writing login information to file");
                    }
                    Intent intent = new Intent(MainActivity.this, PracticeActivity.class);
                    intent.putExtra("USERID", idEntry.getText().toString());
                    startActivity(intent);
                    finish();
                }
                else{
                    String error;
                    switch (validated){
                        case -1:
                            error = getResources().getString(R.string.login_validate_error);
                            break;
                        default:
                            error = "Error with fields";
                    }
                    new AlertDialog.Builder(v.getContext())
                            .setMessage(error)
                            .setNeutralButton(R.string.cancel_btn, null)
                            .show();
                }
            }
        });

        setupUI(findViewById(android.R.id.content));
    }

    public int validateEntries(){
        int validateResult = -1;
        if(!nameEntry.getText().toString().isEmpty()
                && !idEntry.getText().toString().isEmpty()
                && !adminEntry.getText().toString().isEmpty()
                && !locationEntry.getText().toString().isEmpty()){
            validateResult = 0;
            super.userID = idEntry.getText().toString();
        }
        return validateResult;
    }

    @Override
    public void onBackPressed(){

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
                    hideSoftKeyboard();
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

    /**
     * Hide soft keyboard
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(this.getCurrentFocus().getWindowToken() != null){
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
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
}

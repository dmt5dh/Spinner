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

public class MainActivity extends FragmentActivity {

    private String userEmail;

    private EditText nameEntry;
    private EditText idEntry;
    private EditText adminEntry;
    private EditText locationEntry;
    private Button submitBtn;
    UsernameDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new UsernameDbHelper(this);

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
                //TODO: do confirmation here later
                int emailMatch = 0;
                if(emailMatch == 0){
                    Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                    intent.putExtra("USERID", idEntry.getText().toString());
                    startActivity(intent);
                    finish();
                }
                else{
                    String error;
                    switch (emailMatch){
                        case 1:
                            error = getResources().getString(R.string.email_error_empty);
                            break;
                        case 2:
                            error = getResources().getString(R.string.email_error_invalid);
                            break;
                        case 3:
                            error = getResources().getString(R.string.email_error_match);
                            break;
                        case 4:
                            error = getResources().getString(R.string.email_error_redundant);
                            break;
                        default:
                            error = "Error with email";
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

    @Override
    public void onBackPressed(){

    }

//    /**
//     * Check if the two emails match and that they are not empty
//     * @return 1:empty field(s), 2:not valid email, 3:emails don't match, 0:good
//     */
//    public int confirmEmail(){
//        String email = emailEntry.getText().toString();
//        String emailConfirm = emailEntryConfirm.getText().toString();
//
//        SQLiteDatabase db = mDbHelper.getReadableDatabase();
//        String query = "SELECT * FROM " + UsernameContract.Usernames.TABLE_NAME + " WHERE " + UsernameContract.Usernames.COLUMN_NAME_ENTRY_ID + " = ?";
//        Cursor c = db.rawQuery(query, new String[]{emailEntry.getText().toString()});
//        int x = c.getCount();
//        if(email.isEmpty() || emailConfirm.isEmpty()){ //If either field empty
//            return 1;
//        }
//        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !Patterns.EMAIL_ADDRESS.matcher(emailConfirm).matches()){ //If fields arent emails
//            return 2;
//        }
//        else if(!email.equals(emailConfirm)){//If fields don't match
//            return 3;
//        }
//        else if(c.getCount() > 0){ //If user has registered before
//            return 4;
//        }
//        else{
//            return 0;
//        }
//    }

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

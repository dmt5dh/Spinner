package arashincleric.com.spinner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class TaskActivity extends FragmentActivity implements TaskFragment.OnTaskFragmentInteractionListener{

    /** PARAMETERS TO CHANGE**/
    boolean randomizeList = true;

    /**PARAMETERS TO CHANGE**/

    private FragmentManager fragmentManager;
    private Fragment mContent;
    private ArrayList<Wheel> wheelList;
    private HashMap<Wheel, Boolean> selected;
    private int curWheelIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        wheelList = new ArrayList<Wheel>();
        wheelList.add(new Wheel(this, new float[]{30,60,270}, new int[]{1, 2, 3}, new int[]{Color.RED, Color.BLUE, Color.GREEN}));
        wheelList.add(new Wheel(this, new float[]{10,90,260}, new int[]{4, 5, 6}, new int[]{Color.RED, Color.BLUE, Color.GREEN}));
        wheelList.add(new Wheel(this, new float[]{30,60,90, 90, 90}, new int[]{9, 10, 11, 12,13}, new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN}));
        wheelList.add(new Wheel(this, new float[]{180,180}, new int[]{1, 2}, new int[]{Color.RED, Color.BLUE}));

        curWheelIndex = 0;

        selected = new HashMap<>();
        if(randomizeList){
            long seed = System.nanoTime();
            Collections.shuffle(wheelList, new Random(seed));
        }

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = TaskFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();

        setupUI(findViewById(android.R.id.content));
    }

    @Override
    public Wheel getWheelFromList(){
        if(curWheelIndex < wheelList.size()){
            Wheel w = wheelList.get(curWheelIndex);
            selected.put(w, false);
            curWheelIndex++;
            return w;
        }
        else{
            return null;
        }
    }

    @Override
    public void nextScreen(boolean isLeftSelected, Wheel wLeft, Wheel wRight){
        if(isLeftSelected){
            selected.put(wLeft, true);
        }
        else{
            selected.put(wRight, true);
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
        }
        else{
            Intent intent = new Intent(TaskActivity.this, ListActivity.class);
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

}

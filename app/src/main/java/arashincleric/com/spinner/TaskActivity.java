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
import android.util.Log;
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

public class TaskActivity extends AbstractTaskActivity implements TaskFragment.OnTaskFragmentInteractionListener{

    /** PARAMETERS TO CHANGE**/
    boolean randomizeList = true;

    /**PARAMETERS TO CHANGE**/

    private FragmentManager fragmentManager;
    private Fragment mContent;
    private ArrayList<Wheel> wheelList;
    private ArrayList<Boolean> selected;
    private int curWheelIndex;

    protected int stageNum;


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

        super.userID = getIntent().getStringExtra("USERID");

        super.setupUI(findViewById(android.R.id.content));
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
            intent.putExtra("USERID", userID);
            //TODO: easier to use a boolean array than hashmap?
            startActivity(intent);
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

}

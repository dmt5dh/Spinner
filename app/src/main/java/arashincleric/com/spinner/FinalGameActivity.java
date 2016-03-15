package arashincleric.com.spinner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

public class FinalGameActivity extends AbstractTaskActivity implements FinalGameFragment.OnTaskFragmentInteractionListener {

    ArrayList<Wheel> wheelList;
    int gameNum;

    Fragment mContent;
    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_game);

        Intent intent = getIntent();
        wheelList = intent.getParcelableArrayListExtra("WHEELLIST");
        super.userID = intent.getStringExtra("USERNAME");
        gameNum = intent.getIntExtra("GAMENUM", 0);

        TextView gameNumText = (TextView)findViewById(R.id.gameNumberText);
        gameNumText.setText(Integer.toString(gameNum));

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = FinalGameFragment.newInstance(gameNum);
        transaction.add(R.id.fragmentContainer, mContent).commit();
    }

    @Override
    public Wheel getWheelFromList(){
        Wheel w = wheelList.get(0);
        wheelList.remove(0);
        return w;
    }

    @Override
    public void nextScreen(boolean isLeftSelected){
        //Do nothing
    }

    @Override
    public void fullScreen(){
        super.turnFullScreen();
    }

    @Override
    public void logEventTask(String action, String result, final String outcome){
        //LOG: log the score for the player here
        try{
            logEvent(Calendar.getInstance(), "Payment", "Game " + gameNum, result, outcome);
        }
        catch (Exception e){
            Log.e("ERROR", "Error logging payment data");
        }

        if(!outcome.equals("-") && !outcome.isEmpty()){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //TODO: finish everything here
                    Intent intent = new Intent(FinalGameActivity.this, QuestionnaireActivity.class);
                    intent.putExtra("USERNAME", userID);
                    intent.putExtra("SCORE", outcome);
                    startActivity(intent);
                }
            }, 3000);
        }

    }

    @Override
    public void showConfirmation(){
        //Do nothing
    }
}

package arashincleric.com.spinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class FinalGameActivity extends AbstractTaskActivity implements FinalGameFragment.OnTaskFragmentInteractionListener {

    ArrayList<Wheel> wheelList;
    int gameNum;
    int score;

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

        TextView gameNumText = (TextView)findViewById(R.id.gameNumberText); //Set game number
        gameNumText.setText(Integer.toString(gameNum));

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.disallowAddToBackStack();
        mContent = FinalGameFragment.newInstance();
        transaction.add(R.id.fragmentContainer, mContent).commit();
    }

    @Override
    public Wheel getWheelFromList(boolean isLeft){ //Get the first item and remove, should only be done twice on list size of 2
        Wheel w = wheelList.get(0);
        wheelList.remove(0);
        return w;
    }

    @Override
    public void nextScreen(boolean isLeftSelected){ //Go to questionnaire
        Intent intent = new Intent(FinalGameActivity.this, QuestionnaireActivity.class);
        intent.putExtra("USERNAME", userID);
        intent.putExtra("SCORE", score);
        startActivity(intent);
        finish();
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

        if(!outcome.equals("-") && !outcome.isEmpty()){ //Enable confirm button after a score is shown
            score = Integer.parseInt(outcome);
            ((FinalGameFragment)mContent).enableConfirmBtn();
        }

    }

    @Override
    public void showConfirmation(){
        //Do nothing
    }
}

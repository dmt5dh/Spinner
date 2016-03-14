package arashincleric.com.spinner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class FinalGameActivity extends AbstractTaskActivity implements FinalGameFragment.OnTaskFragmentInteractionListener {

    ArrayList<Wheel> wheelList;
    String userID;
    int gameNum;

    Fragment mContent;
    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_game);

        Intent intent = getIntent();
        wheelList = intent.getParcelableArrayListExtra("WHEELLIST");
        userID = intent.getStringExtra("USERNAME");
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
    public void logEventTask(String action, String result, String outcome){
        //TODO: log the score for the player here
        //TODO: freeze everything on final game fragment
        Toast.makeText(this, "completed", Toast.LENGTH_SHORT).show(); //This is for all events so just check for rotation complete
    }
}

package arashincleric.com.spinner;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;


public class FinalGameFragment extends TaskFragment {

    public static FinalGameFragment newInstance() {
        FinalGameFragment fragment = new FinalGameFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FinalGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        super.leftSelectBtn.setVisibility(View.GONE); //Hide checkboxes
        super.rightSelectBtn.setVisibility(View.GONE);

        super.confirmBtn.setEnabled(false); //Disable confirm
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.nextScreen(true);
            }
        });

        int id = getContext().getResources().getIdentifier("GREY", "color", getContext().getPackageName()); //Retriever grey color
        int colorId =getContext().getResources().getColor(id);

        TextView leftCheckBoxInst = (TextView)view.findViewById(R.id.checkBoxInstLeft);
        leftCheckBoxInst.setVisibility(View.GONE);
        TextView rightCheckBoxInst = (TextView)view.findViewById(R.id.checkBoxInstRight);
        rightCheckBoxInst.setVisibility(View.GONE);

        if(super.wLeft.getWheelObject().isChosen()){ //Hide stuff on the right and grey out spinner
            super.rightScoreView.setVisibility(View.GONE);
            super.rightSpinBtn.setVisibility(View.GONE);
            super.rightWheel.setEnabled(false);
            super.rightWheel.setOnTouchListener(null);
            super.rightWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY);

            super.leftSpinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allowRotatingLeft) { //Log spin
                        //LOG: log left click
                        mListener.logEventTask("Chosen Game", "Spun spinner", "-");
                        Random random = new Random();
                        int seed = random.nextInt(2000);
                        startTheSpinWithDirection("normal", 10000 + seed, true);
                    }
                    deactivateSpin(true); //Only spin once

                }
            });
        }
        else{ //Hide stuff on the left and grey out spinner
            super.leftScoreView.setVisibility(View.GONE);
            super.leftSpinBtn.setVisibility(View.GONE);
            super.leftWheel.setEnabled(false);
            super.leftWheel.setOnTouchListener(null);
            super.leftWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY);

            super.rightSpinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allowRotatingRight) {
                        //LOG: log right click
                        mListener.logEventTask("Chosen Game", "Spun spinner", "-");
                        Random random = new Random();
                        int seed = random.nextInt(2000);
                        startTheSpinWithDirection("normal", 10000 + seed, false);
                    }
                    deactivateSpin(false);
                }
            });
        }
    }

    //Not needed but put here just in case... to let the user only spin the spinner once
    public void deactivateSpin(boolean isLeft){

        if(isLeft){
            leftSpinBtn.setEnabled(false);
            super.leftWheel.setOnTouchListener(null);
        }
        else{
            rightSpinBtn.setEnabled(false);
            super.rightWheel.setOnTouchListener(null);
        }
    }

    public void enableConfirmBtn(){
        confirmBtn.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (confirmBtn.isEnabled()) { //If the spin was not resolved before onStop called reattach myTouchListener to wheel
            deactivateSpin(true);
            deactivateSpin(false);
        }
        if(super.wLeft.getWheelObject().isChosen()){
            super.rightWheel.setEnabled(false);
            super.rightWheel.setOnTouchListener(null);
        }
        else{
            super.leftWheel.setEnabled(false);
            super.leftWheel.setOnTouchListener(null);
        }
    }

}

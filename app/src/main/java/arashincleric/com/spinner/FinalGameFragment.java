package arashincleric.com.spinner;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;


public class FinalGameFragment extends TaskFragment {

    public static FinalGameFragment newInstance(int gameNum) {
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

        super.leftSelectBtn.setVisibility(View.GONE);
        super.rightSelectBtn.setVisibility(View.GONE);

        super.confirmBtn.setEnabled(false);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.nextScreen(true);
            }
        });

        int id = getContext().getResources().getIdentifier("GREY", "color", getContext().getPackageName());
        int colorId =getContext().getResources().getColor(id);

        TextView leftCheckBoxInst = (TextView)view.findViewById(R.id.checkBoxInstLeft);
        leftCheckBoxInst.setVisibility(View.GONE);
        TextView rightCheckBoxInst = (TextView)view.findViewById(R.id.checkBoxInstRight);
        rightCheckBoxInst.setVisibility(View.GONE);

        if(super.wLeft.getWheelObject().isChosen()){
            super.rightScoreView.setVisibility(View.GONE);
            super.rightSpinBtn.setVisibility(View.GONE);
            rightWheel.setEnabled(false);
            rightWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY);

            super.leftSpinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allowRotatingLeft) {
                        //LOG: log left click
                        mListener.logEventTask("Chosen Game", "Spun spinner", "-");
                        startTheSpinWithDirection("normal", 1000, true);
                    }
                    deactivateSpin(true);

                }
            });
        }
        else{
            super.leftScoreView.setVisibility(View.GONE);
            super.leftSpinBtn.setVisibility(View.GONE);
            leftWheel.setEnabled(false);
            leftWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY);

            super.rightSpinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (allowRotatingRight) {
                        //LOG: log right click
                        mListener.logEventTask("Chosen Game", "Spun spinner", "-");
                        startTheSpinWithDirection("normal", 1000, false);
                    }
                    deactivateSpin(false);
                }
            });
        }
    }

    //Not needed but put here just in case...
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
    }

}

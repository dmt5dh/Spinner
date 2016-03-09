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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FinalGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FinalGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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

        super.leftSelectBtn.setVisibility(View.GONE);
        super.rightSelectBtn.setVisibility(View.GONE);

        super.confirmBtn.setVisibility(View.GONE);

        if(super.wLeft.getWheelObject().isChosen()){
            super.rightScoreView.setVisibility(View.GONE);
            super.rightSpinBtn.setVisibility(View.GONE);
            rightWheel.setEnabled(false);
            rightWheel.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        }
        else{
            super.leftScoreView.setVisibility(View.GONE);
            super.leftSpinBtn.setVisibility(View.GONE);
            leftWheel.setEnabled(false);
            leftWheel.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}

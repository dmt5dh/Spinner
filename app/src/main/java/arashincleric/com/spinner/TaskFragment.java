package arashincleric.com.spinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    protected OnTaskFragmentInteractionListener mListener;

    protected static Bitmap leftImageOriginal, leftImageScaled;
    protected static Bitmap rightImageOriginal, rightImageScaled;
    protected static Matrix matrixLeft, matrixRight;

    protected ImageView leftWheel, rightWheel;
    protected int leftHeight, leftWidth;
    protected int rightHeight, rightWidth;

    protected GestureDetector detectorLeft, detectorRight;

    // needed for detecting the inversed rotations
    protected boolean[] quadrantTouchedLeft, quadrantTouchedRight;

    protected boolean allowRotatingLeft, allowRotatingRight;

    protected Button leftSpinBtn, rightSpinBtn;

    protected WheelView wLeft;
    protected WheelView wRight;

    protected CheckBox leftSelectBtn;
    protected CheckBox rightSelectBtn;

    protected Button confirmBtn;

    protected TextView leftScoreView;
    protected TextView rightScoreView;

    protected Runnable flingRunnableLeft;
    protected Runnable flingRunnableRight;

    private boolean isFinal;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static TaskFragment newInstance() {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            wLeft = new WheelView(getContext(), mListener.getWheelFromList(true));
            wRight = new WheelView(getContext(), mListener.getWheelFromList(false));
        }

        //New instance so we want to redraw every time
        leftImageOriginal = null;
        leftImageScaled = null;
        rightImageOriginal = null;
        rightImageScaled = null;
        matrixLeft = null;
        matrixRight = null;

        setRetainInstance(true);

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

        // load the image only once
        if (leftImageOriginal == null) {
//            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.circletest);
            leftImageOriginal = wLeft.getBitmap();
        }
        // load the image only once
        if (rightImageOriginal == null) {
            rightImageOriginal = wRight.getBitmap();
        }

        // initialize the left matrix only once
        if (matrixLeft == null) {
            matrixLeft = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            matrixLeft.reset();
        }
        // initialize the right matrix only once
        if (matrixRight == null) {
            matrixRight = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            matrixRight.reset();
        }

        detectorLeft = new GestureDetector(getContext(), new MyGestureDetector(true));
        detectorRight = new GestureDetector(getContext(), new MyGestureDetector(false));

        // there is no 0th quadrant, to keep it simple the first value gets ignored
        quadrantTouchedLeft = new boolean[] { false, false, false, false, false };
        // there is no 0th quadrant, to keep it simple the first value gets ignored
        quadrantTouchedRight = new boolean[] { false, false, false, false, false };

        allowRotatingLeft = true;
        allowRotatingRight = true;

        leftWheel = (ImageView) view.findViewById(R.id.leftWheel);
        ViewGroup.LayoutParams leftWheelParams = leftWheel.getLayoutParams();
        leftWheelParams.width = leftWheelParams.height;
        leftWheel.setOnTouchListener(new MyOnTouchListener(true));
        leftWheel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (leftHeight == 0 || leftWidth == 0) {
                    leftHeight = leftWheel.getHeight();
                    leftWidth = leftWheel.getWidth();

                    // resize
                    Matrix resize = new Matrix();
                    resize.postScale((float) Math.min(leftWidth, leftHeight) / (float) leftImageOriginal.getWidth(), (float) Math.min(leftWidth, leftHeight) / (float) leftImageOriginal.getHeight());
                    leftImageScaled = Bitmap.createBitmap(leftImageOriginal, 0, 0, leftImageOriginal.getWidth(), leftImageOriginal.getHeight(), resize, false);

                    // translate to the image view's center
                    float translateX = leftWidth / 2 - leftImageScaled.getWidth() / 2;
                    float translateY = leftHeight / 2 - leftImageScaled.getHeight() / 2;
                    matrixLeft.postTranslate(translateX, translateY);

                    leftWheel.setImageBitmap(leftImageScaled);
                    leftWheel.setImageMatrix(matrixLeft);
                }
            }
        });

        rightWheel = (ImageView) view.findViewById(R.id.rightWheel);
        ViewGroup.LayoutParams rightWheelParams = rightWheel.getLayoutParams();
        rightWheelParams.width = rightWheelParams.height;
//        rightWheel.setColorFilter(Color.argb(255, 51, 51, 51), PorterDuff.Mode.SRC_OVER); //TODO:TWEAK THIS TO DIM(Multiply)
        rightWheel.setOnTouchListener(new MyOnTouchListener(false));
        rightWheel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (rightHeight == 0 || rightWidth == 0) {
                    rightHeight = rightWheel.getHeight();
                    rightWidth = rightWheel.getWidth();

                    // resize
                    Matrix resize = new Matrix();
                    resize.postScale((float) Math.min(rightWidth, rightHeight) / (float) rightImageOriginal.getWidth(), (float) Math.min(rightWidth, rightHeight) / (float) rightImageOriginal.getHeight());
                    rightImageScaled = Bitmap.createBitmap(rightImageOriginal, 0, 0, rightImageOriginal.getWidth(), rightImageOriginal.getHeight(), resize, false);

                    // translate to the image view's center
                    float translateX = rightWidth / 2 - rightImageScaled.getWidth() / 2;
                    float translateY = rightHeight / 2 - rightImageScaled.getHeight() / 2;
                    matrixRight.postTranslate(translateX, translateY);

                    rightWheel.setImageBitmap(rightImageScaled);
                    rightWheel.setImageMatrix(matrixRight);
                }
            }
        });

        leftSelectBtn = (CheckBox)view.findViewById(R.id.leftSelectBtn);
        rightSelectBtn = (CheckBox)view.findViewById(R.id.rightSelectBtn);

        leftSelectBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    rightSelectBtn.setChecked(false);
                    //LOG: log check left
                    mListener.logEventTask("Choice", "Chose left", "-");
                }
                else{
                    //TODO: log uncheck left?
                }
            }
        });

        rightSelectBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    leftSelectBtn.setChecked(false);
                    //LOG:log check right
                    mListener.logEventTask("Choice", "Chose right", "-");
                }
                else{
                    //TODO: log uncheck right?
                }
            }
        });

        confirmBtn = (Button)view.findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showConfirmation();
            }
        });

        leftScoreView = (TextView)view.findViewById(R.id.leftScore);
        String leftScore = getContext().getResources().getString(R.string.left_score);
        leftScoreView.setText(String.format(leftScore, "-"));
        rightScoreView = (TextView)view.findViewById(R.id.rightScore);
        String rightScore = getContext().getResources().getString(R.string.right_score);
        rightScoreView.setText(String.format(rightScore, "-"));

        leftSpinBtn = (Button)view.findViewById(R.id.leftSpinBtn);
        leftSpinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allowRotatingLeft){
                    //LOG: log left click
                    mListener.logEventTask("Experimenting", "Spin left", "-");
                    Random random = new Random();
                    int seed = random.nextInt(2000);
                    startTheSpinWithDirection("normal", 10000 + seed, true);
                }

            }
        });

        rightSpinBtn = (Button)view.findViewById(R.id.rightSpinBtn);
        rightSpinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allowRotatingRight){
                    //LOG: log right click
                    mListener.logEventTask("Experimenting", "Spin right", "-");
                    Random random = new Random();
                    int seed = random.nextInt(2000);
                    startTheSpinWithDirection("normal", 10000 + seed, false);
                }
            }
        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnTaskFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTaskFragmentInteractionListener");
        }

        if(context instanceof FinalGameActivity){
            isFinal = true;
        }
        else{
            isFinal = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * @return The selected quadrant.
     */
    protected static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    public boolean isLeftSelectBtnChecked(){
        return leftSelectBtn.isChecked();
    }

    public boolean isRightSelectBtnChecked(){
        return rightSelectBtn.isChecked();
    }

    /**
     * Start the rotation depending on the velocity and direction
     * @param direction - String  of normal or inverse wheel direction
     * @param velocity - Rotation velocity
     */
    protected void startTheSpinWithDirection(String direction, float velocity, boolean isLeft) {

        try {
            
            Log.e("DEBUG", "Rotation starts");

            int direct = 1;

            if (direction.equals("inversed")) {
                direct = -1;
            }

//                //if the design of tablet requires more then one orientation
//                //set the current orientation as constant. The configuration change will reset the wheel
//                if (isTablet()) {
//                    setOrientationConstant();
//                }

            //start the runnable process
            if (isLeft) {
                leftSpinBtn.setEnabled(false);
                flingRunnableLeft = new FlingRunnable(direct * velocity, true);
                leftWheel.post(flingRunnableLeft);
            } else {
                rightSpinBtn.setEnabled(false);
                flingRunnableRight = new FlingRunnable(direct * velocity, false);
                rightWheel.post(flingRunnableRight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The angle of the unit circle with the image view's center
     */
    protected double getAngle(double xTouch, double yTouch, boolean isLeft) {
        int width = isLeft ? leftWidth : rightWidth;
        int height = isLeft ? leftHeight : rightHeight;
        double x = xTouch - (width / 2d);
        double y = height - yTouch - (height / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }

    /**
     * Rotate the dialer.
     *
     * @param degrees The degrees, the dialer should get rotated.
     */
    protected void rotateDialer(float degrees, boolean isLeft) {
        int width = isLeft ? leftWidth : rightWidth;
        int height = isLeft ? leftHeight : rightHeight;

        if(isLeft){
            matrixLeft.postRotate(degrees, width / 2, height / 2);
        }
        else{
            matrixRight.postRotate(degrees, width / 2, height / 2);
        }

        if (isLeft && allowRotatingLeft) {
            leftWheel.setEnabled(false);
        }
        else if(!isLeft && allowRotatingRight){
            rightWheel.setEnabled(false);
        }

        if(isLeft){
            wLeft.getRewardFromWheelAngle(matrixLeft);
            leftWheel.setImageMatrix(matrixLeft);
        }
        else{
            wRight.getRewardFromWheelAngle(matrixRight);
            rightWheel.setImageMatrix(matrixRight);
        }
    }

    @SuppressLint("NewApi")
    /**
     * Set the current orientation to be constant
     */
    public void setOrientationConstant() {

        try {

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Simple implementation of a for detecting a fling event.
     */
    protected class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        boolean isLeft;
        public MyGestureDetector(boolean isLeft){
            super();
            this.isLeft = isLeft;

        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // get the quadrant of the start and the end of the fling
            int width = isLeft ? leftWidth : rightWidth;
            int height = isLeft ? leftHeight : rightHeight;
            boolean[] scanQuadrant = isLeft ? quadrantTouchedLeft : quadrantTouchedRight;
            int q1 = getQuadrant(e1.getX() - (width / 2), height - e1.getY() - (height / 2));
            int q2 = getQuadrant(e2.getX() - (width / 2), height - e2.getY() - (height / 2));

            float velocity = velocityX + velocityY;
            int minimumVelocityForRotation = 1000;

            String direction = "normal";

            // the inversed rotations
            if ((q1 == 3 && q2 == 3)
                    || ((q1 == 2 && q2 == 3) && velocityX > 0 && velocityY > 0)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 3 && q2 == 2)
                    || ((q1 == 3 && q2 == 4))
                    || (q1 == 2 && q2 == 4 && scanQuadrant[3])
                    || (q1 == 4 && q2 == 2 && scanQuadrant[3])) {

                direction = "inversed";

            } else if (((q1 == 2 && q2 == 3) || (q1 == 2 && q2 == 2)) //correct spin
                    && velocityX < 0 && velocityY > 0) {
                velocity = -1 * (velocityX / 2) + velocityY;
                direction = "inversed";
            } else if (((q1 == 2 && q2 == 1) || (q1 == 2 && q2 == 2))
                    && velocityX > 0 && velocityY < 0) {
                velocity = velocityX + -1 * (velocityY / 2);
            } else if (((q1 == 4 && q2 == 1) || (q1 == 4 && q2 == 4)) //correct spin
                    && velocityX > 0 && velocityY < 0) {
                velocity = velocityX + -1 * (velocityY / 2);
                direction = "inversed";
            } else if (((q1 == 4 && q2 == 3) || (q1 == 4 && q2 == 4))
                    && velocityX < 0 && velocityY > 0) {
                velocity = -1 * (velocityX / 2) + velocityY;
            }
//            else if(q1 == 4 && q2 == 4 &&){
//
//            }
//            else {
//                // the normal rotation
////                dialer.post(new FlingRunnable(velocityX + velocityY));
////                startTheSpinWithDirection("normal", velocity, isLeft);
//                velocity *= -1;
//            }


            //start the rotation if the velocity is more than the value above
            if (Math.abs(velocity) >= minimumVelocityForRotation) {
                if(isLeft){
                    mListener.logEventTask("Experimenting", "Spin left", "-");
                    //LOG:LOG FLING HERE LEFT
                }
                else{
                    mListener.logEventTask("Experimenting", "Spin right", "-");
                    //LOG:LOG FLING HERE RIGHT
                }
                startTheSpinWithDirection(direction, velocity, isLeft);


            }
            return true;
        }
    }

    /**
     * Simple implementation of an for registering the dialer's touch events.
     */
    protected class MyOnTouchListener implements View.OnTouchListener {

        private double startAngle;
        boolean isLeft;
        public MyOnTouchListener(boolean isLeft){
            super();
            this.isLeft = isLeft;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean[] scanQuadrant = isLeft ? quadrantTouchedLeft : quadrantTouchedRight;
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    startAngle = getAngle(event.getX(), event.getY(), isLeft);
                    // reset the touched quadrants
                    for (int i = 0; i < scanQuadrant.length; i++) {
                        if(isLeft){
                            quadrantTouchedLeft[i] = false;
                        }
                        else{
                            quadrantTouchedRight[i] = false;
                        }
                    }

                    if(isLeft){
                        allowRotatingLeft = false;
                    }
                    else{
                        allowRotatingRight = false;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY(), isLeft);
                    rotateDialer((float) (startAngle - currentAngle), isLeft);
                    startAngle = currentAngle;
                    break;

                case MotionEvent.ACTION_UP:
                    if(isLeft){
                        allowRotatingLeft = true;
                    }
                    else{
                        allowRotatingRight = true;
                    }
                    Log.e("ASD", "Scroll stop");
                    break;
            }

            // set the touched quadrant to true
            if(isLeft){
                quadrantTouchedLeft[getQuadrant(event.getX() - (leftWidth / 2), leftHeight - event.getY() - (leftHeight / 2))] = true;
                detectorLeft.onTouchEvent(event);
            }
            else{
                quadrantTouchedRight[getQuadrant(event.getX() - (rightWidth / 2), rightHeight - event.getY() - (rightHeight / 2))] = true;
                detectorRight.onTouchEvent(event);
            }

            return true;
        }

    }

    /**
     * A {@link Runnable} for animating the the dialer's fling.
     */
    protected class FlingRunnable implements Runnable {

        private float velocity;
        private boolean isLeft;

        public FlingRunnable(float velocity, boolean isLeft) {

            this.velocity = velocity;
            this.isLeft = isLeft;
        }

        @Override
        public void run() {
            boolean allowRotating = isLeft ? allowRotatingLeft : allowRotatingRight;
            if (Math.abs(velocity) > 5 && allowRotating) {
                setOrientationConstant();

                rotateDialer(velocity / 75, isLeft);
//                velocity /= 1.0666F;
//                velocity /= 1.0700F; //fast stopping
                velocity /= 1.0100F; // slow stopping
                // post this instance again
                if(isLeft){
                    leftWheel.post(this);
                }
                else{
                    rightWheel.post(this);
                }
            }
            else {

                // Rotation ends here
                Log.e("DEBUG", "Rotation Ends ");

                if(isLeft){
                    //LOG: log score left
                    String leftScore = getContext().getResources().getString(R.string.left_score);
                    String leftScoreRecord = Integer.toString(wLeft.getRewardFromWheelAngle(matrixLeft));
                    mListener.logEventTask("Experimenting", "Left finished spinning", leftScoreRecord);
                    leftScoreView.setText(String.format(leftScore, leftScoreRecord));
                    if(!isFinal){
                        allowRotatingLeft = true;
                        leftWheel.setEnabled(true);
                        leftSpinBtn.setEnabled(true);
                    }
                }
                else{
                    //LOG: log score right
                    String rightScore = getContext().getResources().getString(R.string.right_score);
                    String rightScoreRecord = Integer.toString(wRight.getRewardFromWheelAngle(matrixRight));
                    mListener.logEventTask("Experimenting", "Right finished spinning", rightScoreRecord);
                    rightScoreView.setText(String.format(rightScore, rightScoreRecord));
                    if(!isFinal){
                        allowRotatingRight = true;
                        rightWheel.setEnabled(true);
                        rightSpinBtn.setEnabled(true);
                    }
                }

            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        leftWheel.removeCallbacks(flingRunnableLeft);
        leftWheel.removeCallbacks(flingRunnableRight);
    }

    @Override
    public void onResume(){
        super.onResume();
        leftWheel.setOnTouchListener(new MyOnTouchListener(true));
        leftWheel.setEnabled(true);
        leftSpinBtn.setEnabled(true);
        rightWheel.setOnTouchListener(new MyOnTouchListener(false));
        rightWheel.setEnabled(true);
        rightSpinBtn.setEnabled(true);
        mListener.fullScreen();
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
    public interface OnTaskFragmentInteractionListener {
        public Wheel getWheelFromList(boolean isLeft);
        public void nextScreen(boolean isLeftSelected);
        public void fullScreen();
        public void logEventTask(String action, String result, String outcome);
        public void showConfirmation();
    }

}

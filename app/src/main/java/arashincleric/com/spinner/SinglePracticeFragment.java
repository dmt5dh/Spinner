package arashincleric.com.spinner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Random;


public class SinglePracticeFragment extends Fragment {

    public OnSinglePracticeFragmentInteractionListener mListener;

    private static Bitmap imageOriginal, imageScaled;
    private static Matrix matrix;

    private ImageView wheelView;
    private int height, width;

    private GestureDetector detector;

    // needed for detecting the inversed rotations
    private boolean[] quadrantTouched;

    private boolean allowRotating;

    private Button spinBtn;

    WheelView wViewObj;

    Button continueBtn;

    TextView scoreView;

    Runnable flingRunnable;

    public static SinglePracticeFragment newInstance() {
        SinglePracticeFragment fragment = new SinglePracticeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SinglePracticeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            wViewObj = new WheelView(getContext(), mListener.getWheelFromListSingle());
        }

        //New instance so we want to redraw every time
        imageOriginal = null;
        imageScaled = null;
        matrix = null;

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_practice, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // load the image only once
        if (imageOriginal == null) {
//            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.circletest);
            imageOriginal = wViewObj.getBitmap();
        }

        // initialize the left matrix only once
        if (matrix == null) {
            matrix = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            matrix.reset();
        }

        detector = new GestureDetector(getContext(), new MyGestureDetector());

        // there is no 0th quadrant, to keep it simple the first value gets ignored
        quadrantTouched = new boolean[] { false, false, false, false, false };

        allowRotating = true;

        wheelView = (ImageView) view.findViewById(R.id.wheel);
        ViewGroup.LayoutParams wheelParams = wheelView.getLayoutParams();
        wheelParams.width = wheelParams.height;
        wheelView.setOnTouchListener(new MyOnTouchListener());
        wheelView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time
                if (height == 0 || width == 0) {
                    height = wheelView.getHeight();
                    width = wheelView.getWidth();

                    // resize
                    Matrix resize = new Matrix();
                    resize.postScale((float) Math.min(width, height) / (float) imageOriginal.getWidth(), (float) Math.min(width, height) / (float) imageOriginal.getHeight());
                    imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(), imageOriginal.getHeight(), resize, false);

                    // translate to the image view's center
                    float translateX = width / 2 - imageScaled.getWidth() / 2;
                    float translateY = height / 2 - imageScaled.getHeight() / 2;
                    matrix.postTranslate(translateX, translateY);

                    wheelView.setImageBitmap(imageScaled);
                    wheelView.setImageMatrix(matrix);
                }
            }
        });

        continueBtn = (Button) view.findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOG: click continue btn
                mListener.logEventSingle("Clicked continue", "Practice single resolution", "-");
                new AlertDialog.Builder(v.getContext())
                        .setMessage(R.string.confirm_wheel_msg_practice)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mListener.fullScreenSingle();
                            }
                        })
                        .setPositiveButton(R.string.yes_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //LOG: confirm continue btn
                                mListener.logEventSingle("Confirmed continue", "Practice single resolution", "-");
                                mListener.nextScreenSingle();
                            }
                        })
                        .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //LOG: cancel continue btn
                                mListener.logEventSingle("Canceled continue", "Practice single resolution", "-");
                            }
                        })
                        .show();
            }
        });

        scoreView = (TextView)view.findViewById(R.id.scoreView);
        String score = getContext().getResources().getString(R.string.left_score);
        scoreView.setText(String.format(score, "-"));

        spinBtn = (Button)view.findViewById(R.id.spinBtn);
        spinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allowRotating){
                    //LOG: spin button pressed
                    mListener.logEventSingle("Experimenting", "Spinned single", "-");
                    Random random = new Random();
                    int seed = random.nextInt(2000);
                    startTheSpinWithDirection("normal", 10000 + seed);
                }

            }
        });

    }

    /**
     * Simple implementation of a for detecting a fling event.
     */
    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        public MyGestureDetector(){
            super();

        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // get the quadrant of the start and the end of the fling
            boolean[] scanQuadrant = quadrantTouched;
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

            //start the rotation if the velocity is more than the value above
            if (Math.abs(velocity) >= minimumVelocityForRotation) {
                //LOG: spinner flung
                mListener.logEventSingle("Experimenting", "Spinned single", "-");
                startTheSpinWithDirection(direction, velocity);


            }
            return true;
        }
    }

    /**
     * Simple implementation of an for registering the dialer's touch events.
     */
    private class MyOnTouchListener implements View.OnTouchListener {

        private double startAngle;
        public MyOnTouchListener(){
            super();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean[] scanQuadrant = quadrantTouched;
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    startAngle = getAngle(event.getX(), event.getY());
                    // reset the touched quadrants
                    for (int i = 0; i < scanQuadrant.length; i++) {
                        quadrantTouched[i] = false;
                    }

                    allowRotating = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    rotateDialer((float) (startAngle - currentAngle));
                    startAngle = currentAngle;
                    break;

                case MotionEvent.ACTION_UP:
                    allowRotating = true;
                    Log.e("ASD", "Scroll stop");
                    break;
            }

            // set the touched quadrant to true
            quadrantTouched[getQuadrant(event.getX() - (width / 2), height - event.getY() - (height / 2))] = true;
            detector.onTouchEvent(event);

            return true;
        }

    }

    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    /**
     * Start the rotation depending on the velocity and direction
     * @param direction - String  of normal or inverse wheel direction
     * @param velocity - Rotation velocity
     */
    private void startTheSpinWithDirection(String direction, float velocity) {

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

            spinBtn.setEnabled(false);
            //start the runnable process
            flingRunnable = new FlingRunnable(direct * velocity);
            wheelView.post(flingRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The angle of the unit circle with the image view's center
     */
    private double getAngle(double xTouch, double yTouch) {
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
    private void rotateDialer(float degrees) {

        matrix.postRotate(degrees, width / 2, height / 2);

        if (allowRotating) {
            wheelView.setEnabled(false);
        }

        wViewObj.getRewardFromWheelAngle(matrix);
        wheelView.setImageMatrix(matrix);
    }

    /**
     * A {@link Runnable} for animating the the dialer's fling.
     */
    private class FlingRunnable implements Runnable {

        private float velocity;

        public FlingRunnable(float velocity) {

            this.velocity = velocity;
        }

        @Override
        public void run() {
            if (Math.abs(velocity) > 5 && allowRotating) {
                setOrientationConstant();

                rotateDialer(velocity / 75);
//                velocity /= 1.0666F;
//                velocity /= 1.0700F; //fast stopping
                velocity /= 1.0100F; // slow stopping
                // post this instance again
                wheelView.post(this);
            }
            else {

                // Rotation ends here
                Log.e("DEBUG", "Rotation Ends ");
                String score = getContext().getResources().getString(R.string.left_score);
                String scoreRecorded = Integer.toString(wViewObj.getRewardFromWheelAngle(matrix));
                //LOG: score
                mListener.logEventSingle("Experimenting", "Single spinner finished spinning", scoreRecorded);
                scoreView.setText(String.format(score, scoreRecorded));
                allowRotating = true;
                wheelView.setEnabled(true);
                spinBtn.setEnabled(true);
            }
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnSinglePracticeFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSinglePracticeFragmentInteractionListener");
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        wheelView.removeCallbacks(flingRunnable);
    }

    @Override
    public void onResume(){
        super.onResume();
        wheelView.setOnTouchListener(new MyOnTouchListener());
        wheelView.setEnabled(true);
        spinBtn.setEnabled(true);
        mListener.fullScreenSingle();
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
    public interface OnSinglePracticeFragmentInteractionListener {
        public Wheel getWheelFromListSingle();
        public void nextScreenSingle();
        public void fullScreenSingle();
        public void logEventSingle(String action, String result, String outcome);

    }

}

package arashincleric.com.spinner;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class WheelListActivity extends ListActivity {

    private WheelListAdapter adapter;
    private ArrayList<WheelTuple> wheelArrayList;

    public class WheelTuple{
        public Wheel left;
        public Wheel right;
        public WheelTuple(Wheel left, Wheel right){
            this.left = left;
            this.right = right;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ArrayList<Wheel> tempList = getIntent().getParcelableArrayListExtra("WHEELLIST");
        wheelArrayList = new ArrayList<WheelTuple>();
        for(int i = 0; i < tempList.size(); i = i + 2){
            Wheel tmpLeft = tempList.get(i);
            Wheel tmpRight = tempList.get(i + 1);
            wheelArrayList.add(new WheelTuple(tmpLeft, tmpRight));
        }

        adapter = new WheelListAdapter(this, wheelArrayList);
        getListView().setAdapter(adapter);

        boolean b = getListView().hasFocusable();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage(R.string.confirm_list_sel)
                        .setNegativeButton(R.string.cancel_btn, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                turnFullScreen();
                            }
                        })
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: log selection here
                                startFinalScreen(wheelArrayList.get(position));
                            }
                        })
                        .show();
            }
        });
        setupUI(findViewById(android.R.id.content));
        turnFullScreen();
    }

    @Override
    public void onBackPressed(){

    }

    public void startFinalScreen(WheelTuple w){
        //TODO: start final screen
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

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    turnFullScreen();
                    return false;
                }

            });

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }
}

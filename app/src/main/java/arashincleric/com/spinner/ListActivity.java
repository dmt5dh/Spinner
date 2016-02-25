package arashincleric.com.spinner;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

public class ListActivity extends android.app.ListActivity {

    private WheelListAdapter adapter;
    private ArrayList<Wheel> wheelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ArrayList<Wheel> tempList = getIntent().getParcelableArrayListExtra("WHEELLIST");
        wheelArrayList = new ArrayList<Wheel>();
        for(int i = 0; i < tempList.size(); i++){
            Wheel tmp = tempList.get(i);
            wheelArrayList.add(new Wheel(this, tmp.getValue_degree(), tmp.getScores(), tmp.getCOLORS()));
        }

        adapter = new WheelListAdapter(this, wheelArrayList);
        getListView().setAdapter(adapter);

    }
}

package arashincleric.com.spinner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Dan on 2/24/2016.
 */
public class WheelListAdapter  extends BaseAdapter implements ListAdapter{

    private ArrayList<WheelListActivity.WheelTuple> list = new ArrayList<WheelListActivity.WheelTuple>();
    private Context context;

    public WheelListAdapter(Context context, ArrayList<WheelListActivity.WheelTuple> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount(){
        return list.size();
    }

    @Override
    public Object getItem(int pos){
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos){
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        View view = convertView;
        if(view == null){ //If view not inflated, inflate it
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.wheel_list_item, null);
        }

        TextView decisionNum = (TextView)view.findViewById(R.id.decisionNumText);
        decisionNum.setText(Integer.toString(position + 1));

        ImageView leftWheel = (ImageView)view.findViewById(R.id.leftWheel);
        ImageView rightWheel = (ImageView)view.findViewById(R.id.rightWheel);

        CheckBox leftCheckBox = (CheckBox)view.findViewById(R.id.leftCheckBox);
        leftCheckBox.setClickable(false);
        CheckBox rightCheckBox = (CheckBox)view.findViewById(R.id.rightCheckBox);
        rightCheckBox.setClickable(false);

        WheelListActivity.WheelTuple wheelTuple = list.get(position);

        leftWheel.setImageBitmap(new WheelView(context, wheelTuple.left).getBitmapReduced());
        rightWheel.setImageBitmap(new WheelView(context, wheelTuple.right).getBitmapReduced());

        int id = context.getResources().getIdentifier("GREY", "color", context.getPackageName());
        int colorId=context.getResources().getColor(id);

        if(!wheelTuple.left.isChosen()){
            leftWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY); //TODO:TWEAK THIS TO DIM(Multiply)
            rightCheckBox.setChecked(true);
        }
        else{
            rightWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY); //TODO:TWEAK THIS TO DIM(Multiply)
            leftCheckBox.setChecked(true);
        }

        return view;
    }
}

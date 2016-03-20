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

    private ViewHolder viewHolder;

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
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.wheel_list_item, null);

            viewHolder.decisionNum = (TextView)view.findViewById(R.id.decisionNumText);
            viewHolder.leftWheel = (ImageView)view.findViewById(R.id.leftWheel);
            viewHolder.rightWheel = (ImageView)view.findViewById(R.id.rightWheel);
            viewHolder.leftCheckBox = (CheckBox)view.findViewById(R.id.leftCheckBox);
            viewHolder.rightCheckBox = (CheckBox)view.findViewById(R.id.rightCheckBox);
            viewHolder.wheelTuple = list.get(position);
            view.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.decisionNum.setText(Integer.toString(position + 1));

        viewHolder.leftCheckBox.setClickable(false);
        viewHolder.rightCheckBox.setClickable(false);

        viewHolder.leftWheel.setImageBitmap(new WheelView(context, viewHolder.wheelTuple.left).getBitmapReduced());
        viewHolder.rightWheel.setImageBitmap(new WheelView(context, viewHolder.wheelTuple.right).getBitmapReduced());

        int id = context.getResources().getIdentifier("GREY", "color", context.getPackageName());
        int colorId=context.getResources().getColor(id);

        if(!viewHolder.wheelTuple.left.isChosen()){
            viewHolder.leftWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY); //TODO:TWEAK THIS TO DIM(Multiply)
            viewHolder.rightCheckBox.setChecked(true);
        }
        else{
            viewHolder.rightWheel.setColorFilter(colorId, PorterDuff.Mode.MULTIPLY); //TODO:TWEAK THIS TO DIM(Multiply)
            viewHolder.leftCheckBox.setChecked(true);
        }

        return view;
    }

    @Override
    public int getViewTypeCount(){
        if(list.size() == 0){
            return 1;
        }
        else{
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    static class ViewHolder{
        TextView decisionNum;
        ImageView leftWheel;
        ImageView rightWheel;
        CheckBox leftCheckBox;
        CheckBox rightCheckBox;
        WheelListActivity.WheelTuple wheelTuple;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}

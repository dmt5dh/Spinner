package arashincleric.com.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.util.ArrayList;

/**
 * Created by Dan on 2/24/2016.
 */
public class WheelListAdapter  extends BaseAdapter implements ListAdapter{

    private ArrayList<Wheel> list = new ArrayList<Wheel>();
    private Context context;

    public WheelListAdapter(Context context, ArrayList<Wheel> list){
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
        if(position % 2 != 0 ){
            return null;
        }
        View view = convertView;
        if(view == null){ //If view not inflated, inflate it
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.wheel_list_item, null);
        }

        ImageView leftWheel = (ImageView)view.findViewById(R.id.leftWheel);
        ImageView rightWheel = (ImageView)view.findViewById(R.id.rightWheel);

        leftWheel.setImageBitmap(list.get(position).getBitmap());
        rightWheel.setImageBitmap(list.get(position + 1).getBitmap());

        return view;
    }
}

package arashincleric.com.spinner;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dan on 4/12/2016.
 */
public class WheelTuple implements Parcelable{
    public Wheel left;
    public Wheel right;
    public WheelTuple(Wheel left, Wheel right){
        this.left = left;
        this.right = right;
    }

    public static final Parcelable.Creator<WheelTuple> CREATOR = new Parcelable.Creator<WheelTuple>() {
        @Override
        public WheelTuple createFromParcel(Parcel source) {
            Wheel lWheel = new Wheel();
            lWheel.value_degree = source.createFloatArray();
            lWheel.scores = source.createIntArray();
            lWheel.COLORS = source.createIntArray();
            lWheel.isChosen = source.createBooleanArray()[0];

            Wheel rWheel = new Wheel();
            rWheel.value_degree = source.createFloatArray();
            rWheel.scores = source.createIntArray();
            rWheel.COLORS = source.createIntArray();
            rWheel.isChosen = source.createBooleanArray()[0];


            return new WheelTuple(lWheel, rWheel);
        }

        @Override
        public WheelTuple[] newArray(int size) {
            return new WheelTuple[0];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags){
        parcel.writeFloatArray(left.value_degree);
        parcel.writeIntArray(left.scores);
        parcel.writeIntArray(left.COLORS);
        parcel.writeBooleanArray(new boolean[]{left.isChosen});

        parcel.writeFloatArray(right.value_degree);
        parcel.writeIntArray(right.scores);
        parcel.writeIntArray(right.COLORS);
        parcel.writeBooleanArray(new boolean[]{right.isChosen});
    }
}

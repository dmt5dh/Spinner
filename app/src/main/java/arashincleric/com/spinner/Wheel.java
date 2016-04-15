package arashincleric.com.spinner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;


public class Wheel implements Parcelable{

    private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public float[] value_degree;
    public int[] scores;
    public int[] COLORS;
    private String[] colorNames;
    RectF rectf = new RectF (0, 0, 2000, 2000); //change this and below for better resolution
    float temp=0;
    public boolean isChosen;
    Context context;

    public Wheel(){
    }

    public Wheel(float[] sections, int[] scores, int[] colors) {
        value_degree=new float[sections.length];
        for(int i=0;i<sections.length;i++)
        {
            value_degree[i]=sections[i];
        }

        COLORS = new int[colors.length];
        for(int i=0;i<colors.length;i++)
        {
            COLORS[i]=colors[i];
        }

        this.scores = scores;

        isChosen = false;
    }

    public Wheel(Context context, float[] sections, int[] scores, String[] colorNames) {
        this.context = context;

        value_degree=new float[sections.length];
        for(int i=0;i<sections.length;i++)
        {
            value_degree[i]=sections[i];
        }

        this.colorNames = colorNames;

        COLORS = new int[colorNames.length];
        for(int i=0;i<colorNames.length;i++)
        {
            int id = context.getResources().getIdentifier(colorNames[i], "color", context.getPackageName());
            COLORS[i]=context.getResources().getColor(id);
        }

        this.scores = scores;

        isChosen = false;
    }

    public float[] getValue_degree(){
        return value_degree;
    }

    public int getSiezeValue_degree(){
        return value_degree.length;
    }

    public int[] getScores(){
        return scores;
    }

    public int[] getCOLORS(){
        return COLORS;
    }

    public String[] getColorNames(){
        return colorNames;
    }

    public void setChosen(boolean b){
        this.isChosen = b;
    }

    public boolean isChosen(){
        return this.isChosen;
    }

    public static final Parcelable.Creator<Wheel> CREATOR = new Creator<Wheel>() {
        @Override
        public Wheel createFromParcel(Parcel source) {
            Wheel wheel = new Wheel();
            wheel.value_degree = source.createFloatArray();
            wheel.scores = source.createIntArray();
            wheel.COLORS = source.createIntArray();
            wheel.isChosen = source.createBooleanArray()[0];
            return wheel;
        }

        @Override
        public Wheel[] newArray(int size) {
            return new Wheel[0];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags){
        parcel.writeFloatArray(value_degree);
        parcel.writeIntArray(scores);
        parcel.writeIntArray(COLORS);
        parcel.writeBooleanArray(new boolean[]{isChosen});
    }
}

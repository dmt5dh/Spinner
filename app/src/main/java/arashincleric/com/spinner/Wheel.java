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


public class Wheel extends ImageView implements Parcelable{

    private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] value_degree;
    private int[] scores;
    private int[] COLORS;
    RectF rectf = new RectF (0, 0, 2000, 2000); //change this and below for better resolution
    float temp=0;

    public Wheel(Context context){
        super(context);
    }

    public Wheel(Context context, float[] sections, int[] scores, int[] colors) {
        super(context);
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
    }

    public float[] getValue_degree(){
        return value_degree;
    }

    public int[] getScores(){
        return scores;
    }

    public int[] getCOLORS(){
        return COLORS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        int centerX = (int)(rectf.left + rectf.right) / 2;
        int centerY = (int)(rectf.top + rectf.bottom) / 2;
        int radius = (int)(rectf.right - rectf.left) / 2;
        radius *= 0.5; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.

        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < value_degree.length; i++) {//values2.length; i++) {
            if (i == 0) {
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, 0, value_degree[i], true, paint);
//                paint.setColor(Color.BLACK);
//                float medianAngle = (temp + (value_degree[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
//                canvas.drawText(Integer.toString(scores[i]), (float)(centerX + (radius * Math.cos(medianAngle))), (float)(centerY + (radius * Math.sin(medianAngle))), paint);
            }
            else
            {
                temp += value_degree[i - 1];
                paint.setColor(COLORS[i]);
                canvas.drawArc(rectf, temp, value_degree[i], true, paint);
//                paint.setColor(Color.BLACK);
//                float medianAngle = (temp + (value_degree[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
//                canvas.drawText("Text", (float) (centerX + (radius * Math.cos(medianAngle))), (float) (centerY + (radius * Math.sin(medianAngle))), paint);
            }
            paint.setColor(Color.BLACK);
            float medianAngle = (temp + (value_degree[i] / 2f)) * (float)Math.PI / 180f; // this angle will place the text in the center of the arc.
            paint.setTextSize(100f);
            canvas.drawText(Integer.toString(scores[i]), (float)(centerX + (radius * Math.cos(medianAngle))), (float)(centerY + (radius * Math.sin(medianAngle))), paint);
        }
    }

    public int getRewardFromWheelAngle(Matrix matrix) {
        /**
         * Get the matrix angle URL: http://stackoverflow.com/a/28307921/3248003
         */
        float[] v = new float[9];

        matrix.getValues(v);

        // calculate the degree of rotation
        float rAngle = Math.round(Math.atan2(v[Matrix.MSKEW_X],
                v[Matrix.MSCALE_X]) * (180 / Math.PI));
        /**
         * Convert 0-180 and -180-0 degrees to 0-360 URL:
         * http://stackoverflow.com/a/25725005/3248003
         */
        rAngle = ((rAngle + 360 - 90) % 360); //Minus 90 because ticker is on top not the right side

        return getReward(rAngle);
    }

    private int getReward(float angle) {

        int position = 0;

        // default reward
        int reward = 0;

        //Calculate the location of the wheel to the nearest degree
        int numberOfSectors = 360;
        int circle = 360;
        float degreePerSector = (float) circle/numberOfSectors;

        //TODO: at position 0 it is 0!!!
        //Then we can get the position of our price
        position = (int) Math.floor( angle / degreePerSector );
        float refPoint1 = 360; //get area start
        for(int i = value_degree.length - 1; i >= 0; i--){
            float refPoint2 = refPoint1 - value_degree[i]; //get the next area end
            if(position < refPoint1 && position >= refPoint2){ //if within return the score.
                Log.e("DEBUG", " Position is " + position + " Reward is " + scores[i] + " " + degreePerSector);
                return scores[i];
            }
            refPoint1 = refPoint2; //update to next area start
        }

        Log.e("DEBUG", " Position is " + position + " Reward is " + reward + " " + degreePerSector);

        return -1; //in a perfect world this should never hit
    }

    public Bitmap getBitmap(){
        Bitmap b = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888); //Change to same value above
        Canvas c = new Canvas(b);
        this.layout(0, 0, 2000, 2000); //Change to same value above
        this.draw(c);
        return b;
    }

    public static final Parcelable.Creator<Wheel> CREATOR = new Creator<Wheel>() {
        @Override
        public Wheel createFromParcel(Parcel source) {
            Wheel wheel = new Wheel(null);
            wheel.value_degree = source.createFloatArray();
            wheel.scores = source.createIntArray();
            wheel.COLORS = source.createIntArray();
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
    }
}

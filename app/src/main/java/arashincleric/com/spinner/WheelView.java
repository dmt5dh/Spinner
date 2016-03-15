package arashincleric.com.spinner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;


public class WheelView extends ImageView {

    private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] value_degree;
    private int[] scores;
    private int[] COLORS;
    RectF rectf;
    private float FONT_SIZE;
    float temp=0;
    private Wheel wheelObject;

    public WheelView(Context context){
        super(context);
    }

    public WheelView(Context context, Wheel w) {
        super(context);

        wheelObject = w;

        float[] wValueDegree = w.getValue_degree();
        value_degree = new float[wValueDegree.length];
        System.arraycopy(wValueDegree, 0, value_degree, 0, wValueDegree.length);

        int[] wColors = w.getCOLORS();
        COLORS = new int[wColors.length];
        System.arraycopy(wColors, 0, COLORS, 0, wColors.length);

        int[] wScores = w.getScores();
        scores = new int[wScores.length];
        System.arraycopy(wScores, 0, scores, 0, wScores.length);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = (int)(rectf.left + rectf.right) / 2;
        int centerY = (int)(rectf.top + rectf.bottom) / 2;
        int radius = (int)(rectf.right - rectf.left) / 2;
        radius *= 0.6; // 1 will put the text in the border, 0 will put the text in the center. Play with this to set the distance of your text.

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
            paint.setTextSize(FONT_SIZE);
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
        rectf = new RectF (0, 0, 2000, 2000); //change this and below for better resolution
        FONT_SIZE = 200f;
        this.draw(c);
        return b;
    }

    public Bitmap getBitmapReduced(){ // do this for listView because too much memory otherwise
        Bitmap b = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888); //Change to same value above
        Canvas c = new Canvas(b);
        this.layout(0, 0, 300, 300); //Change to same value above
        rectf = new RectF (0, 0, 300, 300); //change this and below for better resolution
        FONT_SIZE = 25f;
        this.draw(c);
        return b;
    }

    public Wheel getWheelObject(){
        return wheelObject;
    }
}

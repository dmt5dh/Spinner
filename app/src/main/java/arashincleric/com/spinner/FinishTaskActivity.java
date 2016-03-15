package arashincleric.com.spinner;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class FinishTaskActivity extends AbstractTaskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_task);

        turnFullScreen();
        super.setupUI(findViewById(android.R.id.content));

        int score = getIntent().getIntExtra("SCORE", 0);
        TextView completeMsg = (TextView)findViewById(R.id.completeMsg);
        String msg = String.format(getResources().getString(R.string.complete_msg), score);
        completeMsg.setText(msg);

        Button finishBtn = (Button)findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent mStartActivity = new Intent(FinishTaskActivity.this, MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(FinishTaskActivity.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) FinishTaskActivity.this.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, mPendingIntent);
                        System.exit(0);
                    }
                });
            }
        });
    }
}

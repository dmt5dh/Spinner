package arashincleric.com.spinner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class QuestionnaireActivity extends AbstractTaskActivity {

    ArrayList<Question> questionList;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        super.userID = getIntent().getStringExtra("USERNAME");

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);

        context = this;

        questionList = initializeQuestionList();

        for(int i = 0; i < questionList.size(); i++){ //Go through each question and generate the view
            Question q = questionList.get(i);
            View child;
            if(q instanceof QuestionRadioButtons){ //Radio buttons questions
                ArrayList<String> selections = ((QuestionRadioButtons)q).getSelections();
                if(((QuestionRadioButtons)q).isCheckBox()){
                    child = getLayoutInflater().inflate(R.layout.question_checkbox, null);
                    LinearLayout linearLayout = (LinearLayout)child.findViewById(R.id.checkBoxAnswers);
                    for(int j = 0; j < ((QuestionRadioButtons)q).getNumSelections(); j++){
                        CheckBox checkBox = new CheckBox(this);
                        checkBox.setText(selections.get(j));
                        checkBox.setTextSize(20);
                        linearLayout.addView(checkBox);
                    }
                }
                else{
                    child = getLayoutInflater().inflate(R.layout.question_radio, null);
                    RadioGroup radioGroup = (RadioGroup)child.findViewById(R.id.radioAnswers);
                    for(int j = 0; j < ((QuestionRadioButtons)q).getNumSelections(); j++){ //Set all radio buttons
                        RadioButton radioButton = new RadioButton(this);
                        radioButton.setText(selections.get(j));
                        radioButton.setTextSize(20);
                        radioGroup.addView(radioButton);
                    }
                }
                TextView questionView = (TextView)child.findViewById(R.id.question);
                questionView.setText(q.getQuestion());
            }
            else{
                child = getLayoutInflater().inflate(R.layout.question_free_response, null);
                TextView questionView = (TextView)child.findViewById(R.id.question);
                questionView.setText(q.getQuestion());

                EditText answerText = (EditText)child.findViewById(R.id.answer);
                answerText.setHint(R.string.answer_hint);
            }
            child.setTag(q); //Save the object to this view to retrieve data later
            child.setPadding(0,0,0,50);
            layout.addView(child);
        }

        Button submitButton = new Button(this); //Add submit button at the end
        submitButton.setText(R.string.submit_btn);
        submitButton.setTextSize(35);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean success = createQuestionnaireFile();

                if (success) {
                    Intent intent = new Intent(QuestionnaireActivity.this, FinishTaskActivity.class);
                    intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
                    intent.putExtra("SCORE", getIntent().getIntExtra("SCORE", 0));
                    startActivity(intent);
                    finish();
                }
            }
        });
        layout.addView(submitButton);

        View viewHolder = new View(this); //To give some space on the bottom
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 25);
        viewHolder.setLayoutParams(params);
        layout.addView(viewHolder);

        setupUI(findViewById(R.id.scrollView));

        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.requestFocus(); //Start at the top of the scrollView
    }

    /**
     * Save the questionnaire answers to file
     * @return true if successful
     */
    private boolean createQuestionnaireFile(){
        //Check external storage available
        if(!isExternalStorageWritable()){
            Toast.makeText(this, "External Storage not writable. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        int currentAPIVersion = Build.VERSION.SDK_INT;
        File root;
        if(currentAPIVersion >= Build.VERSION_CODES.KITKAT){ //Get API version because external storage is different depending
            File[] dirs = ContextCompat.getExternalFilesDirs(this, null);
            root = dirs[0];
        }
        else{
            root = Environment.getExternalStorageDirectory();
        }
        super.fileRoot = new File(root + "/spinnerData");
        //Check if file directory exists. If not, create it and check if it was created.
        File filePath = new File(super.fileRoot + "/QuestionnaireAnswers");
        if(!filePath.exists()){
            boolean makeDir = filePath.mkdirs(); //Can't use this to check because it is false for error and dir exists
        }
        if(!filePath.exists() || !filePath.isDirectory()){
            Toast.makeText(this, "Error with creating directory. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        String answersFileName = "Answers.txt";
        File answersFile = new File(filePath, answersFileName);
        if(!answersFile.exists()){ //Make the file if it doesnt exist
            try{
                FileOutputStream answerFileStream = new FileOutputStream(answersFile);
                String columns = "";
                String userInfoColumns = "Username\t Date\t Time\t";
                answerFileStream.write(userInfoColumns.getBytes());

                for(int i = 0; i < questionList.size(); i++){ //Go through each view and retrieve question
                    columns = columns + questionList.get(i).getQuestion() + "\t";
                }
                columns = columns + "\n";
                answerFileStream.write(columns.getBytes());
                answerFileStream.close();
            } catch (Exception e){
                Toast.makeText(this, "Error creating event file. Exiting...", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        Date now = Calendar.getInstance().getTime();
        String date = dateFormat.format(now) + "\t";
        String time = timeFormat.format(now) + "\t";

        String data = super.userID + "\t" + date + time;
        for(int i = 0; i < layout.getChildCount() - 2; i++){ //Go to 2nd to last because last child is the button
            RadioGroup radioGroup = (RadioGroup)layout.getChildAt(i).findViewById(R.id.radioAnswers);
            LinearLayout checkGroup = (LinearLayout)layout.getChildAt(i).findViewById(R.id.checkBoxAnswers);
            if(radioGroup != null){
                int radioId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton)radioGroup.findViewById(radioId);
                if(radioButton == null){ //Force user to choose something
                    alertFillFields();
                    return false;
                }
                data = data + radioButton.getText() + "\t";
            }
            else if(checkGroup != null){
                String selections = "";
                for(int j = 0; j < checkGroup.getChildCount(); j++){
                    CheckBox checkBox = (CheckBox)checkGroup.getChildAt(j);
                    if(checkBox.isChecked() && checkBox.getText().toString().toLowerCase().contains("other")){
                        TextView otherText = (TextView)layout.getChildAt(i).findViewById(R.id.otherEditText);
                        selections = selections + "Other: " +  otherText.getText().toString() + ",";
                    }
                    else if(checkBox.isChecked()){
                        selections = selections + checkBox.getText().toString() + ",";
                    }
                }
                data = data + selections + "\t";
            }
            else{
                EditText editText = (EditText)layout.getChildAt(i).findViewById(R.id.answer);
                if(editText.getText().toString().isEmpty()){ //For user to type something
                    alertFillFields();
                    return false;
                }
                data = data + editText.getText().toString() + "\t";
            }
        }

        data = data + "\n";

        try{
            FileOutputStream answerFileStream = new FileOutputStream(answersFile, true);
            answerFileStream.write(data.getBytes());
            answerFileStream.close();
        }catch (Exception e){
            Toast.makeText(this, "Error saving answers. Exiting...", Toast.LENGTH_LONG).show();
            finish();
        }

        return true;
    }

    /**
     * Hide soft keyboard
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(this.getCurrentFocus().getWindowToken() != null){
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Put a listener on every view to hide softkeyboard if edittext not chosen
     * @param view The view set up
     */
    @Override
    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    turnFullScreen();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    /**
     * Generate alert to tell the user to fill out all fields
     */
    public void alertFillFields(){
        new AlertDialog.Builder(this)
                .setMessage(R.string.questionnaire_alert)
                .setNegativeButton(R.string.cancel_btn, null)
                .setCancelable(false)
                .show();
    }

    public ArrayList<Question> initializeQuestionList(){
        //Read wheel data from JSON
        InputStream is;
            is = getResources().openRawResource(R.raw.questions);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try{
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1){
                writer.write(buffer, 0, n);
            }
            is.close();
        }
        catch (IOException e){
            Log.e("ERROR", "Error loading wheels");
        }

        ArrayList<Question> questionList = new ArrayList<Question>();
        String json = writer.toString();
        try{
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonQuestion = jsonArray.getJSONObject(i).getJSONObject("questionObject");
                String questionText = jsonQuestion.getString("question");
                String type = jsonQuestion.getString("type");
                Question question;
                if(type.equals("radio") || type.equals("checkbox")){
                    JSONArray answerJsonArray = jsonQuestion.getJSONArray("answer");
                    String[] answerList = new String[answerJsonArray.length()];
                    for(int j = 0; j < answerList.length; j++){
                        answerList[j] = answerJsonArray.getString(j);
                    }
                    question = new QuestionRadioButtons(questionText,
                            answerJsonArray.length(),
                            answerList,
                            type.equals("checkbox"));
                }
                else{
                    question = new Question(questionText);
                }
                questionList.add(question);
            }
        }
        catch (JSONException e){
            Log.e("ERROR", "Error loading wheels...json");
        }

        return questionList;
    }
}

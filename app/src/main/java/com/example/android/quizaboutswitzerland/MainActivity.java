package com.example.android.quizaboutswitzerland;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Declare the shared preference file creation mode
    private static final int PREFERENCE_MODE_PRIVATE = 0;

    // Declare some program constants
    private static final int NUMBER_OF_QUESTIONS = 10;
    private static final int NUMBER_OF_MULTIPLE_CHOICE_QUESTIONS = 7;
    private static final int NUMBER_OF_ANSWERS_FOR_QUESTIONS_2 = 5;
    private static final int NUMBER_OF_ANSWERS_FOR_QUESTIONS_5 = 6;
    private static final int THIRD_QUESTION_INDEX = 0;
    private static final int FOURTH_QUESTION_INDEX = 1;
    private static final int FIFTH_QUESTION_INDEX = 2;
    private static final int SEVENTH_QUESTION_INDEX = 3;
    private static final int EIGHTH_QUESTION_INDEX = 4;
    private static final int NINTH_QUESTION_INDEX = 5;
    private static final int TENTH_QUESTION_INDEX = 6;

    // Declare a variable indicating if the official quiz answers are visible
    private boolean boolShowOfficialAnswers = false;
    // Declare a variable for the number of the first unanswered question
    private int intFirstUnAnsweredQuestion = -1;
    // Declare all the variables that will allow us to connect the designing side ('xml') of our
    // app with its coding side ('java') on a per component basis.
    private ScrollView scrollviewMain;
    private Button buttonMainQuizSubmit;
    private Button buttonMainQuizViewAnswers;
    private EditText edittextMainUserName;
    private EditText edittextMainQuestion1Answer;
    private TextView textviewMainQuizSummary;
    private TextView textviewQuestionsTitlesArray[] = new TextView[NUMBER_OF_QUESTIONS];
    private TextView textviewAnswersTitlesArray[] = new TextView[NUMBER_OF_QUESTIONS];
    private TextView textviewAnswersTextsArray[] = new TextView[NUMBER_OF_QUESTIONS];
    private LinearLayout linearlayoutQuestionsArray[] = new LinearLayout[NUMBER_OF_QUESTIONS];
    private RadioGroup radiogroupMultipleChoicesQuestions[] = new RadioGroup[NUMBER_OF_MULTIPLE_CHOICE_QUESTIONS];
    private RadioButton radiogroupMultipleChoicesQuestionsCorrectAnswers[] = new RadioButton[NUMBER_OF_MULTIPLE_CHOICE_QUESTIONS];
    private CheckBox checkboxMultipleResponsesQuestion2Answers[] = new CheckBox[NUMBER_OF_ANSWERS_FOR_QUESTIONS_2];
    private CheckBox checkboxMultipleResponsesQuestion5Answers[] = new CheckBox[NUMBER_OF_ANSWERS_FOR_QUESTIONS_5];


    @Override
    protected void onStop() {
        super.onStop();

        // Use the default shared preference file to store all the user's inputs
        SharedPreferences.Editor preferencesEditor = getPreferences(PREFERENCE_MODE_PRIVATE).edit();
        preferencesEditor.putString("username", edittextMainUserName.getText().toString());
        preferencesEditor.putString("question1_open_answer", edittextMainQuestion1Answer.getText().toString());
        for (int i = 1; i <= NUMBER_OF_MULTIPLE_CHOICE_QUESTIONS; i++)
            preferencesEditor.putInt("radio_group_selected_button_index" + i, radiogroupMultipleChoicesQuestions[i - 1].getCheckedRadioButtonId());
        for (int i = 1; i <= NUMBER_OF_ANSWERS_FOR_QUESTIONS_2; i++)
            preferencesEditor.putBoolean("multiple_choice_answer2_is_selected_index" + i, checkboxMultipleResponsesQuestion2Answers[i - 1].isChecked());
        for (int i = 1; i <= NUMBER_OF_ANSWERS_FOR_QUESTIONS_5; i++)
            preferencesEditor.putBoolean("multiple_choice_answer5_is_selected_index" + i, checkboxMultipleResponsesQuestion5Answers[i - 1].isChecked());
        // Use the default shared preference file to store the 'state' (have the answers been submitted?) of the quiz
        preferencesEditor.putBoolean("answers_were_submitted", (buttonMainQuizSubmit.getVisibility() == View.GONE));
        // Use the default shared preference file to store the official answers visibility status
        preferencesEditor.putBoolean("official_answers_are_visible", boolShowOfficialAnswers);
        preferencesEditor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Use the default shared preference file to retrieve all the user's inputs
        SharedPreferences preferencesSettings = getPreferences(PREFERENCE_MODE_PRIVATE);
        String restoredText = preferencesSettings.getString("username", null);
        if (restoredText != null) {
            edittextMainUserName.setText(restoredText);
        }
        edittextMainUserName.setText(preferencesSettings.getString("username", null));
        edittextMainQuestion1Answer.setText(preferencesSettings.getString("question1_open_answer", null));
        for (int i = 1; i <= NUMBER_OF_MULTIPLE_CHOICE_QUESTIONS; i++)
            radiogroupMultipleChoicesQuestions[i - 1].check(preferencesSettings.getInt("radio_group_selected_button_index" + i, -1));
        for (int i = 1; i <= NUMBER_OF_ANSWERS_FOR_QUESTIONS_2; i++)
            checkboxMultipleResponsesQuestion2Answers[i - 1].setChecked(preferencesSettings.getBoolean("multiple_choice_answer2_is_selected_index" + i, false));
        for (int i = 1; i <= NUMBER_OF_ANSWERS_FOR_QUESTIONS_5; i++)
            checkboxMultipleResponsesQuestion5Answers[i - 1].setChecked(preferencesSettings.getBoolean("multiple_choice_answer5_is_selected_index" + i, false));
        // Use the default shared preference file to retrieve the 'state' (had the answers been submitted?) of the quiz
        if (preferencesSettings.getBoolean("answers_were_submitted", false)) {
            // Hide the button that should not be visible once a quiz has been scored
            setViewVisibility(buttonMainQuizSubmit, View.GONE);
            // Show the button that should only be visible once a quiz has been scored
            setViewVisibility(buttonMainQuizViewAnswers, View.VISIBLE);
            // Score the quiz
            scoreQuiz();
            // Use the default shared preference file to retrieve the official answers visibility status
            if (preferencesSettings.getBoolean("official_answers_are_visible", false)) {
                // Show all the quiz answers
                setQuizAnswersVisibility(View.VISIBLE);
            }
        } else {
            // Hide the the button that should only be visible once a quiz has been scored
            setViewVisibility(buttonMainQuizViewAnswers, View.GONE);
            // Show the button that should only be visible before a quiz has been scored
            setViewVisibility(buttonMainQuizSubmit, View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the virtual keyboard at the start of the activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Initialize all the variables that will allow us to connect the designing side ('xml') of our
        // app with its coding side ('java') on a per component basis.
        scrollviewMain = findViewById(R.id.scrollview_main);
        edittextMainUserName = findViewById(R.id.edittext_main_user_name);
        textviewMainQuizSummary = findViewById(R.id.textview_main_quiz_summary);

        buttonMainQuizSubmit = findViewById(R.id.button_main_quiz_submit);
        buttonMainQuizViewAnswers = findViewById(R.id.button_main_quiz_view_answers);

        linearlayoutQuestionsArray[0] = findViewById(R.id.linearlayout_main_question_1);
        linearlayoutQuestionsArray[1] = findViewById(R.id.linearlayout_main_question_2);
        linearlayoutQuestionsArray[2] = findViewById(R.id.linearlayout_main_question_3);
        linearlayoutQuestionsArray[3] = findViewById(R.id.linearlayout_main_question_4);
        linearlayoutQuestionsArray[4] = findViewById(R.id.linearlayout_main_question_5);
        linearlayoutQuestionsArray[5] = findViewById(R.id.linearlayout_main_question_6);
        linearlayoutQuestionsArray[6] = findViewById(R.id.linearlayout_main_question_7);
        linearlayoutQuestionsArray[7] = findViewById(R.id.linearlayout_main_question_8);
        linearlayoutQuestionsArray[8] = findViewById(R.id.linearlayout_main_question_9);
        linearlayoutQuestionsArray[9] = findViewById(R.id.linearlayout_main_question_10);

        textviewQuestionsTitlesArray[0] = findViewById(R.id.textview_main_title_question_1);
        textviewQuestionsTitlesArray[1] = findViewById(R.id.textview_main_title_question_2);
        textviewQuestionsTitlesArray[2] = findViewById(R.id.textview_main_title_question_3);
        textviewQuestionsTitlesArray[3] = findViewById(R.id.textview_main_title_question_4);
        textviewQuestionsTitlesArray[4] = findViewById(R.id.textview_main_title_question_5);
        textviewQuestionsTitlesArray[5] = findViewById(R.id.textview_main_title_question_6);
        textviewQuestionsTitlesArray[6] = findViewById(R.id.textview_main_title_question_7);
        textviewQuestionsTitlesArray[7] = findViewById(R.id.textview_main_title_question_8);
        textviewQuestionsTitlesArray[8] = findViewById(R.id.textview_main_title_question_9);
        textviewQuestionsTitlesArray[9] = findViewById(R.id.textview_main_title_question_10);

        edittextMainQuestion1Answer = findViewById(R.id.edittext_main_question_1_answer);

        radiogroupMultipleChoicesQuestions[THIRD_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_3);
        radiogroupMultipleChoicesQuestions[FOURTH_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_4);
        radiogroupMultipleChoicesQuestions[FIFTH_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_6);
        radiogroupMultipleChoicesQuestions[SEVENTH_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_7);
        radiogroupMultipleChoicesQuestions[EIGHTH_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_8);
        radiogroupMultipleChoicesQuestions[NINTH_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_9);
        radiogroupMultipleChoicesQuestions[TENTH_QUESTION_INDEX] = findViewById(R.id.radiogroup_main_question_10);

        radiogroupMultipleChoicesQuestionsCorrectAnswers[THIRD_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_3_answer_c);
        radiogroupMultipleChoicesQuestionsCorrectAnswers[FOURTH_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_4_answer_b);
        radiogroupMultipleChoicesQuestionsCorrectAnswers[FIFTH_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_6_answer_b);
        radiogroupMultipleChoicesQuestionsCorrectAnswers[SEVENTH_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_7_answer_a);
        radiogroupMultipleChoicesQuestionsCorrectAnswers[EIGHTH_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_8_answer_a);
        radiogroupMultipleChoicesQuestionsCorrectAnswers[NINTH_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_9_answer_a);
        radiogroupMultipleChoicesQuestionsCorrectAnswers[TENTH_QUESTION_INDEX] = findViewById(R.id.radiobutton_main_question_10_answer_c);

        checkboxMultipleResponsesQuestion2Answers[0] = findViewById(R.id.checkbox_main_question_2_answer_a);
        checkboxMultipleResponsesQuestion2Answers[1] = findViewById(R.id.checkbox_main_question_2_answer_b);
        checkboxMultipleResponsesQuestion2Answers[2] = findViewById(R.id.checkbox_main_question_2_answer_c);
        checkboxMultipleResponsesQuestion2Answers[3] = findViewById(R.id.checkbox_main_question_2_answer_d);
        checkboxMultipleResponsesQuestion2Answers[4] = findViewById(R.id.checkbox_main_question_2_answer_e);

        checkboxMultipleResponsesQuestion5Answers[0] = findViewById(R.id.checkbox_main_question_5_answer_a);
        checkboxMultipleResponsesQuestion5Answers[1] = findViewById(R.id.checkbox_main_question_5_answer_b);
        checkboxMultipleResponsesQuestion5Answers[2] = findViewById(R.id.checkbox_main_question_5_answer_c);
        checkboxMultipleResponsesQuestion5Answers[3] = findViewById(R.id.checkbox_main_question_5_answer_d);
        checkboxMultipleResponsesQuestion5Answers[4] = findViewById(R.id.checkbox_main_question_5_answer_e);
        checkboxMultipleResponsesQuestion5Answers[5] = findViewById(R.id.checkbox_main_question_5_answer_f);

        textviewAnswersTitlesArray[0] = findViewById(R.id.textview_main_title_answer_1);
        textviewAnswersTitlesArray[1] = findViewById(R.id.textview_main_title_answer_2);
        textviewAnswersTitlesArray[2] = findViewById(R.id.textview_main_title_answer_3);
        textviewAnswersTitlesArray[3] = findViewById(R.id.textview_main_title_answer_4);
        textviewAnswersTitlesArray[4] = findViewById(R.id.textview_main_title_answer_5);
        textviewAnswersTitlesArray[5] = findViewById(R.id.textview_main_title_answer_6);
        textviewAnswersTitlesArray[6] = findViewById(R.id.textview_main_title_answer_7);
        textviewAnswersTitlesArray[7] = findViewById(R.id.textview_main_title_answer_8);
        textviewAnswersTitlesArray[8] = findViewById(R.id.textview_main_title_answer_9);
        textviewAnswersTitlesArray[9] = findViewById(R.id.textview_main_title_answer_10);

        textviewAnswersTextsArray[0] = findViewById(R.id.textview_main_quiz_answer_1);
        textviewAnswersTextsArray[1] = findViewById(R.id.textview_main_quiz_answer_2);
        textviewAnswersTextsArray[2] = findViewById(R.id.textview_main_quiz_answer_3);
        textviewAnswersTextsArray[3] = findViewById(R.id.textview_main_quiz_answer_4);
        textviewAnswersTextsArray[4] = findViewById(R.id.textview_main_quiz_answer_5);
        textviewAnswersTextsArray[5] = findViewById(R.id.textview_main_quiz_answer_6);
        textviewAnswersTextsArray[6] = findViewById(R.id.textview_main_quiz_answer_7);
        textviewAnswersTextsArray[7] = findViewById(R.id.textview_main_quiz_answer_8);
        textviewAnswersTextsArray[8] = findViewById(R.id.textview_main_quiz_answer_9);
        textviewAnswersTextsArray[9] = findViewById(R.id.textview_main_quiz_answer_10);

        // Initialize the quiz layout
        initializeQuizLayout();
    }

    /**
     * Set the 'visible' property of a specific View
     *
     * @param v              The View considered
     * @param visibilityMode The visibility status of the View considered
     */
    private void setViewVisibility(View v, int visibilityMode) {
        v.setVisibility(visibilityMode);
    }

    /**
     * Set the 'visible' property of the quiz answers
     *
     * @param visibilityMode The visibility status of the View considered
     */
    private void setQuizAnswersVisibility(int visibilityMode) {
        for (int i = 0; i < NUMBER_OF_QUESTIONS; i++) {
            setViewVisibility(textviewAnswersTitlesArray[i], visibilityMode);
            setViewVisibility(textviewAnswersTextsArray[i], visibilityMode);
        }
    }

    /**
     * Remove all the icons drawn to the right of the question titles
     */
    private void removeAllQuestionsDrawables() {
        for (int i = 0; i < NUMBER_OF_QUESTIONS; i++)
            textviewQuestionsTitlesArray[i].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    /**
     * Reset all the user input choices offered by the quiz
     */
    private void resetAllUserInputChoices() {
        // Reset the name of the person taking the quiz
        edittextMainUserName.setText("");
        edittextMainUserName.clearFocus();
        // Reset the user input for the first question
        edittextMainQuestion1Answer.setText("");
        edittextMainQuestion1Answer.clearFocus();
        // Reset all the user input choices for the multiple choice questions: 3rd, 4th, 6th, 7th, 8th, 9th, and 10th questions
        for (int i = 0; i < NUMBER_OF_MULTIPLE_CHOICE_QUESTIONS; i++)
            radiogroupMultipleChoicesQuestions[i].clearCheck();
        // Reset all the user input choices for the second question
        for (int i = 0; i < NUMBER_OF_ANSWERS_FOR_QUESTIONS_2; i++)
            checkboxMultipleResponsesQuestion2Answers[i].setChecked(false);
        // Reset all the user input choices for the fifth question
        for (int i = 0; i < NUMBER_OF_ANSWERS_FOR_QUESTIONS_5; i++)
            checkboxMultipleResponsesQuestion5Answers[i].setChecked(false);
    }

    /**
     * Initialize the quiz layout
     */
    private void initializeQuizLayout() {
        // Reset all the user input choices
        resetAllUserInputChoices();
        // Remove all the icons positioned to the right of the questions titles
        removeAllQuestionsDrawables();
        // Hide all the quiz answers
        setQuizAnswersVisibility(View.GONE);
        // Hide the quiz summary
        setViewVisibility(textviewMainQuizSummary, View.GONE);
        // Hide the the button that should only be visible once a quiz has been scored
        setViewVisibility(buttonMainQuizViewAnswers, View.GONE);
        // Show the button that should only be visible before a quiz has been scored
        setViewVisibility(buttonMainQuizSubmit, View.VISIBLE);
    }

    /**
     * Check if each question has been answered by the user
     *
     * @return a message specifying which Questions are in need of an answer
     */
    private String haveAllQuestionsBeenAnswered() {
        int[] arrayOfUnansweredQuestions = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        int unansweredQuestionsCounter = 0;
        intFirstUnAnsweredQuestion = -1;

        // Have all the questions been answered
        //
        // The 1st question is an open response question
        // The 3rd, 4th, 6th, 7th, 8th, 9th, and 10th questions are multiple choice questions
        // The 2nd, and 5th questions are multiple response questions
        int j = 1;
        boolean booleanAnswerIsMissing = false;
        for (int i = 1; i <= NUMBER_OF_QUESTIONS; i++) {
            switch (i) {
                case 1:    //  1st question
                    booleanAnswerIsMissing = (TextUtils.isEmpty(edittextMainQuestion1Answer.getText().toString()));
                    break;
                case 3:    //  3rd question
                case 4:    //  4th question
                case 6:    //  6th question
                case 7:    //  7th question
                case 8:    //  8th question
                case 9:    //  9th question
                case 10:    // 10th question
                    booleanAnswerIsMissing = (radiogroupMultipleChoicesQuestions[j - 1].getCheckedRadioButtonId() == -1);
                    j++;
                    break;
                case 2:    //  2nd question
                    booleanAnswerIsMissing = (!(checkboxMultipleResponsesQuestion2Answers[0].isChecked() ||
                            checkboxMultipleResponsesQuestion2Answers[1].isChecked() ||
                            checkboxMultipleResponsesQuestion2Answers[2].isChecked() ||
                            checkboxMultipleResponsesQuestion2Answers[3].isChecked() ||
                            checkboxMultipleResponsesQuestion2Answers[4].isChecked()));
                    break;
                case 5:    //  5th question
                    booleanAnswerIsMissing = (!(checkboxMultipleResponsesQuestion5Answers[0].isChecked() ||
                            checkboxMultipleResponsesQuestion5Answers[1].isChecked() ||
                            checkboxMultipleResponsesQuestion5Answers[2].isChecked() ||
                            checkboxMultipleResponsesQuestion5Answers[3].isChecked() ||
                            checkboxMultipleResponsesQuestion5Answers[4].isChecked() ||
                            checkboxMultipleResponsesQuestion5Answers[5].isChecked()));
                    break;
            }

            if (booleanAnswerIsMissing) {
                arrayOfUnansweredQuestions[unansweredQuestionsCounter] = i;
                if (intFirstUnAnsweredQuestion == -1)
                    intFirstUnAnsweredQuestion = i;
                unansweredQuestionsCounter++;
            }
        }

        String missingAnswers = "";
        if (unansweredQuestionsCounter > 0) {
            if (unansweredQuestionsCounter == 1)
                missingAnswers = "Question #" + arrayOfUnansweredQuestions[0] + " has not been answered.";
            else {
                missingAnswers = "The following questions #";
                for (int i = 1; i <= unansweredQuestionsCounter; i++) {
                    if (arrayOfUnansweredQuestions[i - 1] == -1)
                        break;

                    if (i <= (unansweredQuestionsCounter - 2)) {
                        missingAnswers = missingAnswers + arrayOfUnansweredQuestions[i - 1] + ", ";
                    } else if (i <= (unansweredQuestionsCounter - 1)) {
                        missingAnswers = missingAnswers + arrayOfUnansweredQuestions[i - 1] + " and ";
                    } else {
                        missingAnswers = missingAnswers + arrayOfUnansweredQuestions[i - 1];
                    }
                }
                missingAnswers = missingAnswers + " have not been answered.";
            }
        }
        return missingAnswers;
    }

    /**
     * Add an icon to the right of a question title representing the status of the answer: right or wrong
     *
     * @param correctAnswer                  True if the question was answered correctly; False if it was not
     * @param questionNumber                 Number of the question considered
     * @param previousNumberOfCorrectAnswers Total number of questions correctly answered until now
     * @return New total number of questions correctly answered
     */
    private int addIconStatusToQuestion(boolean correctAnswer, int questionNumber, int previousNumberOfCorrectAnswers) {
        int numberOfCorrectAnswers = previousNumberOfCorrectAnswers;

        if (correctAnswer) {
            textviewQuestionsTitlesArray[questionNumber - 1].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_answer, 0);
            numberOfCorrectAnswers++;
        } else
            textviewQuestionsTitlesArray[questionNumber - 1].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_wrong_answer, 0);
        textviewQuestionsTitlesArray[questionNumber - 1].setCompoundDrawablePadding(8);

        return numberOfCorrectAnswers;
    }

    /**
     * Determine the number of correct answers
     *
     * @return Number of correct answers
     */
    private int scoreQuiz() {
        boolean correctAnswer = false;
        int numberOfCorrectAnswers = 0;

        // Have all the questions been answered correctly?
        //
        // The 1st question is an open response question
        // The 3rd, 4th, 6th, 7th, 8th, 9th, and 10th questions are multiple choice questions
        // The 2nd, and 5th questions are multiple response questions
        int j = 1;
        for (int i = 1; i <= NUMBER_OF_QUESTIONS; i++) {
            switch (i) {
                case 1:    // 1st question
                    // At least two (2) of the following five (5) words should be part of the user's answer: alp, bank, watch, cheese, and chocolate
                    int intCountTargetedWords = 0;
                    String stringAnswerToQuestion10 = edittextMainQuestion1Answer.getText().toString().toLowerCase();
                    if (stringAnswerToQuestion10.contains("alp"))
                        intCountTargetedWords++;
                    if (stringAnswerToQuestion10.contains("bank"))
                        intCountTargetedWords++;
                    if (stringAnswerToQuestion10.contains("watch"))
                        intCountTargetedWords++;
                    if (stringAnswerToQuestion10.contains("cheese"))
                        intCountTargetedWords++;
                    if (stringAnswerToQuestion10.contains("chocolate"))
                        intCountTargetedWords++;
                    correctAnswer = (intCountTargetedWords >= 2);
                    break;
                case 3:    // 3rd question
                case 4:    // 4th question
                case 6:    // 6th question
                case 7:    // 7th question
                case 8:    // 8th question
                case 9:    // 9th question
                case 10:    // 10th question
                    correctAnswer = radiogroupMultipleChoicesQuestions[j - 1].getCheckedRadioButtonId() == radiogroupMultipleChoicesQuestionsCorrectAnswers[j - 1].getId();
                    j++;
                    break;
                case 2:    // 2nd question
                    correctAnswer = (checkboxMultipleResponsesQuestion2Answers[0].isChecked() &&
                            checkboxMultipleResponsesQuestion2Answers[1].isChecked() &&
                            checkboxMultipleResponsesQuestion2Answers[2].isChecked() &&
                            checkboxMultipleResponsesQuestion2Answers[3].isChecked() &&
                            checkboxMultipleResponsesQuestion2Answers[4].isChecked());
                    break;
                case 5:    // 5th question
                    correctAnswer = (checkboxMultipleResponsesQuestion5Answers[0].isChecked() &&
                            checkboxMultipleResponsesQuestion5Answers[1].isChecked() &&
                            checkboxMultipleResponsesQuestion5Answers[4].isChecked() &&
                            checkboxMultipleResponsesQuestion5Answers[5].isChecked());
                    break;
            }
            numberOfCorrectAnswers = addIconStatusToQuestion(correctAnswer, i, numberOfCorrectAnswers);
        }

        return numberOfCorrectAnswers;
    }

    /**
     * Compute the score of the current quiz
     */
    public void computeQuizScore(View v) {
        // If the user has not entered his/her name, he/she should be reminded to do so
        if (edittextMainUserName.getText().toString().isEmpty()) {
            // Display a toast notification (informing the user of the lack of a name) for a long length/duration
            Toast.makeText(this, getString(R.string.name_required), Toast.LENGTH_LONG).show();
            // Scroll back to the beginning of the quiz
            scrollviewMain.smoothScrollTo(0, 0);
            return;
        }

        // Have all the questions been answered?
        String unansweredQuestions = haveAllQuestionsBeenAnswered();
        // If they have not, let's remind the user to do so
        if (!unansweredQuestions.isEmpty()) {
            // Display a toast notification (informing the user of missing answers) for a long length/duration
            Toast.makeText(this, getString(R.string.answers_required) + "\n\n" + unansweredQuestions, Toast.LENGTH_LONG).show();
            // Scroll back to the first unanswered question
            linearlayoutQuestionsArray[intFirstUnAnsweredQuestion - 1].getParent().requestChildFocus(linearlayoutQuestionsArray[intFirstUnAnsweredQuestion - 1], linearlayoutQuestionsArray[intFirstUnAnsweredQuestion - 1]);
            return;
        }

        // Score the quiz
        int numberOfCorrectAnswers = scoreQuiz();
        // Display a toast notification (informing the user of his/her results) for a long length/duration
        String stringQuizSummary = "You answered correctly " + numberOfCorrectAnswers + " questions out of " + NUMBER_OF_QUESTIONS + ".";
        Toast.makeText(this, stringQuizSummary, Toast.LENGTH_LONG).show();
        // Hide the button that should not be visible once a quiz has been scored
        setViewVisibility(buttonMainQuizSubmit, View.GONE);
        // Show the button that should only be visible once a quiz has been scored
        setViewVisibility(buttonMainQuizViewAnswers, View.VISIBLE);
    }


    /**
     * Display all the official answers to the quiz
     */
    public void displayQuizAnswers(View v) {
        boolShowOfficialAnswers = true;
        // Show all the quiz answers
        setQuizAnswersVisibility(View.VISIBLE);
        // Scroll back to the first question
        linearlayoutQuestionsArray[0].getParent().requestChildFocus(linearlayoutQuestionsArray[0], linearlayoutQuestionsArray[0]);
    }

    /**
     * Reset the current quiz
     */
    public void resetQuiz(View v) {
        boolShowOfficialAnswers = false;
        // Reset the quiz layout
        initializeQuizLayout();
        // Scroll back to the beginning of the quiz
        scrollviewMain.smoothScrollTo(0, 0);
        scrollviewMain.smoothScrollTo(0, 0);
    }
}
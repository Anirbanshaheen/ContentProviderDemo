package com.example.contentproviderdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.contentproviderdemo.droidtermsprovider.DroidTermsExampleContract;

/**
 * Gets the data from the ContentProvider.
 */
public class MainActivity extends AppCompatActivity {

    // The data from the DroidTermsExample content provider
    private Cursor mData;

    // The current state of the app
    private int mCurrentState;

    // The index of the definition and word column in the cursor
    private int mDefCol, mWordCol;

    private TextView mWordTextView, mDefinitionTextView;
    private Button mButton;

    // This state is when the word definition is hidden and clicking the button will therefore
    // show the definition
    /**
     * For Definition Show.
     */
    private final int STATE_HIDDEN = 0;

    // This state is when the word definition is shown and clicking the button will therefore
    // advance the app to the next word
    /**
     *  For Word Show.
     */
    private final int STATE_SHOWN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWordTextView = findViewById(R.id.text_view_word);
        mDefinitionTextView = findViewById(R.id.text_view_definition);
        mButton = findViewById(R.id.button_next);

        new wordFetchTask().execute();
    }

    public void onButtonClick(View view) {
        // Either show the definition of the current word, or if the definition is currently
        // showing, move to the next word.
        switch (mCurrentState) {
            case STATE_HIDDEN:       // Definition hidden
                showDefinition();
                break;
            case STATE_SHOWN:        // Definition shown
                nextWord();
                break;
        }
    }

    private void nextWord() {

        if (mData != null) {
            if (!mData.moveToNext()) {
                mData.moveToFirst();
            }

            /**
             * Here, First hide the definition TextView
             */
            mDefinitionTextView.setVisibility(View.INVISIBLE);

            // Change button text
            mButton.setText(getString(R.string.show_definition));

            // Get the next word
            mWordTextView.setText(mData.getString(mWordCol));
            mDefinitionTextView.setText(mData.getString(mDefCol));

            mCurrentState = STATE_HIDDEN;
        }
    }

    private void showDefinition() {

        if (mData != null) {
            // Show the definition TextView
            mDefinitionTextView.setVisibility(View.VISIBLE);

            // Change button text
            mButton.setText(getString(R.string.next_word));

            mCurrentState = STATE_SHOWN;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mData.close();
    }

    // Use an async task to do the data fetch off of the main thread.
    // First three parameters are Parameters, Progress, Result.
    public class wordFetchTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {

            // Get the content resolver
            ContentResolver resolver = getContentResolver();

            // Call the query method on the resolver with the correct Uri from the contract class
            Cursor cursor = resolver.query(
                    DroidTermsExampleContract.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            // Set the data for MainActivity
            mData = cursor;

            // Get the column index, in the Cursor, of each piece of data
            mDefCol = mData.getColumnIndex(DroidTermsExampleContract.COLUMN_DEFINITION);
            mWordCol = mData.getColumnIndex(DroidTermsExampleContract.COLUMN_WORD);

            // Set the initial state
            nextWord();
        }
    }
}

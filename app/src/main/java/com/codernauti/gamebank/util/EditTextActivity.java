package com.codernauti.gamebank.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditTextActivity extends AppCompatActivity {

    private static final String TAG = "EditTextActivity";

    public static final String TOOLBAR_TITLE = "toolbar_name";
    public static final String EDIT_FIELD_NAME = "edit_field_name";
    public static final String DESCRIPTION = "description";
    public static final String PREVIOUS_VALUE = "nickname";

    public static final String TEXT_RESULT = "text_result";

    @BindView(R.id.edit_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.edit_field_description)
    TextView mDescription;

    @BindView(R.id.edit_input)
    TextInputEditText mInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(TAG, "onCreate EditTextActivity");

        Intent data = getIntent();

        if (data != null) {

            Bundle config = data.getExtras();

            String title = config.getString(TOOLBAR_TITLE);
            String editFieldName = config.getString(EDIT_FIELD_NAME);
            String description = config.getString(DESCRIPTION);
            String previousValue = config.getString(PREVIOUS_VALUE);

            Log.d(TAG, "Data passed: toolbar name: " + title + " editfield: " + editFieldName + " description: " + description);

            getSupportActionBar().setTitle(title);
            mInput.setHint(editFieldName);
            mInput.setText(previousValue);
            mDescription.setText(description);

        } else {
            Log.d(TAG, "Intent data is null");
        }
    }

    @OnClick(R.id.edit_save_btn)
    public void saveText() {
        String result = mInput.getText().toString();

        if (result.isEmpty()) {

            Toast.makeText(this, getString(R.string.warning_edit_input),
                    Toast.LENGTH_SHORT).show();

        } else {

            Intent returnIntent = new Intent();
            returnIntent.putExtra(TEXT_RESULT, result);

            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

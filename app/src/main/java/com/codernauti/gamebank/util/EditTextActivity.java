package com.codernauti.gamebank.util;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.codernauti.gamebank.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditTextActivity extends AppCompatActivity {

    private static final String TOOLBAR_TITLE = "toolbar_name";
    private static final String EDIT_FIELD_NAME = "edit_field_name";
    private static final String DESCRIPTION = "description";

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

        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(TOOLBAR_TITLE);
            String editFieldName = savedInstanceState.getString(EDIT_FIELD_NAME);
            String description = savedInstanceState.getString(DESCRIPTION);

            getSupportActionBar().setTitle(title);
            mInput.setHint(editFieldName);
            mDescription.setText(description);
        }
    }

}

package com.veyndan.generic;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.veyndan.generic.ui.BaseActivity;
import com.veyndan.generic.util.LogUtils;

public class NoteActivity extends BaseActivity {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(NoteActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

}

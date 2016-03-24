package com.veyndan.generic.ui;

import android.annotation.SuppressLint;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.veyndan.generic.R;

import butterknife.Bind;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    @Nullable @Bind(R.id.toolbar) Toolbar toolbar;

    protected Toolbar getToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        return toolbar;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }
}

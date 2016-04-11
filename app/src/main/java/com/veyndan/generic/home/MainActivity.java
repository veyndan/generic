package com.veyndan.generic.home;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.veyndan.generic.R;
import com.veyndan.generic.ui.BaseActivity;
import com.veyndan.generic.util.LogUtils;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Remove toolbar title
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(null);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Returning false so overflow icon isn't drawn, return true to draw it
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

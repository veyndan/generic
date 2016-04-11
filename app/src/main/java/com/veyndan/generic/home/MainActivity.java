package com.veyndan.generic.home;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.veyndan.generic.R;
import com.veyndan.generic.ui.BaseActivity;
import com.veyndan.generic.util.LogUtils;

import butterknife.Bind;
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

        final RecyclerView postBackStack = (RecyclerView) findViewById(R.id.post_back_stack);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        if (postBackStack != null) {
            postBackStack.setLayoutManager(linearLayoutManager);
        }

        BackStackAdapter adapter = new BackStackAdapter();
        if (postBackStack != null) {
            postBackStack.setAdapter(adapter);
        }

    }

    public static class BackStackAdapter extends RecyclerView.Adapter<BackStackAdapter.VH> {

        @Override
        public BackStackAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.profile_back_stack, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(BackStackAdapter.VH holder, int position) {
            Glide.with(holder.itemView.getContext()).load("https://scontent-lhr3-1.xx.fbcdn.net/hphotos-frc3/v/" +
                    "t1.0-9/1098101_1387041911520027_1668446817_n.jpg?oh=" +
                    "85cb27b32003fb5080e73e18d03bbbc4&oe=574FB4F9").into(holder.image);
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        public class VH extends RecyclerView.ViewHolder {
            @Bind(R.id.profile_back_stack_image)
            ImageView image;

            public VH(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
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

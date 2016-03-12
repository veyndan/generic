package com.veyndan.generic;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.veyndan.generic.util.LogUtils;

public class MainActivity extends BaseActivity {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remove toolbar title
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(null);
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (HackyViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        if (viewPager != null && tabLayout != null) {
            viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));
            tabLayout.setupWithViewPager(viewPager);
            // Must do setupWithViewPager before setting icons
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setIcon(R.drawable.ic_home_24dp);
                }
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

    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 1;

        public SampleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return HomeFragment.newInstance();
        }

    }

}

package com.veyndan.generic.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.veyndan.generic.R;
import com.veyndan.generic.util.LogUtils;

public class HomeFragment extends Fragment implements ScrollRecyclerView {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(HomeFragment.class);

    private HomeAdapter adapter;
    private RecyclerView recyclerView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO: Gif loading in Android M in the recyclerview problem
        // https://stackoverflow.com/questions/33363107/warning-using-glide-in-recyclerview

        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        adapter = new HomeAdapter(this, getActivity(), new Firebase("https://sweltering-heat-8337.firebaseio.com"));
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

    @Override
    public void scrollBy(int dy) {
        if (recyclerView != null) recyclerView.smoothScrollBy(0, dy);
    }
}

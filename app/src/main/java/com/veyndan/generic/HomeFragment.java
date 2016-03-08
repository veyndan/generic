package com.veyndan.generic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;

public class HomeFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(HomeFragment.class);

    private HomeAdapter adapter;

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

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        adapter = new HomeAdapter(getActivity(), new Firebase("https://sweltering-heat-8337.firebaseio.com"));
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }
}

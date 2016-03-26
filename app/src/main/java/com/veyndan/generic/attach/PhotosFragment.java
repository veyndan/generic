package com.veyndan.generic.attach;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.View;

import com.veyndan.generic.R;
import com.veyndan.generic.attach.util.Gallery;
import com.veyndan.generic.util.LogUtils;
import com.veyndan.generic.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class PhotosFragment extends BottomSheetDialogFragment {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(PhotosFragment.class);

    private static final int GRID_SPACING_DP = 4;

    private final BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback =
            new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.attach_photo_fragment, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetBehaviorCallback);
        }

        int screenWidth = getScreenWidth();
        int gridSpanCount = getGridSpanCount(screenWidth);
        int itemWidth = getItemWidth(screenWidth, gridSpanCount);

        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.attach_photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), gridSpanCount));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(gridSpanCount, UIUtils.dpToPx(getContext(), GRID_SPACING_DP), false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                // IMPORTANT: Allows animations in RecyclerView e.g. ImageView spring, to continue
                // on notifying data set change.
                return true;
            }
        });

        PhotosAdapter adapter = new PhotosAdapter(init(), itemWidth);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback();
        recyclerView.setAdapter(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private List<Photo> init() {
        List<Photo> photos = new ArrayList<>();
        for (String path : Gallery.getImagesPath(getContext())) {
            photos.add(new Photo(path));
        }
        return photos;
    }

    private int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private int getGridSpanCount(int screenWidth) {
        return (int) Math.floor((float) screenWidth / UIUtils.dpToPx(getContext(), 120));
    }

    private int getItemWidth(int screenWidth, int gridSpanCount) {
        return (int) ((float) screenWidth / gridSpanCount);
    }

}

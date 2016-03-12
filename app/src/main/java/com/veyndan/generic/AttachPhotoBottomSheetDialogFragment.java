package com.veyndan.generic;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.veyndan.generic.ui.Gallery;
import com.veyndan.generic.ui.GridSpacingItemDecoration;
import com.veyndan.generic.util.UIUtils;

public class AttachPhotoBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final int GRID_SPAN_COUNT = 3;

    private final BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

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

        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.attach_photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), GRID_SPAN_COUNT));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(GRID_SPAN_COUNT, UIUtils.dpToPx(getContext(), 4), false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                // IMPORTANT: Allows animations in RecyclerView e.g. ImageView spring, to continue
                // on notifying data set change.
                return true;
            }
        });

        ImagesAdapter adapter = new ImagesAdapter(getContext(), Gallery.getImagesPath(getContext()));
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

}

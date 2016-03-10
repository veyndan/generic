package com.veyndan.generic;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AttachPhotoBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

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
        View contentView = View.inflate(getContext(),
                R.layout.attach_photo_bottom_sheet_dialog_fragment, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetBehaviorCallback);
        }
        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.attach_photo_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ImagesAdapter(getContext(), getImagesPath(getContext())));
    }

    public static List<String> getImagesPath(Context context) {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

        List<String> imagePaths = new ArrayList<>();

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isLast()) {
                cursor.moveToNext();
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                imagePaths.add(cursor.getString(dataColumnIndex));
            }
            cursor.close();
        }

        return imagePaths;
    }

}

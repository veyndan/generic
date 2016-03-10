package com.veyndan.generic;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.veyndan.generic.util.LogUtils;
import com.veyndan.generic.util.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.VH> {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(ImagesAdapter.class);

    private final Context context;
    private final List<String> imagePaths;

    private final List<Integer> selected;

    public ImagesAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;

        selected = new ArrayList<>(Collections.nCopies(imagePaths.size(), 0));
        Log.d(TAG, "ImagesAdapter: " + selected);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attach_camera, parent, false);
        return new VH(context, v, selected);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Glide.with(context).loadFromMediaStore(
                Uri.fromFile(new File(imagePaths.get(position)))).into(holder.image);
        holder.count.setText(String.valueOf(selected.get(position)));
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public class VH extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        private final String TAG = LogUtils.makeLogTag(VH.class);

        final ImageView image;
        final TextView count;

        public VH(final Context context, final View itemView, final List<Integer> selected) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_attach_camera_image);
            count = (TextView) itemView.findViewById(R.id.item_attach_count);

            // Create a system to run the physics loop for a set of springs.
            final SpringSystem springSystem = SpringSystem.create();
            final Spring spring = springSystem.createSpring();

            final float depth = 0.72f;

            spring.addListener(new SimpleSpringListener() {

                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();
                    float scale = 1f - (value * (1f - depth));
                    image.setScaleX(scale);
                    image.setScaleY(scale);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    spring.setEndValue(spring.getEndValue() == 1.0f ? 0 : 1.0f);
                    if (spring.getEndValue() == 1.0f) {
                        selected.set(getAdapterPosition(), selected.size() - Collections.frequency(selected, 0) + 1);
                        int margin = (int) ((itemView.getWidth() - itemView.getWidth() * depth) / 2 - UIUtils.dpToPx(context, 12));
                        ((RelativeLayout.LayoutParams) count.getLayoutParams()).setMargins(margin, margin, margin, margin);
                        count.setVisibility(View.VISIBLE);
                    } else {
                        int c = selected.get(getAdapterPosition());
                        for (int i = 0; i < selected.size(); i++) {
                            int current = selected.get(i);
                            if (current > c) {
                                selected.set(i, current - 1);
                                notifyItemChanged(i);
                            }
                        }
                        selected.set(getAdapterPosition(), 0);
                        count.setVisibility(View.GONE);
                    }
                    notifyItemChanged(getAdapterPosition());
                    Log.d(TAG, "onClick: " + selected);
                }
            });
        }
    }
}

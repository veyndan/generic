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

    private static final float SPRING_SCALE = 0.72f;

    private final Context context;
    private final List<String> imagePaths;
    private final List<Integer> selected;

    private final SpringSystem springSystem;

    public ImagesAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;

        selected = new ArrayList<>(Collections.nCopies(imagePaths.size(), 0));

        springSystem = SpringSystem.create();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attach_camera, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Glide.with(context).loadFromMediaStore(
                Uri.fromFile(new File(imagePaths.get(position)))).into(holder.image);
        int visibility;
        float scale;
        if (selected.get(position) == 0) {
            visibility = View.GONE;
            scale = 1f;
        } else {
            int margin = (int) ((holder.itemView.getWidth() -
                    holder.itemView.getWidth() * SPRING_SCALE) / 2 - UIUtils.dpToPx(context, 12));
            ((RelativeLayout.LayoutParams) holder.count.getLayoutParams())
                    .setMargins(margin, margin, margin, margin);
            holder.count.setText(String.valueOf(selected.get(position)));
            visibility = View.VISIBLE;
            scale = SPRING_SCALE;
        }
        holder.count.setVisibility(visibility);
        if (holder.spring.isAtRest()) {
            Log.d(TAG, "onBindViewHolder: At rest " + position);
            holder.image.setScaleX(scale);
            holder.image.setScaleY(scale);
        }
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
        final Spring spring;

        public VH(final View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_attach_camera_image);
            count = (TextView) itemView.findViewById(R.id.item_attach_camera_count);

            // Create a system to run the physics loop for a set of springs.
            spring = springSystem.createSpring();

            spring.addListener(new SimpleSpringListener() {
                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();
                    float scale = 1f - value * (1f - SPRING_SCALE);
                    image.setScaleX(scale);
                    image.setScaleY(scale);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double endValue;
                    if (selected.get(getAdapterPosition()) == 0) {
                        endValue = 1;
                        selected.set(getAdapterPosition(),
                                selected.size() - Collections.frequency(selected, 0) + 1);
                    } else {
                        endValue = 0;
                        int c = selected.get(getAdapterPosition());
                        for (int i = 0; i < selected.size(); i++) {
                            int current = selected.get(i);
                            if (current > c) {
                                selected.set(i, current - 1);
                                notifyItemChanged(i);
                            }
                        }
                        selected.set(getAdapterPosition(), 0);
                    }
                    spring.setEndValue(endValue);
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}

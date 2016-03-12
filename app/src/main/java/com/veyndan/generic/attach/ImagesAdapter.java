package com.veyndan.generic.attach;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.veyndan.generic.R;
import com.veyndan.generic.util.LogUtils;
import com.veyndan.generic.util.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.VH>
        implements ItemTouchHelperAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(ImagesAdapter.class);

    private static final float SPRING_SCALE = 0.72f;

    private boolean hide = false;

    private final Context context;
    private final List<String> imagePaths;
    private final List<Integer> selected;

    private final SpringSystem springSystem;

    private int counterMargin = 0;

    public ImagesAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;

        selected = new ArrayList<>(imagePaths.size());

        springSystem = SpringSystem.create();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attach_photo_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (hide) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            Glide.with(context).loadFromMediaStore(
                    Uri.fromFile(new File(imagePaths.get(position)))).into(holder.image);
            float scale;
            if (!selected.contains(position)) {
                holder.count.setVisibility(View.GONE);
                scale = 1f;
            } else {
                ((RelativeLayout.LayoutParams) holder.count.getLayoutParams())
                        .setMargins(counterMargin, counterMargin, counterMargin, counterMargin);
                holder.count.setText(String.valueOf(selected.indexOf(position) + 1));
                holder.count.setVisibility(View.VISIBLE);
                scale = SPRING_SCALE;
            }
            if (holder.spring.isAtRest()) {
                holder.image.setScaleX(scale);
                holder.image.setScaleY(scale);
            }
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    public class VH extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        @SuppressWarnings("unused")
        private final String TAG = LogUtils.makeLogTag(VH.class);

        final ImageView image;
        final TextView count;
        final Spring spring;

        public VH(final View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_attach_camera_image);
            count = (TextView) itemView.findViewById(R.id.item_attach_camera_count);

            if (counterMargin == 0) {
                itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        counterMargin = (int) ((itemView.getWidth() * (1.0f - SPRING_SCALE)) / 2 - UIUtils.dpToPx(context, 8));
                        itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }

            // Create a system to run the physics loop for a set of springs.
            spring = springSystem.createSpring();

            spring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40, 7));

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
                    if (!selected.contains(getAdapterPosition())) {
                        spring.setCurrentValue(0, true);
                        spring.setEndValue(1);
                        selected.add(getAdapterPosition());
                    } else {
                        spring.setCurrentValue(1, true);
                        spring.setEndValue(0);
                        int c = selected.indexOf(getAdapterPosition());
                        selected.remove(c);
                        for (int i = c; i < selected.size(); i++) {
                            notifyItemChanged(selected.get(i));
                        }
                    }
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        @Override
        public void onItemSelected() {
            hide = true;
            for (Integer i = 0; i < getItemCount(); i++) {
                if (!selected.contains(i)) notifyItemChanged(i);
            }
        }

        @Override
        public void onItemClear() {
            hide = false;
            for (Integer i = 0; i < getItemCount(); i++) {
                if (!selected.contains(i)) notifyItemChanged(i);
            }
        }
    }
}

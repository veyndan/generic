package com.veyndan.generic.attach;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import com.veyndan.generic.R;
import com.veyndan.generic.util.LogUtils;
import com.veyndan.generic.util.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH>
        implements ItemTouchHelperAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(PhotosAdapter.class);

    private static final float SPRING_SCALE = 0.72f;

    private final int durationCollapse;
    private final int durationShort;

    private Interpolator interpolatorCollapse;

    private float location[] = new float[2];

    private final Context context;
    private final List<String> imagePaths;
    private final List<Integer> selected;

    private final SpringSystem springSystem;

    private int counterMargin = 0;

    private List<VH> selectedItemViews;

    private int longPressed = -1;

    public PhotosAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;

        selected = new ArrayList<>(imagePaths.size());

        springSystem = SpringSystem.create();

        durationCollapse = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
        durationShort = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        interpolatorCollapse = new DecelerateInterpolator(4);

        selectedItemViews = new ArrayList<>();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attach_photo_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        final float x = location[0] - holder.itemView.getX();
        final float y = location[1] - holder.itemView.getY();
        if (longPressed != -1) {
            if (longPressed == position) {
                holder.count.setVisibility(View.VISIBLE);
                holder.count.setText(String.valueOf(selected.size()));
            } else if (selected.contains(position)) {

                // Max 3 values of selected excluding longPressed
                final List<Integer> list = Ordering.natural().greatestOf(
                        Collections2.filter(selected, new Predicate<Integer>() {
                            @Override
                            public boolean apply(Integer input) {
                                return input != longPressed;
                            }
                        }), 3);

                TranslateAnimation translate = new TranslateAnimation(0, x, 0, y);
                translate.setInterpolator(interpolatorCollapse);
                translate.setDuration(durationCollapse);
                translate.setFillAfter(true);
                holder.itemView.startAnimation(translate);

                holder.image.animate()
                        .rotation(list.contains(position) ? 8 * (list.indexOf(position) + 1) : 24)
                        .alpha(list.contains(position) ? 0.6f : 0)
                        .setInterpolator(interpolatorCollapse)
                        .setDuration(durationCollapse);

                holder.itemView.setTag("anim");
                holder.count.setVisibility(View.GONE);

                // TODO selectedItemViews probably causing a lot of UI and performance issues
                // Somehow attach underlay to top view, as only need images, removing the need
                // for this. Similar to the previous commit one with an xml file, but instead
                // dynamically do a similar thing.
                if (list.contains(holder.getAdapterPosition())) selectedItemViews.add(holder);
            } else {
                ObjectAnimator
                        .ofFloat(holder.itemView, View.ALPHA, 0f)
                        .setDuration(durationShort)
                        .start();
            }
        } else {
            if (selected.contains(position) && "anim".equals(holder.itemView.getTag())) {
                TranslateAnimation animation = new TranslateAnimation(x, 0, y, 0);
                animation.setInterpolator(interpolatorCollapse);
                animation.setDuration(durationCollapse);
                animation.setFillAfter(true);
                holder.itemView.startAnimation(animation);

                holder.image.animate()
                        .rotation(0)
                        .alpha(1)
                        .setInterpolator(interpolatorCollapse)
                        .setDuration(durationCollapse);

                holder.itemView.setTag(null);

                selectedItemViews.clear();
            }
            ObjectAnimator
                    .ofFloat(holder.itemView, View.ALPHA, 1f)
                    .setDuration(durationShort)
                    .start();
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
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        viewHolder.itemView.setTranslationX(dX);
        viewHolder.itemView.setTranslationY(dY);
        for (VH holder : selectedItemViews) {
            holder.itemView.setTranslationX(dX);
            holder.itemView.setTranslationY(dY);
        }
        viewHolder.itemView.bringToFront();
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
                        counterMargin = (int) ((itemView.getWidth() *
                                (1.0f - SPRING_SCALE)) / 2 - UIUtils.dpToPx(context, 8));
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
            if (!selected.contains(getAdapterPosition())) {
                selected.add(getAdapterPosition());
                spring.setCurrentValue(0, true);
                spring.setEndValue(1);
            }
            location[0] = itemView.getLeft();
            location[1] = itemView.getTop();
            longPressed = getAdapterPosition();
            notifyItemRangeChanged(0, getItemCount());
        }

        @Override
        public void onItemClear() {
            longPressed = -1;
            notifyItemRangeChanged(0, getItemCount());
        }

    }
}

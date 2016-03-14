package com.veyndan.generic.attach;

import android.content.Context;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
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

/**
 * TODO selectedItemViews probably causing a lot of UI and performance issues
 * Somehow attach underlay to top view, as only need images, removing the need
 * for this. Similar to the previous commit one with an xml file, but instead
 * dynamically do a similar thing.
 * <p/>
 * TODO Stop bringToFront() being called for top longPressed view every time it is moved.
 * <p/>
 * TODO Hide bottom sheet while maintaining 'hold' of selected photos into HomeFragment.
 * <p/>
 * TODO Show bottom sheet again if not dropped into note composition view.
 * <p/>
 * TODO Add to composition view if photos dropped in.
 */
public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH>
        implements ItemTouchHelperAdapter {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(PhotosAdapter.class);

    private float springScale = 0;

    private final int durationCollapse;
    private final int durationShort;

    private Interpolator interpolatorCollapse;

    private float location[] = new float[2];

    private final Context context;
    private final List<Photo> photos;
    private final List<Integer> selected;

    private final SpringSystem springSystem;

    private List<VH> selectedItemViews;

    private int longPressed = -1;

    public PhotosAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;

        selected = new ArrayList<>(photos.size());

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
        test(position);

        final float x = location[0] - holder.itemView.getX();
        final float y = location[1] - holder.itemView.getY();
        if (longPressed == position) {
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(String.valueOf(selected.size()));
        } else if (longPressed != -1) {
            if (selected.contains(position)) {

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

                if (list.contains(holder.getAdapterPosition())) selectedItemViews.add(holder);
            } else {
                holder.image.animate()
                        .alpha(0)
                        .setDuration(durationShort);
            }
        } else {
            if ("anim".equals(holder.itemView.getTag())) {
                TranslateAnimation animation = new TranslateAnimation(x, 0, y, 0);
                animation.setInterpolator(interpolatorCollapse);
                animation.setDuration(durationCollapse);
                animation.setFillAfter(true);
                holder.itemView.startAnimation(animation);

                holder.image.animate()
                        .rotation(0)
                        .setInterpolator(interpolatorCollapse)
                        .setDuration(durationCollapse);

                holder.itemView.setTag(null);

                selectedItemViews.clear();
            }
            holder.image.animate()
                    .alpha(1)
                    .setDuration(durationShort);
            Glide.with(context).loadFromMediaStore(
                    Uri.fromFile(new File(photos.get(position).getPath()))).into(holder.image);
            float scale;
            if (!selected.contains(position)) {
                holder.count.setVisibility(View.GONE);
                scale = 1f;
            } else {
                holder.count.setText(String.valueOf(selected.indexOf(position) + 1));
                holder.count.setVisibility(View.VISIBLE);
                scale = springScale;
            }
            if (holder.spring.isAtRest()) {
                holder.image.setScaleX(scale);
                holder.image.setScaleY(scale);
            }
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder
            viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        viewHolder.itemView.setTranslationX(dX);
        viewHolder.itemView.setTranslationY(dY);
        for (VH holder : selectedItemViews) {
            holder.itemView.setTranslationX(dX);
            holder.itemView.setTranslationY(dY);
        }
        viewHolder.itemView.bringToFront();
    }

    public void test(int position) {
        int a = selected.indexOf(position);
        int b = photos.get(position).getCount();
        List<Integer> list = new ArrayList<>();
        if (a != b) {
            Log.e(TAG, position + "\n" +
                    "Expected: " + a + "    " + selected + "\n" +
                    "Received: " + b + "    " + list);
        }
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

            if (springScale == 0) {
                itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        float width = itemView.getWidth();
                        springScale = (width - UIUtils.dpToPx(context, 32)) / width;
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
                    float scale = 1f - value * (1f - springScale);
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

                        // Photos
                        int count = 0;
                        for (Photo photo : photos) {
                            if (photo.isSelected()) count++;
                        }
                        photos.get(getAdapterPosition()).setCount(count);
                        //
                    } else {
                        spring.setCurrentValue(1, true);
                        spring.setEndValue(0);
                        int c = selected.indexOf(getAdapterPosition());
                        selected.remove(c);

                        // Photos
                        for (Photo photo : photos) {
                            if (photo.getCount() > photos.get(getAdapterPosition()).getCount()) {
                                photo.setCount(photo.getCount() - 1);
                            }
                        }
                        photos.get(getAdapterPosition()).setCount(-1);
                        //

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

                //Photos
                int count = 0;
                for (Photo photo : photos) {
                    if (photo.isSelected()) count++;
                }
                photos.get(getAdapterPosition()).setCount(count);
                //

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

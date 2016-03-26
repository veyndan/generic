package com.veyndan.generic.attach;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import com.veyndan.generic.R;
import com.veyndan.generic.util.LogUtils;
import com.veyndan.generic.util.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.ButterKnife;

// TODO Hide bottom sheet while maintaining 'hold' of selected photos into HomeFragment.
// TODO Show bottom sheet again if not dropped into note composition view.
// TODO Add to composition view if photos dropped in.

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.VH> {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(PhotosAdapter.class);

    private final List<Photo> photos;
    private final int itemWidth;

    private boolean selected = false;
    private float selectedLocation[] = new float[2];

    public PhotosAdapter(List<Photo> photos, int itemWidth) {
        this.photos = photos;
        this.itemWidth = itemWidth;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attach_photo_item, parent, false);
        return new VH(v, itemWidth);
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        holder.bind(photos.get(position), photos);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class VH extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        @SuppressWarnings("unused")
        private final String TAG = LogUtils.makeLogTag(VH.class);

        @BindInt(android.R.integer.config_mediumAnimTime) int durationCollapse;
        @BindInt(android.R.integer.config_shortAnimTime) int durationShort;

        @Bind(R.id.item_attach_camera_image) ImageView image;
        @Bind({R.id.test1, R.id.test2, R.id.test3}) List<ImageView> tests;
        @Bind(R.id.item_attach_camera_count) TextView count;

        private final SpringSystem springSystem = SpringSystem.create();
        private final Spring spring = springSystem.createSpring();
        private final float springScale;
        private final Interpolator interpolatorCollapse = new DecelerateInterpolator(4);

        private final Context context;

        public VH(final View itemView, final int itemWidth) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.context = itemView.getContext();

            springScale = ((float) itemWidth - UIUtils.dpToPx(context, 32)) / itemWidth;

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
                    if (photos.get(getAdapterPosition()).isSelected()) {
                        spring.setCurrentValue(1, true);
                        spring.setEndValue(0);

                        photos.get(getAdapterPosition()).setCount(-1);
                        for (int i = 0; i < photos.size(); i++) {
                            if (photos.get(i).getCount() > photos.get(getAdapterPosition()).getCount()) {
                                photos.get(i).setCount(photos.get(i).getCount() - 1);
                                notifyItemChanged(i);
                            }
                        }
                    } else {
                        spring.setCurrentValue(0, true);
                        spring.setEndValue(1);

                        int count = 0;
                        for (Photo photo : photos) {
                            if (photo.isSelected()) count++;
                        }
                        photos.get(getAdapterPosition()).setCount(count);
                    }
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        public void bind(Photo photo, final List<Photo> photos) {
            final float x = selectedLocation[0] - itemView.getX();
            final float y = selectedLocation[1] - itemView.getY();
            int position = getAdapterPosition();

            if (Objects.equal(itemView.getTag(R.id.tag_selected), position)) {
                count.setVisibility(View.VISIBLE);
                count.setText(String.valueOf(Collections2.filter(photos, Photo::isSelected).size()));

                List<Integer> list1 = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    if (position == i) continue;
                    Photo p = photos.get(i);
                    if (p.getCount() != -1) list1.add(p.getCount());
                }
                list1 = Ordering.natural().greatestOf(list1, 3);
                final List<Integer> list = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    if (list1.contains(photos.get(i).getCount())) list.add(i);
                }

                new Handler().postDelayed(() -> {
                    for (int i = 0; i < list.size(); i++) {
                        tests.get(i).setVisibility(View.VISIBLE);
                        Glide.with(context).loadFromMediaStore(
                                Uri.fromFile(new File(photos.get(list.get(i)).getPath())))
                                .into(tests.get(i));
                    }
                }, durationCollapse);

                for (ImageView test : tests) {
                    test.setScaleX(springScale);
                    test.setScaleY(springScale);
                }

            } else if (selected) {
                for (ImageView test : tests) {
                    test.setVisibility(View.GONE);
                }
                if (photo.isSelected()) {

                    // TODO Gives right elements but not in correct order (reason is using .contains)
                    List<Integer> list1 = new ArrayList<>();
                    for (Photo p : photos) {
                        list1.add(p.getCount());
                    }
                    list1 = Ordering.natural().greatestOf(list1, 3);
                    List<Integer> list = new ArrayList<>();
                    for (int i = 0; i < photos.size(); i++) {
                        if (list1.contains(photos.get(i).getCount())) list.add(i);
                    }

                    TranslateAnimation translate = new TranslateAnimation(0, x, 0, y);
                    translate.setInterpolator(interpolatorCollapse);
                    translate.setDuration(durationCollapse);
                    translate.setFillAfter(true);
                    itemView.startAnimation(translate);

                    image.animate()
                            .rotation(list.contains(position) ? 8 * (list.indexOf(position) + 1) : 24)
//                            .alpha(list.contains(position) ? 0.6f : 0)
                            .alpha(0)
                            .setInterpolator(interpolatorCollapse)
                            .setDuration(durationCollapse);

                    itemView.setTag("anim");
                    count.setVisibility(View.GONE);
                } else {
                    image.animate()
                            .alpha(0)
                            .setDuration(durationShort);
                }
            } else {
                for (ImageView test : tests) {
                    test.setVisibility(View.GONE);
                }
                if ("anim".equals(itemView.getTag())) {
                    TranslateAnimation animation = new TranslateAnimation(x, 0, y, 0);
                    animation.setInterpolator(interpolatorCollapse);
                    animation.setDuration(durationCollapse);
                    animation.setFillAfter(true);
                    itemView.startAnimation(animation);

                    image.animate()
                            .rotation(0)
                            .setInterpolator(interpolatorCollapse)
                            .setDuration(durationCollapse);

                    itemView.setTag(null);
                }
                image.animate()
                        .alpha(1)
                        .setDuration(durationShort);
                Glide.with(context).loadFromMediaStore(
                        Uri.fromFile(new File(photos.get(position).getPath()))).into(image);
                float scale;
                if (photos.get(position).isSelected()) {
                    count.setText(String.valueOf(photos.get(position).getCount() + 1));
                    count.setVisibility(View.VISIBLE);
                    scale = springScale;
                } else {
                    count.setVisibility(View.GONE);
                    scale = 1f;
                }
                if (spring.isAtRest()) {
                    image.setScaleX(scale);
                    image.setScaleY(scale);
                }
            }
        }

        @Override
        public void onItemSelected() {
            if (!photos.get(getAdapterPosition()).isSelected()) {
                int count = 0;
                for (Photo photo : photos) {
                    if (photo.isSelected())
                        count++;
                }
                photos.get(getAdapterPosition()).setCount(count);

                spring.setCurrentValue(0, true).setEndValue(1);
            }
            selectedLocation[0] = itemView.getLeft();
            selectedLocation[1] = itemView.getTop();
            itemView.setTag(R.id.tag_selected, getAdapterPosition());
            selected = true;
            notifyItemRangeChanged(0, getItemCount());
        }

        @Override
        public void onItemClear() {
            itemView.setTag(R.id.tag_selected, null);
            selected = false;
            notifyItemRangeChanged(0, getItemCount());
        }

    }
}

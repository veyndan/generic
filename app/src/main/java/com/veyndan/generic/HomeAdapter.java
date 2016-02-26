package com.veyndan.generic;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private final Context context;
    private final String[] dataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = LogUtils.makeLogTag(ViewHolder.class);

        final TextView name, about;
        final TextView description;
        final ImageView profile, image;
        final ToggleButton heart, code, basket;
        final AppCompatImageButton more;

        public ViewHolder(View v, Context context) {
            super(v);
            name = (TextView) v.findViewById(R.id.name);
            about = (TextView) v.findViewById(R.id.about);
            description = (TextView) v.findViewById(R.id.description);
            profile = (ImageView) v.findViewById(R.id.profile);
            image = (ImageView) v.findViewById(R.id.image);
            heart = (ToggleButton) v.findViewById(R.id.heart);
            code = (ToggleButton) v.findViewById(R.id.code);
            basket = (ToggleButton) v.findViewById(R.id.basket);
            more = (AppCompatImageButton) v.findViewById(R.id.more);

            // Make profile picture black and white
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            profile.setColorFilter(new ColorMatrixColorFilter(matrix));

            // Popup menu for QAB overflow
            final PopupMenu menu = new PopupMenu(context, more);
            menu.getMenu().add("titleRes");

            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menu.show();
                }
            });
        }
    }

    public HomeAdapter(Context context, String[] dataset) {
        this.context = context;
        this.dataset = dataset;
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ViewHolder(v, context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }
}
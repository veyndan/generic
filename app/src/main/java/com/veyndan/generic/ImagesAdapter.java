package com.veyndan.generic;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.veyndan.generic.util.LogUtils;

import java.io.File;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.VH> {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(ImagesAdapter.class);

    private final Context context;
    private final List<String> imagePaths;

    public ImagesAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
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
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VH.class);

        final ImageView image;

        public VH(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_attach_camera_image);
        }
    }
}

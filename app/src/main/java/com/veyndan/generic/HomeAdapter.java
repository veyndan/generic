package com.veyndan.generic;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends FirebaseAdapterRecyclerAdapter<Post, HomeAdapter.VH> {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(HomeAdapter.class);

    private final Context context;
    private final Resources res;
    private final Firebase rootRef;

    public HomeAdapter(Context context, Firebase rootRef) {
        super(Post.class, rootRef);
        this.context = context;
        this.res = context.getResources();
        this.rootRef = rootRef;
    }

    @Override
    protected VH onCreateHeaderItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_header, parent, false);
        return new VHHeader(v, context);
    }

    @Override
    protected VH onCreateContentItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content, parent, false);
        return new VHItem(v, context);
    }

    @Override
    protected void onBindHeaderItemViewHolder(VH holder, int position) {
        final VHHeader vhHeader = (VHHeader) holder;
        Glide.with(context).load("https://scontent-lhr3-1.xx.fbcdn.net/hphotos-frc3/v/t1.0-9/1098101_1387041911520027_1668446817_n.jpg?oh=85cb27b32003fb5080e73e18d03bbbc4&oe=574FB4F9").into(vhHeader.profile);
        vhHeader.name.setText("Veyndan Stuart");
        vhHeader.date.setText(context.getString(R.string.date, "Now"));

        vhHeader.description.removeAllViewsInLayout();

        final EditText paragraph = (EditText) LayoutInflater.from(vhHeader.description.getContext())
                .inflate(R.layout.description_paragraph_new, vhHeader.description, false);
        vhHeader.description.addView(paragraph);

        vhHeader.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Post.Description> descriptions = new ArrayList<>();
                for (int i = 0; i < vhHeader.description.getChildCount(); i++) {
                    View child = vhHeader.description.getChildAt(i);
                    if (child instanceof EditText) {
                        descriptions.add(new Post.Description(
                                ((EditText) child).getText().toString(),
                                Post.Description.TYPE_PARAGRAPH
                        ));
                    }
                }
                rootRef.push().setValue(new Post(
                        vhHeader.name.getText().toString(),
                        "Now",
                        vhHeader.visibility.getSelectedItem().toString(),
                        "0",
                        "https://scontent-lhr3-1.xx.fbcdn.net/hphotos-frc3/v/t1.0-9/1098101_1387041911520027_1668446817_n.jpg?oh=85cb27b32003fb5080e73e18d03bbbc4&oe=574FB4F9",
                        descriptions
                ));
                paragraph.setText(null);
            }
        });
    }

    @Override
    protected void onBindContentItemViewHolder(VH holder, final int position) {
        VHItem vhItem = (VHItem) holder;
        Post post = getItem(position);
        Glide.with(context).load(post.getProfile()).into(vhItem.profile);
        vhItem.name.setText(post.getName());
        vhItem.about.setText(context.getString(R.string.about, post.getDate(), post.getVisibility()));

        try {
            int pinCount = Integer.parseInt(post.getPins());
            vhItem.pins.setText(res.getQuantityString(R.plurals.pins, pinCount, pinCount));
        } catch (NumberFormatException e) {
            vhItem.pins.setText(res.getQuantityString(R.plurals.pins, -1, post.getPins()));
        }

        for (Post.Description description : post.getDescriptions()) {
            switch (description.getType()) {
                case Post.Description.TYPE_PARAGRAPH:
                    TextView paragraph = (TextView) LayoutInflater.from(vhItem.description.getContext())
                            .inflate(R.layout.description_paragraph, vhItem.description, false);
                    vhItem.description.removeAllViewsInLayout();
                    vhItem.description.addView(paragraph);
                    paragraph.setText(description.getBody());
                    break;
                case Post.Description.TYPE_IMAGE:
                    ImageView image = (ImageView) LayoutInflater.from(vhItem.description.getContext())
                            .inflate(R.layout.description_image, vhItem.description, false);
                    vhItem.description.removeAllViewsInLayout();
                    vhItem.description.addView(image);
                    Glide.with(context).load(description.getBody()).into(image);
                    break;
                default:
                    Log.e(TAG, String.format("Unknown description type: %d", description.getType()));
            }
        }

        final PopupMenu otherMenu = new PopupMenu(context, vhItem.other);
        otherMenu.getMenuInflater().inflate(R.menu.menu_other, otherMenu.getMenu());

        // Force show icon
        try {
            Field fieldPopup = otherMenu.getClass().getDeclaredField("mPopup");
            fieldPopup.setAccessible(true);
            MenuPopupHelper mPopup = (MenuPopupHelper) fieldPopup.get(otherMenu);
            mPopup.setForceShowIcon(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        vhItem.other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherMenu.show();
            }
        });

        otherMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        getRef(position).removeValue();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public static class VH extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VH.class);

        final LinearLayout description;
        final TextView name;
        final ImageView profile;

        public VH(View v) {
            super(v);
            description = (LinearLayout) v.findViewById(R.id.description);
            name = (TextView) v.findViewById(R.id.name);
            profile = (ImageView) v.findViewById(R.id.profile);

            UIUtils.grayscale(profile);
        }
    }

    public static class VHHeader extends VH {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VHItem.class);

        final TextView date;
        final Button post;
        final Spinner visibility;

        public VHHeader(View v, Context context) {
            super(v);
            date = (TextView) v.findViewById(R.id.date);
            post = (Button) v.findViewById(R.id.post);
            visibility = (Spinner) v.findViewById(R.id.visibility);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.visibility, R.layout.spinner_visibility);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            visibility.setAdapter(adapter);

        }
    }

    public static class VHItem extends VH {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VHItem.class);

        final TextView about;
        final Button pins;
        final LinearLayout description;
        final ToggleButton heart, code, basket;
        final AppCompatImageButton other, more;

        public VHItem(View v, Context context) {
            super(v);
            about = (TextView) v.findViewById(R.id.about);
            pins = (Button) v.findViewById(R.id.pins);
            description = (LinearLayout) v.findViewById(R.id.description);
            heart = (ToggleButton) v.findViewById(R.id.heart);
            code = (ToggleButton) v.findViewById(R.id.code);
            basket = (ToggleButton) v.findViewById(R.id.basket);
            other = (AppCompatImageButton) v.findViewById(R.id.other);
            more = (AppCompatImageButton) v.findViewById(R.id.more);

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
}
package com.veyndan.generic;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
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
import com.veyndan.generic.util.LogUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends FirebaseAdapterRecyclerAdapter<Note, HomeAdapter.VH> {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(HomeAdapter.class);

    private final FragmentActivity context;
    private final MenuAttach listener;
    private final Resources res;
    private final Firebase rootRef;

    public HomeAdapter(FragmentActivity context, Firebase rootRef, MenuAttach listener) {
        super(Note.class, rootRef);
        this.context = context;
        this.rootRef = rootRef;
        this.listener = listener;
        this.res = context.getResources();
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
        return new VHContent(v, context);
    }

    @Override
    protected void onBindHeaderItemViewHolder(VH holder, final int position) {
        final VHHeader vhHeader = (VHHeader) holder;
        Glide.with(context).load("https://scontent-lhr3-1.xx.fbcdn.net/hphotos-frc3/v/" +
                "t1.0-9/1098101_1387041911520027_1668446817_n.jpg?oh=" +
                "85cb27b32003fb5080e73e18d03bbbc4&oe=574FB4F9").into(vhHeader.profile);
        vhHeader.name.setText("Veyndan Stuart");
        vhHeader.date.setText(context.getString(R.string.date, "Now"));

        vhHeader.description.removeAllViewsInLayout();

        final EditText paragraph = (EditText) LayoutInflater.from(vhHeader.description.getContext())
                .inflate(R.layout.description_paragraph_new, vhHeader.description, false);
        vhHeader.description.addView(paragraph);

        vhHeader.post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Note.Description> descriptions = new ArrayList<>();
                for (int i = 0; i < vhHeader.description.getChildCount(); i++) {
                    View child = vhHeader.description.getChildAt(i);
                    if (child instanceof EditText) {
                        descriptions.add(new Note.Description(
                                ((EditText) child).getText().toString(),
                                Note.Description.TYPE_PARAGRAPH
                        ));
                    }
                }
                rootRef.push().setValue(new Note(
                        vhHeader.name.getText().toString(),
                        "Now",
                        vhHeader.visibility.getSelectedItem().toString(),
                        "0",
                        "https://scontent-lhr3-1.xx.fbcdn.net/hphotos-frc3/v/t1.0-9/1098101_" +
                                "1387041911520027_1668446817_n.jpg?oh=" +
                                "85cb27b32003fb5080e73e18d03bbbc4&oe=574FB4F9",
                        descriptions
                ));
                paragraph.setText(null);
            }
        });

        final PopupMenu otherMenu = new PopupMenu(context, vhHeader.attach);
        otherMenu.getMenuInflater().inflate(R.menu.menu_attach, otherMenu.getMenu());

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

        vhHeader.attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherMenu.show();
            }
        });

        otherMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_picture:
//                        listener.menuAttachPhoto();
                        BottomSheetDialogFragment bottomSheetDialogFragment = new AttachPhotoBottomSheetDialogFragment();
                        bottomSheetDialogFragment.show(context.getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    protected void onBindContentItemViewHolder(VH holder, final int position) {
        VHContent vhContent = (VHContent) holder;
        Note note = getItem(position);
        Glide.with(context).load(note.getProfile()).into(vhContent.profile);
        vhContent.name.setText(note.getName());
        vhContent.about.setText(context.getString(R.string.about, note.getDate(), note.getVisibility()));

        try {
            int pinCount = Integer.parseInt(note.getPins());
            vhContent.pins.setText(res.getQuantityString(R.plurals.pins, pinCount, pinCount));
        } catch (NumberFormatException e) {
            vhContent.pins.setText(res.getQuantityString(R.plurals.pins, -1, note.getPins()));
        }

        for (Note.Description description : note.getDescriptions()) {
            switch (description.getType()) {
                case Note.Description.TYPE_PARAGRAPH:
                    TextView paragraph = (TextView) LayoutInflater.from(vhContent.description.getContext())
                            .inflate(R.layout.description_paragraph, vhContent.description, false);
                    vhContent.description.removeAllViewsInLayout();
                    vhContent.description.addView(paragraph);
                    paragraph.setText(description.getBody());
                    break;
                case Note.Description.TYPE_IMAGE:
                    ImageView image = (ImageView) LayoutInflater.from(vhContent.description.getContext())
                            .inflate(R.layout.description_image, vhContent.description, false);
                    vhContent.description.removeAllViewsInLayout();
                    vhContent.description.addView(image);
                    Glide.with(context).load(description.getBody()).into(image);
                    break;
                default:
                    Log.e(TAG, String.format("Unknown description type: %d", description.getType()));
            }
        }

        final PopupMenu otherMenu = new PopupMenu(context, vhContent.other);
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

        vhContent.other.setOnClickListener(new View.OnClickListener() {
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
        }
    }

    public static class VHHeader extends VH {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VHContent.class);

        final TextView date;
        final Button post;
        final Spinner visibility;
        final AppCompatImageButton attach;

        public VHHeader(View v, Context context) {
            super(v);
            date = (TextView) v.findViewById(R.id.date);
            post = (Button) v.findViewById(R.id.post);
            visibility = (Spinner) v.findViewById(R.id.visibility);
            attach = (AppCompatImageButton) v.findViewById(R.id.attach);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.visibility, R.layout.spinner_visibility);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            visibility.setAdapter(adapter);
        }
    }

    public static class VHContent extends VH {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VHContent.class);

        final TextView about;
        final Button pins;
        final LinearLayout description;
        final ToggleButton heart, code, basket;
        final AppCompatImageButton other, more;

        public VHContent(View v, Context context) {
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
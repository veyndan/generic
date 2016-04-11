package com.veyndan.generic.home;

import android.content.res.Resources;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.veyndan.generic.R;
import com.veyndan.generic.attach.PhotosFragment;
import com.veyndan.generic.home.data.FirebaseAdapterRecyclerAdapter;
import com.veyndan.generic.util.LogUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeAdapter extends FirebaseAdapterRecyclerAdapter<Note, HomeAdapter.VH> {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(HomeAdapter.class);

    private final ScrollRecyclerView scrollRecyclerView;
    private final FragmentActivity context;
    private final Resources res;
    private final Firebase rootRef;

    public HomeAdapter(ScrollRecyclerView scrollRecyclerView, FragmentActivity context, Firebase rootRef) {
        super(Note.class, rootRef);
        this.scrollRecyclerView = scrollRecyclerView;
        this.context = context;
        this.rootRef = rootRef;
        this.res = context.getResources();
    }

    @Override
    protected VH onCreateHeaderItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_header, parent, false);
        return new VHHeader(v);
    }

    @Override
    protected VH onCreateContentItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content, parent, false);
        return new VHContent(v);
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

        vhHeader.post.setOnClickListener(v -> {
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

        vhHeader.attach.setOnClickListener(v -> otherMenu.show());

        otherMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_picture:
                    BottomSheetDialogFragment bottomSheetDialogFragment = new PhotosFragment();
                    bottomSheetDialogFragment.show(context.getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    protected void onBindContentItemViewHolder(VH holder, final int position) {
        VHContent vhContent = (VHContent) holder;
        final Note note = getItem(position);
        Glide.with(context).load(note.getProfile()).into(vhContent.profile);
        vhContent.name.setText(note.getName());
        vhContent.about.setText(context.getString(R.string.about, note.getDate(), note.getVisibility()));

        try {
            int pinCount = Integer.parseInt(note.getPins());
            vhContent.notes.setText(res.getQuantityString(R.plurals.notes, pinCount, pinCount));
        } catch (NumberFormatException e) {
            vhContent.notes.setText(res.getQuantityString(R.plurals.notes, -1, note.getPins()));
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
        //noinspection TryWithIdenticalCatches
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

        vhContent.other.setOnClickListener(v -> otherMenu.show());

        otherMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    getRef(position).removeValue();
                    return true;
                default:
                    return false;
            }
        });

        vhContent.notes.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            Log.d(TAG, String.valueOf(v.isSelected()));
            if (v.isSelected()) {
                vhContent.subNotes.setVisibility(View.VISIBLE);
                vhContent.itemView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                scrollRecyclerView.scrollBy(vhContent.itemView.getTop());
            } else {
                vhContent.subNotes.setVisibility(View.GONE);
                vhContent.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VH.class);

        @Bind(R.id.description) LinearLayout description;
        @Bind(R.id.name) TextView name;
        @Bind(R.id.profile) ImageView profile;

        public VH(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    static class VHHeader extends VH {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VHContent.class);

        @Bind(R.id.date) TextView date;
        @Bind(R.id.post) Button post;
        @Bind(R.id.visibility) Spinner visibility;
        @Bind(R.id.attach) AppCompatImageButton attach;

        public VHHeader(View v) {
            super(v);
            ButterKnife.bind(this, v);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(v.getContext().getApplicationContext(),
                    R.array.visibility, R.layout.spinner_visibility);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            visibility.setAdapter(adapter);
        }
    }

    public static class VHContent extends VH {
        @SuppressWarnings("unused")
        private static final String TAG = LogUtils.makeLogTag(VHContent.class);

        @Bind(R.id.about) TextView about;
        @Bind(R.id.notes) Button notes;
        @Bind(R.id.other) AppCompatImageButton other;
        @Bind(R.id.sub_notes) View subNotes;

        public VHContent(final View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

}
package com.veyndan.generic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.veyndan.generic.home.DividerItemDecoration;
import com.veyndan.generic.home.HomeAdapter;
import com.veyndan.generic.home.Note;
import com.veyndan.generic.util.LogUtils;

import java.lang.reflect.Field;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class NoteActivityFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = LogUtils.makeLogTag(NoteActivityFragment.class);

    @Bind(R.id.description) LinearLayout description;
    @Bind(R.id.name) TextView name;
    @Bind(R.id.profile) ImageView profile;
    @Bind(R.id.about) TextView about;
    @Bind(R.id.notes) Button notes;
    @Bind(R.id.other) AppCompatImageButton other;
    @Bind(R.id.comments) RecyclerView comments;

    public NoteActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        ButterKnife.bind(this, view);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            Note note = intent.getParcelableExtra("NOTE");
            Glide.with(this).load(note.getProfile()).into(profile);
            name.setText(note.getName());
            about.setText(getActivity().getString(R.string.about, note.getDate(), note.getVisibility()));

            try {
                int pinCount = Integer.parseInt(note.getPins());
                notes.setText(getResources().getQuantityString(R.plurals.notes, pinCount, pinCount));
            } catch (NumberFormatException e) {
                notes.setText(getResources().getQuantityString(R.plurals.notes, -1, note.getPins()));
            }

            for (Note.Description descrip : note.getDescriptions()) {
                switch (descrip.getType()) {
                    case Note.Description.TYPE_PARAGRAPH:
                        TextView paragraph = (TextView) LayoutInflater.from(description.getContext())
                                .inflate(R.layout.description_paragraph, description, false);
                        description.removeAllViewsInLayout();
                        description.addView(paragraph);
                        paragraph.setText(descrip.getBody());
                        break;
                    case Note.Description.TYPE_IMAGE:
                        ImageView image = (ImageView) LayoutInflater.from(description.getContext())
                                .inflate(R.layout.description_image, description, false);
                        description.removeAllViewsInLayout();
                        description.addView(image);
                        Glide.with(this).load(descrip.getBody()).into(image);
                        break;
                    default:
                        Log.e(TAG, String.format("Unknown description type: %d", descrip.getType()));
                }
            }

            final PopupMenu otherMenu = new PopupMenu(getActivity(), other);
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

            other.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    otherMenu.show();
                }
            });

            comments.setLayoutManager(new LinearLayoutManager(getContext()));
            comments.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

            HomeAdapter adapter = new HomeAdapter(getActivity(), new Firebase("https://sweltering-heat-8337.firebaseio.com"));
            comments.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

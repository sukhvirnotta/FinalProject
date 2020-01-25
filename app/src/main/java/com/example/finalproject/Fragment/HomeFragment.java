/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

HomeFragment class represents a part of MainActivity's user interface
*/

package com.example.finalproject.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.finalproject.Adapter.PostAdapter;
import com.example.finalproject.Model.Post;
import com.example.finalproject.R;
import com.example.finalproject.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    EditText search;
    TextView noResults;
    SharedPref sharedPref;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<String> followingList;

    // Instantiate fragments user interface view
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        // check shared preferences and set app theme
        if (sharedPref.loadNightMode()) {
            getContext().getTheme().applyStyle(R.style.DarkTheme, true);

        } else {
            getContext().getTheme().applyStyle(R.style.LightTheme, true);

        }
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // retrieve view with matching id
        recyclerView = view.findViewById(R.id.recyclerview);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        // reverse item traversal and layout order
        mLayoutManager.setReverseLayout(true);
        // list fills content starting from end of view
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        progressBar = view.findViewById(R.id.progress_circular);

        search = view.findViewById(R.id.text_search);
        noResults = view.findViewById(R.id.text_noresult);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        getFollowingList();


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // call searchPhotosList method when search text changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    getFollowingList();
                } else {
                    searchPhotosList(charSequence.toString().toLowerCase());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    // Update postList from search query
    private void searchPhotosList(String s) {
        // match all posts where description starts with s in ascending order
        // character \uf8ff is high code point in Unicode range
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("description")
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            // Update postList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : followingList) {
                        if (Objects.requireNonNull(post).getPublisher().equals(id)) {
                            postList.add(post);
                        }
                    }
                }
                // display "no results" if postList is empty
                if (postList.isEmpty()) {
                    noResults.setVisibility(View.VISIBLE);
                } else {
                    noResults.setVisibility(View.GONE);
                }
                Collections.reverse(postList);
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Update list of users that current users is following
    private void getFollowingList() {
        followingList = new ArrayList<>();
        // reference to following in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            // update followingList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }
                getPostList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Update list of posts
    private void getPostList() {
        noResults.setVisibility(View.GONE);
        // reference to posts in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            // update postList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search.getText().toString().equals("")) {
                    postList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        for (String id : followingList) {
                            if (Objects.requireNonNull(post).getPublisher().equals(id)) {
                                postList.add(post);
                            }
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

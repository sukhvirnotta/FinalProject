/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

PhotoFragment class represents a part of MainActivity's user interface
*/

package com.example.finalproject.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalproject.Adapter.PostAdapter;
import com.example.finalproject.Model.Post;
import com.example.finalproject.R;
import com.example.finalproject.SharedPref;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PhotoFragment extends Fragment {

    String postid;
    SharedPref sharedPref;
    private PostAdapter postAdapter;
    private List<Post> postList;

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
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postid = sharedPreferences.getString("postid", "none");

        // retrieve view with matching id
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        getPostList();

        return view;
    }

    // Update list of posts to current post
    private void getPostList() {
        // reference to posts in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            // update postList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                Post post = dataSnapshot.getValue(Post.class);
                postList.add(post);

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

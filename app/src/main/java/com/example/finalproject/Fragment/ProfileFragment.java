/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

ProfileFragment class represents a part of MainActivity's user interface
*/

package com.example.finalproject.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Adapter.MyPhotoAdapter;
import com.example.finalproject.EditProfileActivity;
import com.example.finalproject.FollowersActivity;
import com.example.finalproject.Model.Post;
import com.example.finalproject.Model.User;
import com.example.finalproject.R;
import com.example.finalproject.SettingsActivity;
import com.example.finalproject.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ProfileFragment extends Fragment {

    ImageView image_profile, settings;
    View view1, view2;
    TextView followers, following, username, fullname, bio, noResults;
    Button follow;
    EditText search;
    LinearLayout layout_followers, layout_following, layout_numbers;
    FirebaseUser firebaseUser;
    String profileid;
    SharedPref sharedPref;
    private MyPhotoAdapter myPhotoAdapter;
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // get current user signed in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = sharedPreferences.getString("profileid", "none");

        // retrieve view with matching id
        image_profile = view.findViewById(R.id.image_profile);
        view1 = view.findViewById(R.id.view1);
        view2 = view.findViewById(R.id.view2);
        settings = view.findViewById(R.id.image_settings);
        followers = view.findViewById(R.id.text_followers);
        following = view.findViewById(R.id.text_following);
        username = view.findViewById(R.id.text_username);
        fullname = view.findViewById(R.id.text_fullname);
        bio = view.findViewById(R.id.text_bio);
        follow = view.findViewById(R.id.button_follow);
        layout_followers = view.findViewById(R.id.layout_followers);
        layout_following = view.findViewById(R.id.layout_following);
        layout_numbers = view.findViewById(R.id.layout_second);
        search = view.findViewById(R.id.text_search);
        noResults = view.findViewById(R.id.text_noresult);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getContext(), postList);
        recyclerView.setAdapter(myPhotoAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        getUserInfo();
        getFollowers();
        getPostList();

        if (profileid.equals(firebaseUser.getUid())) {
            // set button to edit if current profile matches current user
            follow.setText("Edit");
        } else {
            // hide settings button if current profile doesn't match current user
            settings.setVisibility(View.GONE);
            layout_numbers.setVisibility(View.GONE);
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            isFollowing();
        }

        follow.setOnClickListener(new View.OnClickListener() {
            // Update followers/following in database when view is clicked
            @Override
            public void onClick(View v) {
                String btn = follow.getText().toString();

                if (btn.equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else if (btn.equals("Following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                } else if (btn.equals("Edit")) {
                    Intent intent = new Intent(getContext(), EditProfileActivity.class);
                    startActivity(intent);
                }
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            // Go to settings page when view is clicked
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        layout_followers.setOnClickListener(new View.OnClickListener() {
            // Go to followers page when view is clicked
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        layout_following.setOnClickListener(new View.OnClickListener() {
            // Go to following page when view is clicked
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // call searchPhotosList method when search text changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    getPostList();
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
                    if (Objects.requireNonNull(post).getPublisher().equals(profileid)) {
                        postList.add(post);
                    }
                }
                // display "no results" if postList is empty
                if (postList.isEmpty()) {
                    noResults.setVisibility(View.VISIBLE);
                } else {
                    noResults.setVisibility(View.GONE);
                }
                Collections.reverse(postList);
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Get user information of current profile
    private void getUserInfo() {
        // reference to user in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = dataSnapshot.getValue(User.class);

                // Bump Technologies (no date) glide: An image loading and caching library for Android focused on smooth scrolling. Available at: https://github.com/bumptech/glide.
                // load user profile image into ImageView
                Glide.with(getContext()).load(Objects.requireNonNull(user).getImageURL()).into(image_profile);
                // set full name and bio
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Check if current user is following profile
    private void isFollowing() {
        // reference to following in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            // Set button text from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
                    follow.setText("Following");
                } else {
                    follow.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Get number of followers and following of profile
    private void getFollowers() {
        // reference to followers in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("followers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            // Set text from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // reference to following in datbase
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("following");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            // Set text from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText("" + dataSnapshot.getChildrenCount());
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
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (Objects.requireNonNull(post).getPublisher().equals(profileid)) {
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

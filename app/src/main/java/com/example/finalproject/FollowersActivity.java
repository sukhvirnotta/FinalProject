/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

FollowersActivity class represents one screen of app's user interface
*/

package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.finalproject.Adapter.UserAdapter;
import com.example.finalproject.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowersActivity extends AppCompatActivity {

    String id, title;
    List<String> idList;
    TextView toolbarTitle, noResults;

    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<User> userList;
    SharedPref sharedPref;

    // Set up activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        // check shared preferences and set app theme
        if (sharedPref.loadNightMode()) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
        super.onCreate(savedInstanceState);
        // define layout for activity's user interface
        setContentView(R.layout.activity_followers);

        // set page title
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        // retrieve view with matching id
        toolbarTitle = findViewById(R.id.text_toolbar);
        toolbarTitle.setText(title);
        noResults = findViewById(R.id.text_noresult);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, false);
        recyclerView.setAdapter(userAdapter);

        idList = new ArrayList<>();

        // select title of page
        switch (title) {
            case "Likes":
                getLikes();
                break;
            case "Following":
                getFollowing();
                break;
            case "Followers":
                getFollowers();
                break;
        }
    }

    // Get list of user ids that have liked specific post
    private void getLikes() {
        // reference to id in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            // update idList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(snapshot.getKey());
                }
                getUserList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Get list of user ids that specific user is following
    private void getFollowing() {
        // reference to following in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(id).child("following");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            // update idList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(snapshot.getKey());
                }
                getUserList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // Get list of user ids that follow specific user
    private void getFollowers() {
        // reference to followers in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(id).child("followers");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            // update idList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    idList.add(snapshot.getKey());
                }
                getUserList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Update list of users
    private void getUserList() {
        // reference to users in database, order by username in ascending order
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            // update userList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (String id : idList) {
                        if (Objects.requireNonNull(user).getId().equals(id)) {
                            userList.add(user);
                        }
                    }
                }
                // display "no results" if postList is empty
                if (userList.isEmpty()) {
                    noResults.setVisibility(View.VISIBLE);
                } else {
                    noResults.setVisibility(View.GONE);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

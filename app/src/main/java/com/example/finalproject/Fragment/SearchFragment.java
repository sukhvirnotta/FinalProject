/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

SearchFragment class represents a part of MainActivity's user interface
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
import android.widget.TextView;

import com.example.finalproject.Adapter.UserAdapter;
import com.example.finalproject.Model.User;
import com.example.finalproject.R;
import com.example.finalproject.SharedPref;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment {

    RecyclerView recyclerView;
    EditText search;
    TextView noResults;
    SharedPref sharedPref;
    private UserAdapter userAdapter;
    private List<User> userList;

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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // retrieve view with matching id
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search = view.findViewById(R.id.text_search);
        noResults = view.findViewById(R.id.text_noresult);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList, true);
        recyclerView.setAdapter(userAdapter);

        getUserList();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // call searchUserList method when search text changed
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    getUserList();
                }
                else {
                    searchUserList(charSequence.toString().toLowerCase());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    // Update userList from search query
    private void searchUserList(String s) {
        // match all users where username starts with s in ascending order
        // character \uf8ff is high code point in Unicode range
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            // Update userList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
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

    // Update list of users
    private void getUserList() {
        noResults.setVisibility(View.GONE);
        // reference to users in database, order by username in ascending order
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username");
        query.addValueEventListener(new ValueEventListener() {
            // update userList
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search.getText().toString().equals("")) {
                    userList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        userList.add(user);

                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
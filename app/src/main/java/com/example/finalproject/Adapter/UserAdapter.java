/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

UserAdapter class shows implementation for data set that consists of list of users displayed using View widgets
*/

package com.example.finalproject.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Fragment.ProfileFragment;
import com.example.finalproject.Model.User;
import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ImageViewHolder> {

    private Context context;
    private List<User> userList;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    // Constructor
    public UserAdapter(Context context, List<User> userList, boolean isFragment) {
        this.context = context;
        this.userList = userList;
        this.isFragment = isFragment;
    }

    // Create new views, invoked by layout manager
    @NonNull
    @Override
    public UserAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ImageViewHolder(view);
    }

    // Replace content of view, invoked by layout manager
    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ImageViewHolder imageViewHolder, final int position) {
        // get current user signed in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // get user from list at this position
        final User user = userList.get(position);

        imageViewHolder.follow.setVisibility(View.VISIBLE);
        isFollowing(user.getId(), imageViewHolder.follow);

        // set username and fullname
        imageViewHolder.username.setText(user.getUsername());
        imageViewHolder.fullname.setText(user.getFullname());

        // Bump Technologies (no date) glide: An image loading and caching library for Android focused on smooth scrolling. Available at: https://github.com/bumptech/glide.
        // load user profile image into ImageView
        Glide.with(context).load(user.getImageURL()).into(imageViewHolder.image_profile);

        if (user.getId().equals(firebaseUser.getUid())) {
            // hide follow button if same user
            imageViewHolder.follow.setVisibility(View.GONE);
        }

        imageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            // Go to matching profile page when profile image view is clicked
            @Override
            public void onClick(View view) {
                if (isFragment) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getId());
                    editor.apply();

                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).addToBackStack(null).commit();
                }

            }
        });

        imageViewHolder.follow.setOnClickListener(new View.OnClickListener() {
            // Update followers/following in database when view is clicked
            @Override
            public void onClick(View view) {
                if (imageViewHolder.follow.getText().toString().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }

        });
    }

    // Return size of list, invoked by layout manager
    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Check if user is followed by current user signed in
    private void isFollowing(final String userid, final Button button) {

        // get current user signed in
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // reference to following in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(Objects.requireNonNull(firebaseUser).getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            // Set button text from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.setText("Following");
                } else {
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Provide a reference to the views for each data item
    public class ImageViewHolder extends RecyclerView.ViewHolder {

        // each data item is a set of text, images and
        public TextView username, fullname;
        // Henning Dodenhof (no date) CircleImageView: A circular ImageView for Android. Available at: https://github.com/hdodenhof/CircleImageView.
        public CircleImageView image_profile;
        public Button follow;

        public ImageViewHolder(View itemView) {
            super(itemView);

            // retrieve view with matching id
            username = itemView.findViewById(R.id.text_username);
            fullname = itemView.findViewById(R.id.text_fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            follow = itemView.findViewById(R.id.button_follow);
        }
    }
}
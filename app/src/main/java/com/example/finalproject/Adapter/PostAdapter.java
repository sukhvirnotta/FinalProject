/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

PostAdapter class shows implementation for data set that consists of list of posts displayed using View widgets
*/

package com.example.finalproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalproject.FollowersActivity;
import com.example.finalproject.Fragment.PhotoFragment;
import com.example.finalproject.Fragment.ProfileFragment;
import com.example.finalproject.Model.Post;
import com.example.finalproject.Model.User;
import com.example.finalproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder> {

    private Context context;
    private List<Post> postList;

    private FirebaseUser firebaseUser;

    // Constructor
    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    // Create new views, invoked by layout manager
    @NonNull
    @Override
    public PostAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ImageViewHolder(view);
    }

    // Replace content of view, invoked by layout manager
    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ImageViewHolder imageViewHolder, int i) {
        // get current user signed in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // get post from list at this position
        final Post post = postList.get(i);

        // Bump Technologies (no date) glide: An image loading and caching library for Android focused on smooth scrolling. Available at: https://github.com/bumptech/glide.
        // load post image into ImageView
        Glide.with(context).load(post.getPostimage()).fitCenter().into(imageViewHolder.image_post);

        publisherInfo(imageViewHolder.username, post.getPublisher());
        isLike(post.getPostid(), imageViewHolder.like);

        imageViewHolder.username.setOnClickListener(new View.OnClickListener() {
            // Go to matching profile page when view is clicked
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).addToBackStack(null).commit();
            }
        });

        imageViewHolder.image_post.setOnClickListener(new View.OnClickListener() {
            // Go to matching photo page when view is clicked
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container, new PhotoFragment()).addToBackStack(null).commit();
            }
        });

        if (post.getPublisher().equals(firebaseUser.getUid())) {
            // hide like button if post is from same user
            imageViewHolder.like.setVisibility(View.GONE);
        }
        else {
            imageViewHolder.like.setOnClickListener(new View.OnClickListener() {
                // Update likes in database when view is clicked
                @Override
                public void onClick(View v) {
                    if (imageViewHolder.like.getTag().equals("like")) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                                .child(firebaseUser.getUid()).setValue(true);
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                                .child(firebaseUser.getUid()).removeValue();
                    }
                }
            });
        }

        if (!post.getPublisher().equals(firebaseUser.getUid())) {
            // hide like text if post is not from same user
            imageViewHolder.likes.setVisibility(View.GONE);
        }
        else {
            imageViewHolder.likes.setOnClickListener(new View.OnClickListener() {
                // Go to matching likes page when view is clicked
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FollowersActivity.class);
                    intent.putExtra("id", post.getPostid());
                    intent.putExtra("title", "Likes");
                    context.startActivity(intent);
                }
            });
        }

        if (!post.getPublisher().equals(firebaseUser.getUid())) {
            // hide options button if post is not from same user
            imageViewHolder.options.setVisibility(View.GONE);
        } else {
            imageViewHolder.options.setOnClickListener(new View.OnClickListener() {
                // Create popup options menu for post
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    // delete post from database when item clicked
                                    FirebaseDatabase.getInstance().getReference("Posts")
                                            .child(post.getPostid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                // Notify user that post is deleted
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.inflate(R.menu.post_menu);
                    popupMenu.show();
                }

            });
        }
    }

    // Return size of list, invoked by layout manager
    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Check if post is liked by current user signed in
    private void isLike(String postid, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // reference to likes in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            // Set like image and tag from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Objects.requireNonNull(firebaseUser).getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Get publisher information on a post
    private void publisherInfo(final TextView username, final String userid) {
        // reference to users in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            // Set profile image and username text from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(Objects.requireNonNull(user).getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Provide a reference to the views for each data item
    public class ImageViewHolder extends RecyclerView.ViewHolder {

        // each data item is a set of images and text
        public ImageView image_post, like, options;
        public TextView username, likes;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // retrieve view with matching id
            image_post = itemView.findViewById(R.id.image_post);
            like = itemView.findViewById(R.id.image_like);
            username = itemView.findViewById(R.id.text_username);
            likes = itemView.findViewById(R.id.text_likes);
            options = itemView.findViewById(R.id.image_options);
        }
    }
}

/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

MyPhotoAdapter class shows implementation for data set that consists of list of posts displayed using View widgets
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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalproject.Fragment.PhotoFragment;
import com.example.finalproject.Model.Post;
import com.example.finalproject.R;

import java.util.List;

public class MyPhotoAdapter extends RecyclerView.Adapter<MyPhotoAdapter.ImageViewHolder> {

    private Context context;
    private List<Post> postList;

    // Constructor
    public MyPhotoAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    // Create new views, invoked by layout manager
    @NonNull
    @Override
    public MyPhotoAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(context).inflate(R.layout.photos_item, parent, false);
        return new MyPhotoAdapter.ImageViewHolder(view);
    }

    // Replace content of view, invoked by layout manager
    @Override
    public void onBindViewHolder(@NonNull final MyPhotoAdapter.ImageViewHolder holder, final int position) {
        // get post from list at this position
        final Post post = postList.get(position);

        // Bump Technologies (no date) glide: An image loading and caching library for Android focused on smooth scrolling. Available at: https://github.com/bumptech/glide.
        // load post image into ImageView
        Glide.with(context).load(post.getPostimage()).fitCenter().into(holder.post_image);

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            // Go to matching photo page when image view is clicked
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container, new PhotoFragment()).addToBackStack(null).commit();
            }
        });

    }

    // Return size of list, invoked by layout manager
    @Override
    public int getItemCount() {
        return postList.size();
    }


    // Provide a reference to the views for each data item
    public class ImageViewHolder extends RecyclerView.ViewHolder {

        // each data item is an image
        public ImageView post_image;


        public ImageViewHolder(View itemView) {
            super(itemView);
            // retrieve view with matching id
            post_image = itemView.findViewById(R.id.post_image);

        }
    }
}
/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

EditProfileActivity class represents one screen of app's user interface
*/

package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.finalproject.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close, image_profile, save;
    TextView change;
    // Kai Zhu (no date) MaterialEditText: EditText in Material Design. Available at: https://github.com/rengwuxian/MaterialEditText.
    MaterialEditText fullname, bio;

    FirebaseUser firebaseUser;
    StorageReference storageRef;
    SharedPref sharedPref;
    private Uri imageUri;

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
        setContentView(R.layout.activity_edit_profile);

        // retrieve view with matching id
        close = findViewById(R.id.image_close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.image_save);
        change = findViewById(R.id.text_changephoto);
        fullname = findViewById(R.id.edit_fullname);
        bio = findViewById(R.id.edit_bio);

        // get current user signed in
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        // reference to user in database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            // set username, full name, bio and profile image from database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                fullname.setText(Objects.requireNonNull(user).getFullname());
                bio.setText(user.getBio());

                // Bump Technologies (no date) glide: An image loading and caching library for Android focused on smooth scrolling. Available at: https://github.com/bumptech/glide.
                // load user profile image into ImageView
                Glide.with(getApplicationContext()).load(user.getImageURL()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            // activity is done and should be closed when view is clicked
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            // call updateProfile method when view is clicked
            @Override
            public void onClick(View view) {
                updateProfile(Objects.requireNonNull(fullname.getText()).toString(), Objects.requireNonNull(bio.getText()).toString());
            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            // start picker to get image for cropping then use the image in cropping activity when view is clicked
            @Override
            public void onClick(View view) {
                // Arthur (no date) Android-Image-Cropper: Image Cropping Library for Android, optimized for Camera/Gallery. Available at: https://github.com/ArthurHub/Android-Image-Cropper.
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditProfileActivity.this);
            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            // start picker to get image for cropping then use the image in cropping activity when view is clicked
            @Override
            public void onClick(View view) {
                // Arthur (no date) Android-Image-Cropper: Image Cropping Library for Android, optimized for Camera/Gallery. Available at: https://github.com/ArthurHub/Android-Image-Cropper.
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditProfileActivity.this);
            }
        });
    }

    // Update username, full name and bio of user profile
    private void updateProfile(final String fullname, final String bio) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());

        // create HashMap to store string as key and object as value, update database
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullname", fullname);
        hashMap.put("bio", bio);

        databaseReference.updateChildren(hashMap);
    }


    // Return file extension for given uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Upload user profile image
    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();
        if (imageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            StorageTask uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                // Update image of user profile
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = Objects.requireNonNull(downloadUri).toString();

                        // reference to user in database
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        // create HashMap to store string as key and object as value, update database
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL", "" + url);
                        databaseReference.updateChildren(hashMap);

                        progressDialog.dismiss();

                    } else {
                        // notify user of error
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // notify user of error
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // notify user of error
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Called when launched activity exits
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            uploadImage();

        } else {
            // notify user of error
            Toast.makeText(this, "Oops! Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
}
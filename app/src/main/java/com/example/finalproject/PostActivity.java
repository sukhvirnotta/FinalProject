/*
File based on tutorials:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.
Firebase (no date) Label Images with ML Kit on Android. Available at: https://firebase.google.com/docs/ml-kit/android/label-images.

PostActivity class represents one screen of app's user interface
*/

package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    Uri uri;
    String str_url = "";
    StorageTask storageTask;
    StorageReference storageReference;

    ImageView close, added_img, post;
    String description;
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
        setContentView(R.layout.activity_post);

        // retrieve view with matching id
        close = findViewById(R.id.image_close);
        added_img = findViewById(R.id.image_added_img);
        post = findViewById(R.id.image_post);

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(new View.OnClickListener() {
            // Go to main page when button is clicked
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            // Call uploadImage method when button is clicked
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        // Arthur (no date) Android-Image-Cropper: Image Cropping Library for Android, optimized for Camera/Gallery. Available at: https://github.com/ArthurHub/Android-Image-Cropper.
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(PostActivity.this);
    }

    // Return file extension for given uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Upload post image
    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (uri != null) {
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

            storageTask = reference.putFile(uri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        str_url = Objects.requireNonNull(downloadUri).toString();

                        // reference to posts in database
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference1.push().getKey();

                        // create HashMap to store string as key and object as value, update database
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid", postid);
                        hashMap.put("postimage", str_url);
                        hashMap.put("description", description.toLowerCase());
                        hashMap.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        reference1.child(Objects.requireNonNull(postid)).setValue(hashMap);
                        progressDialog.dismiss();

                        // go to main page after post added
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // notify user of error
                        Toast.makeText(PostActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // notify user of error
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // notify user of error
            Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Called when launched activity exits
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            uri = Objects.requireNonNull(activityResult).getUri();

            added_img.setImageURI(uri);

            // convert image to bitmap
            Bitmap bitmap = ((BitmapDrawable) added_img.getDrawable()).getBitmap();

            // create FirebaseVisionImage object from Bitmap object
            FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);

            // get instance of on-device image labeler
            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

            // pass the image
            labeler.processImage(visionImage)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        // Image processed successfully
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                            try {
                                // get label's text description
                                description = firebaseVisionImageLabels.get(0).getText();
                            } catch (IndexOutOfBoundsException e) {
                                description = "Unlabelled";
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        // Image processing failed
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            description = "Unlabelled";
                        }
                    });
        } else {
            // go to main page
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }
}

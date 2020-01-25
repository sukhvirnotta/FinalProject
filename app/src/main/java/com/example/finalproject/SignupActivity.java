/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

SignupActivity class represents one screen of app's user interface
*/

package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    // Kai Zhu (no date) MaterialEditText: EditText in Material Design. Available at: https://github.com/rengwuxian/MaterialEditText.
    MaterialEditText username, fullname, bio, email, password;
    Button signup;
    TextView login;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
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
        setContentView(R.layout.activity_signup);

        // retrieve view with matching id
        username = findViewById(R.id.text_username);
        fullname = findViewById(R.id.text_fullname);
        bio = findViewById(R.id.text_bio);
        email = findViewById(R.id.text_email);
        password = findViewById(R.id.text_password);
        signup = findViewById(R.id.button_signup);
        login = findViewById(R.id.text_login);

        firebaseAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            // Go to login page when button is clicked
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            // Check entered details when button is clicked
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(SignupActivity.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                String str_username = Objects.requireNonNull(username.getText()).toString();
                String str_fullname = Objects.requireNonNull(fullname.getText()).toString();
                String str_bio = Objects.requireNonNull(bio.getText()).toString();
                String str_email = Objects.requireNonNull(email.getText()).toString();
                String str_password = Objects.requireNonNull(password.getText()).toString();

                if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_fullname) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                    progressDialog.dismiss();
                    // notify user to enter information
                    Toast.makeText(SignupActivity.this, "Enter your information.", Toast.LENGTH_SHORT).show();
                } else if (!isEmailValid(str_email)) {
                    progressDialog.dismiss();
                    // notify user to enter valid email
                    Toast.makeText(SignupActivity.this, "Enter a valid email address.", Toast.LENGTH_SHORT).show();
                } else if (str_password.length() < 8) {
                    progressDialog.dismiss();
                    // notify user to enter valid password
                    Toast.makeText(SignupActivity.this, "Use 8 characters or more for your password.", Toast.LENGTH_SHORT).show();
                } else {
                    // call signup method passing entered information
                    signup(str_username, str_fullname, str_bio, str_email, str_password, v);
                }
            }
        });

    }

    // Create a new account for user
    private void signup(final String username, final String fullname, final String bio, final String email, final String password, final View v) {
        // reference to users in database with matching username
        Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(username.toLowerCase());
        usernameQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // check if user with same username already exists
                if(dataSnapshot.getChildrenCount()>0) {
                    progressDialog.dismiss();
                    // notify user to enter unique username
                    Toast.makeText(SignupActivity.this, "We're sorry, that username is taken.", Toast.LENGTH_SHORT).show();
                } else {
                    // crete new account with entered email and password
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // get current user signed in
                                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                        String uid = Objects.requireNonNull(firebaseUser).getUid();
                                        // reference to user in database
                                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                        // create HashMap to store string as key and object as value, update database
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("id", uid);
                                        hashMap.put("username", username.toLowerCase());
                                        hashMap.put("fullname", fullname);
                                        hashMap.put("bio", bio);
                                        // default profile image
                                        hashMap.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/finalproject-fd68c.appspot.com/o/user.png?alt=media&token=92ba3cb8-a35a-44ff-a3e1-590a28603541");

                                        databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            // Go to main page once user has signed up successfully
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        // notify user to enter unique email
                                        Toast.makeText(SignupActivity.this, "We're sorry, that email is taken.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    // Exit application when back is pressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Check if email is valid
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

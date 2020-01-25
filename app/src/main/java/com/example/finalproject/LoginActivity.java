/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

LoginActivity class represents one screen of app's user interface
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // Kai Zhu (no date) MaterialEditText: EditText in Material Design. Available at: https://github.com/rengwuxian/MaterialEditText.
    MaterialEditText email, password;
    Button login;
    TextView signup;

    FirebaseAuth firebaseAuth;
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
        setContentView(R.layout.activity_login);

        // retrieve view with matching id
        email = findViewById(R.id.text_email);
        password = findViewById(R.id.text_password);
        login = findViewById(R.id.button_login);
        signup = findViewById(R.id.text_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            // Go to sign up page when button is clicked
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            // Check entered details when button is clicked
            @Override
            public void onClick(final View v) {
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                String str_email = Objects.requireNonNull(email.getText()).toString();
                String str_password = Objects.requireNonNull(password.getText()).toString();

                if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                    progressDialog.dismiss();
                    // notify user to enter information
                    Toast.makeText(LoginActivity.this, "Enter an email and password", Toast.LENGTH_SHORT).show();
                } else {
                    // sign in user with entered email and password
                    firebaseAuth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // reference to current user in database
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());

                                        databaseReference.addValueEventListener(new ValueEventListener() {
                                            // Go to main page once user is logged in successfully
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                progressDialog.dismiss();
                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        // notify user to enter correct information
                                        Toast.makeText(LoginActivity.this, "Wrong email or password. Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
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
}

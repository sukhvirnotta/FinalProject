/*
File based on tutorial:
KOD Dev (2018) Available at: https://www.youtube.com/playlist?list=PLzLFqCABnRQduspfbu2empaaY9BoIGLDM.

MainActivity class represents one screen of app's user interface
*/

package com.example.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.finalproject.Fragment.HomeFragment;
import com.example.finalproject.Fragment.ProfileFragment;
import com.example.finalproject.Fragment.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment fragment = null;
    SharedPref sharedPref;
    // bottom navigation bar for application
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                // Go to specific page when item selected in bottom navigation
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    // select page fragment or activity
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            fragment = new HomeFragment();
                            break;
                        case R.id.nav_search:
                            fragment = new SearchFragment();
                            break;
                        case R.id.nav_post:
                            fragment = null;
                            startActivity(new Intent(MainActivity.this, PostActivity.class));
                            break;
                        case R.id.nav_profile:
                            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            editor.putString("profileid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            editor.apply();
                            fragment = new ProfileFragment();
                            break;
                    }

                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                                fragment).commit();
                    }
                    return true;
                }
            };

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
        setContentView(R.layout.activity_main);

        // retrieve view with matching id
        bottomNavigationView = findViewById(R.id.bottomnav);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        // go to home fragment when activity is created
        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                new HomeFragment()).commit();
    }

    // Actions when back is pressed
    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();
        // exit application
        if (bottomNavigationView.getSelectedItemId() == R.id.nav_home) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        // go to home fragment
        else if (count == 0) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        // go to previous fragment/activity
        else {
            getSupportFragmentManager().popBackStack();
        }
    }
}

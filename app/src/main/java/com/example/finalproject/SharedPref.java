/*
File from tutorial:
Aws Rh (2017) Available at: https://www.youtube.com/watch?v=xqY7Yu5C8pg.

SharedPref class represents shared preference for night mode in application
*/

package com.example.finalproject;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private SharedPreferences sharedPref;

    // Constructor receives application context
    public SharedPref(Context context) {
        sharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    // Set the night mode state
    public void setNightMode(Boolean state) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    // Load the night mode state
    public Boolean loadNightMode() {
        return sharedPref.getBoolean("NightMode", false);
    }
}

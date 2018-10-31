package com.example.anti2110.instagramcloneapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.anti2110.instagramcloneapp.Home.HomeActivity;
import com.example.anti2110.instagramcloneapp.Likes.LikesActivity;
import com.example.anti2110.instagramcloneapp.Profile.ProfileActivity;
import com.example.anti2110.instagramcloneapp.R;
import com.example.anti2110.instagramcloneapp.Search.SearchActivity;
import com.example.anti2110.instagramcloneapp.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent = null;
                switch (menuItem.getItemId()) {
                    case R.id.ic_house:
                        intent = new Intent(context, HomeActivity.class);
                        break;
                    case R.id.ic_search:
                        intent = new Intent(context, SearchActivity.class);
                        break;
                    case R.id.ic_circle:
                        intent = new Intent(context, ShareActivity.class);
                        break;
                    case R.id.ic_alert:
                        intent = new Intent(context, LikesActivity.class);
                        break;
                    case R.id.ic_android:
                        intent = new Intent(context, ProfileActivity.class);
                        break;
                }
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                return false;
            }
        });
    }

}

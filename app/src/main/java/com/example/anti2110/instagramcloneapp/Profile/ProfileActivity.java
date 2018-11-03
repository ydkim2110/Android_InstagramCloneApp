package com.example.anti2110.instagramcloneapp.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.anti2110.instagramcloneapp.R;
import com.example.anti2110.instagramcloneapp.Utils.BottomNavigationViewHelper;
import com.example.anti2110.instagramcloneapp.Utils.GridImageAdapter;
import com.example.anti2110.instagramcloneapp.Utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS= 3;

    private Context mContext;

    private ProgressBar mProgressBar;
    private ImageView mProfilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: Started.");
        mContext = ProfileActivity.this;

        init();

//        setupToolbar();
//        setupBottomNavigationView();
//        setupActivityWidgets();
//        setProfileImage();
//        tempGridSetup();

    }

    private void init() {
        Log.d(TAG, "init: inflating: " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        //transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }


//    private void tempGridSetup() {
//        ArrayList<String> imageURLs = new ArrayList<>();
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/09/30/19/03/railway-station-3714297__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/18/19/02/woman-3757184__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/18/23/53/cactus-3757657__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/14/12/16/lago-federa-3746335__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/19/20/51/walnut-3759573__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/11/23/08/hahn-3741129__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/13/17/31/fall-leaves-3744649__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/15/18/32/bee-eaters-3749679__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/09/30/22/10/girl-3714828__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/07/10/24/sail-3729599__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/14/13/01/background-3746423__340.jpg");
//        imageURLs.add("https://cdn.pixabay.com/photo/2018/10/07/11/49/fallow-deer-3729821__340.jpg");
//
//        setupImageGrid(imageURLs);
//    }
//
//    private void setupImageGrid(ArrayList<String> imageURLs) {
//        GridView gridView = findViewById(R.id.gridView);
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter adapter =
//                new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imageURLs);
//        gridView.setAdapter(adapter);
//
//    }
//
//    private void setProfileImage() {
//        Log.d(TAG, "setProfileImage: setting profile photo.");
//        String imageURL = "cdn.pixabay.com/photo/2018/10/12/22/08/flamingo-3743094__340.jpg";
//        UniversalImageLoader.setImage(imageURL, mProfilePhoto, mProgressBar, "https://");
//    }
//
//    private void setupActivityWidgets() {
//        mProgressBar = findViewById(R.id.profileProgressbar);
//        mProgressBar.setVisibility(View.GONE);
//        mProfilePhoto = findViewById(R.id.profile_photo);
//    }
//
//    private void setupToolbar() {
//        Toolbar toolbar = findViewById(R.id.profileToolBar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = findViewById(R.id.profileMenu);
//        profileMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: navigating to account settings.");
//                startActivity(new Intent(mContext, AccountSettingsActivity.class));
//            }
//        });
//    }
//
//    /**
//     * BottomNavigationView setup
//     */
//    private void setupBottomNavigationView() {
//        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
//        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
//        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
//        Menu menu = bottomNavigationViewEx.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return true;
//    }

}

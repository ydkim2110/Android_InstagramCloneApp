package com.example.anti2110.instagramcloneapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anti2110.instagramcloneapp.Model.Comment;
import com.example.anti2110.instagramcloneapp.Model.Like;
import com.example.anti2110.instagramcloneapp.Model.Photo;
import com.example.anti2110.instagramcloneapp.Model.User;
import com.example.anti2110.instagramcloneapp.Model.UserAccountSettings;
import com.example.anti2110.instagramcloneapp.Model.UserSettings;
import com.example.anti2110.instagramcloneapp.Profile.AccountSettingsActivity;
import com.example.anti2110.instagramcloneapp.Profile.ProfileActivity;
import com.example.anti2110.instagramcloneapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileFragment extends Fragment {

    private static final String TAG = "ViewProfileFragment";

    public interface  OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    // widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayname, mUsername, mWebsite, mDescription, mFollow, mUnfollow;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView mGridView;
    private ImageView mBackArrow;
    private BottomNavigationViewEx mBottomNavigationViewEx;
    private Context mContext;
    private TextView editProfile;

    // vars
    private User mUser;
    private int mFollowersCount  = 0;
    private int mFollowingCount  = 0;
    private int mPostsCount  = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        Log.d(TAG, "onCreateView: started.");
        mContext = getActivity();

        mDisplayname = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowing = view.findViewById(R.id.tvFollowing);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        mGridView = view.findViewById(R.id.gridView);
        mBottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mFollow = view.findViewById(R.id.follow);
        mUnfollow = view.findViewById(R.id.unfollow);
        editProfile = view.findViewById(R.id.textEditProfile);
        mBackArrow = view.findViewById(R.id.backArrow);

        try {
            mUser = getUserFromBundle();
            init();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            Toast.makeText(mContext, "something went wrong.", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }

        setupBottomNavigationView();
        setupFirebaseAuth();

        isFollowing();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: now following: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                setFollowing();
            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: now unfollowing: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                setUnfollowing();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile_fragment));
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    private void init() {
        // set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();

        Query query1 = reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // get the users profile photos
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = reference2
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Photo> photos = new ArrayList<Photo>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Photo photo =new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments = new ArrayList<>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_comments)).getChildren()) {
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }

                    photo.setComments(comments);

                    List<Like> likesList = new ArrayList<>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);

                }
                setupImageGrid(photos);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checking if follwing this users.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersCount() {
        mFollowersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found follower:" + singleSnapshot.getValue());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount() {
        mFollowingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found following:" + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount() {
        mPostsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found post:" + singleSnapshot.getValue());
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for following this user.");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }

    private void setUnfollowing() {
        Log.d(TAG, "setUnfollowing: updating UI for unfollowing this user.");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);
    }

    private void setCurrentUsersProfile() {
        Log.d(TAG, "setCurrentUsersProfile: updating UI for showing this user their own profile.");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private void setupImageGrid(final ArrayList<Photo> photos) {
        // setup our image grid
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        mGridView.setColumnWidth(imageWidth);

        ArrayList<String> imageURLs = new ArrayList<String>();
        for (int i=0; i<photos.size(); i++) {
            imageURLs.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter =
                new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "", imageURLs);
        mGridView.setAdapter(adapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(i), ACTIVITY_NUM);
            }
        });
    }

    private User getUserFromBundle() {
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
        
        super.onAttach(context);
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: started.");
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayname.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mProgressBar.setVisibility(View.GONE);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(mBottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), mBottomNavigationViewEx);
        Menu menu = mBottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*
    ----------------------------------------- Firebase --------------------------------------------
     */

    /**
     * setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // Signed In
                } else {
                    // Signed Out
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

}

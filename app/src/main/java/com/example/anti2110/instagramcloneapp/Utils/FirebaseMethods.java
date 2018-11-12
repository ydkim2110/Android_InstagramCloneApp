package com.example.anti2110.instagramcloneapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.anti2110.instagramcloneapp.Home.HomeActivity;
import com.example.anti2110.instagramcloneapp.Model.Photo;
import com.example.anti2110.instagramcloneapp.Model.User;
import com.example.anti2110.instagramcloneapp.Model.UserAccountSettings;
import com.example.anti2110.instagramcloneapp.Model.UserSettings;
import com.example.anti2110.instagramcloneapp.Profile.AccountSettingsActivity;
import com.example.anti2110.instagramcloneapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    private UploadTask uploadTask;

    private Context mContext;

    // vars
    private double mPhotoUploadProgress = 0;


    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, final String caption, int imageCount, final String imageURL, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");

        FilePaths filePaths = new FilePaths();
        // case1) new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (imageCount + 1));

            // convert image url to bitmap
            if (bm == null) {
                bm =ImageManager.getBitmap(imageURL);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done.");
                }
            });

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Toast.makeText(mContext, "Photo upload success.", Toast.LENGTH_SHORT).show();

                        // add the new photo to 'photo' node and 'user_photos' node
                        addPhotoDatabase(caption, downloadUri.toString());

                        // navigating to the main feed so the user can see their photo
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        mContext.startActivity(intent);

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            });

        // case2) new profile photo
        } else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            // convert image url to bitmap
            if (bm == null) {
                bm =ImageManager.getBitmap(imageURL);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done.");
                }
            });

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Toast.makeText(mContext, "Photo upload success.", Toast.LENGTH_SHORT).show();

                        // insert into 'user_account_settings' node
                        setProfilePhoto(downloadUri.toString());

                        ((AccountSettingsActivity)mContext).setViewPager(
                                ((AccountSettingsActivity)mContext).mPagerAdapter.getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                        );

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private void addPhotoDatabase(String caption, String downloadUri) {
        Log.d(TAG, "addPhotoDatabase: adding photo to database.");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(downloadUri);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        // insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdf.format(new Date());
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;

        for(DataSnapshot snapshot: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;

    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayname
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayname, String website, String description, long phoneNumber) {

        Log.d(TAG, "updateUserAccountSettings: updating user account settings");

        if (displayname != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayname);
        }

        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if (phoneNumber != 0) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }

    }

    /**
     * update username in the 'users' node and 'user_account_settings' node
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update the email in the 'user' node
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
//        Log.d(TAG, "checkIfUsernameExists: ");
//
//        User user = new User();
//
//        for (DataSnapshot snapshot : dataSnapshot.child(userID).getChildren()) {
//            Log.d(TAG, "checkIfUsernameExists: datasnapshot: " + snapshot);
//
//            user.setUsername(snapshot.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUsername());
//
//            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
//                Log.d(TAG, "checkIfUsernameExists: Found a match! " + user.getUsername());
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            //send verification email
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: authstate changed: " + userID);
                        }

                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "Couldn't send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add information to the users nodes
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo) {

        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieve the account settings for the user currently logged in
     * Database user_account_settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


            // user_account_settings node
            if (snapshot.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserSettings: datasnapshot: " + snapshot);
                try {
                    settings.setDisplay_name(snapshot.child(userID).getValue(UserAccountSettings.class).getDisplay_name());
                    settings.setUsername(snapshot.child(userID).getValue(UserAccountSettings.class).getUsername());
                    settings.setWebsite(snapshot.child(userID).getValue(UserAccountSettings.class).getWebsite());
                    settings.setDescription(snapshot.child(userID).getValue(UserAccountSettings.class).getDescription());
                    settings.setProfile_photo(snapshot.child(userID).getValue(UserAccountSettings.class).getProfile_photo());
                    settings.setPosts(snapshot.child(userID).getValue(UserAccountSettings.class).getPosts());
                    settings.setFollowers(snapshot.child(userID).getValue(UserAccountSettings.class).getFollowers());
                    settings.setFollowing(snapshot.child(userID).getValue(UserAccountSettings.class).getFollowing());

                    Log.d(TAG, "getUserSettings: retrieved user_account_settings information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserSettings: NullPointerException: " + e.getMessage());
                }
            }


            // user node
            if (snapshot.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserSettings: datasnapshot: " + snapshot);
                try {
                    user.setUsername(snapshot.child(userID).getValue(User.class).getUsername());
                    user.setEmail(snapshot.child(userID).getValue(User.class).getEmail());
                    user.setPhone_number(snapshot.child(userID).getValue(User.class).getPhone_number());
                    user.setUser_id(snapshot.child(userID).getValue(User.class).getUser_id());

                    Log.d(TAG, "getUserSettings: retrieved user information: " + user.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserSettings: NullPointerException: " + e.getMessage());
                }
            }

        }
        return new UserSettings(user, settings);


    }


}

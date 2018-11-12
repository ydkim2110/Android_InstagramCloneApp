package com.example.anti2110.instagramcloneapp.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Heart {

    private static final String TAG = "Heart";

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public ImageView mHeartWhite, mHearRed;

    public Heart(ImageView heartWhite, ImageView hearRed) {
        mHeartWhite = heartWhite;
        mHearRed = hearRed;
    }

    public void toggleLike() {
        Log.d(TAG, "toggleLike: toggling heart.");

        AnimatorSet animatorSet = new AnimatorSet();

        if (mHearRed.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "toggleLike: toggling red heart off.");
            mHearRed.setScaleX(0.1f);
            mHearRed.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mHearRed, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mHearRed, "scaleX", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

            mHearRed.setVisibility(View.GONE);
            mHeartWhite.setVisibility(View.VISIBLE);

            animatorSet.playTogether(scaleDownY, scaleDownX);

        } else if (mHearRed.getVisibility() == View.GONE) {
            Log.d(TAG, "toggleLike: toggling red heart off.");
            mHearRed.setScaleX(0.1f);
            mHearRed.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mHearRed, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mHearRed, "scaleX", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);

            mHearRed.setVisibility(View.VISIBLE);
            mHeartWhite.setVisibility(View.GONE);

            animatorSet.playTogether(scaleDownY, scaleDownX);
        }

        animatorSet.start();
    }

}

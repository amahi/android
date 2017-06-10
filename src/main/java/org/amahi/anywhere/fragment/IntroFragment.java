package org.amahi.anywhere.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.NavigationActivity;

import java.util.ArrayList;

public class IntroFragment extends OnboardingFragment {

    private ArrayList<String> mTitles,mDescriptions;

    private static final int[] CONTENT_IMAGES = {
            R.drawable.ic_app_logo,
            R.drawable.network,
            R.drawable.photos,
            R.drawable.music,
            R.drawable.movies,
            R.drawable.tick,
    };

    private ArrayList<Integer> mColors;

    private View mBackgroundView;

    private ImageView mContentView;

    private Animator mContentAnimator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTitles = new ArrayList<>();
        mDescriptions = new ArrayList<>();
        mColors = new ArrayList<>();
        mColors.add(Color.parseColor("#0277bd"));
        mColors.add(Color.parseColor("#3949ab"));
        mColors.add(Color.DKGRAY);
        mColors.add(Color.parseColor("#26a69a"));
        mColors.add(Color.parseColor("#FBC02D"));
        mColors.add(Color.parseColor("#303f9f"));
        mTitles.add("Amahi TV");
        mDescriptions.add("Presenting the all new Amahi TV.");
        mTitles.add("Access your HDA");
        mDescriptions.add("Amahi TV lets you access your HDA. Play your Videos, Listen to your music and yes! View your photos");
        mTitles.add("Watch your photos.");
        mDescriptions.add("View all your photos backed in your HDA directly in your Android TV.");
        mTitles.add("Listen to your music");
        mDescriptions.add("Listen to all of your songs backed in your HDA directly in your Android TV.");
        mTitles.add("Play your videos.");
        mDescriptions.add("Play Videos directly on your Android TV.");
        mTitles.add("Ready");
        mDescriptions.add("You're all set to go. Thanks for using Amahi.");
        setLogoResourceId(R.drawable.ic_app_logo);
    }

    @Override
    protected int getPageCount() {
        return mTitles.size();
    }

    @Override
    protected CharSequence getPageTitle(int pageIndex) {
        return mTitles.get(pageIndex);
    }

    @Override
    protected CharSequence getPageDescription(int pageIndex) {
        return mDescriptions.get(pageIndex);
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        mBackgroundView = inflater.inflate(R.layout.onboarding_image, container, false);
        return mBackgroundView;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        mContentView = (ImageView) inflater.inflate(R.layout.onboarding_image, container,
                false);
        ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) mContentView.getLayoutParams());
        layoutParams.topMargin = 30;
        layoutParams.bottomMargin = 60;
        return mContentView;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }
    @Override
    protected Animator onCreateEnterAnimation() {
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(createFadeInAnimator(mBackgroundView));
        mContentView.setImageResource(CONTENT_IMAGES[0]);
        mContentAnimator = createFadeInAnimator(mContentView);
        animators.add(mContentAnimator);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        mBackgroundView.setBackground(new ColorDrawable(mColors.get(0)));
        return set;
    }

    @Override
    protected void onPageChanged(final int newPage, int previousPage) {
        if (mContentAnimator != null) {
            mContentAnimator.end();
        }
        ArrayList<Animator> animators = new ArrayList<>();
        Animator fadeOut = createFadeOutAnimator(mContentView);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(getClass().getName(), String.valueOf(newPage));
                mContentView.setImageResource(CONTENT_IMAGES[newPage]);
                switch (newPage){
                    case 0:
                        mBackgroundView.setBackground(new ColorDrawable(mColors.get(newPage)));
                        break;
                    case 1:
                        mBackgroundView.setBackground(new ColorDrawable(mColors.get(newPage)));
                        break;
                    case 2:
                        mBackgroundView.setBackground(new ColorDrawable(mColors.get(newPage)));
                        break;
                    case 3:
                        mBackgroundView.setBackground(new ColorDrawable(mColors.get(newPage)));
                        break;
                    case 4:
                        mBackgroundView.setBackground(new ColorDrawable(mColors.get(newPage)));
                        break;
                    case 5:
                        mBackgroundView.setBackground(new ColorDrawable(mColors.get(newPage)));
                        break;
                }
            }
        });
        animators.add(fadeOut);
        animators.add(createFadeInAnimator(mContentView));
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animators);
        set.start();
        mContentAnimator = set;
    }

    private Animator createFadeInAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(500);
    }

    private Animator createFadeOutAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(500);
    }

    @Override
    protected void onFinishFragment() {
        startActivity(new Intent(getActivity(), NavigationActivity.class));
    }
}
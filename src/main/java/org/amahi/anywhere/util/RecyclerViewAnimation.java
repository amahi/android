package org.amahi.anywhere.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class RecyclerViewAnimation {


    public void animateFadeIn(View view) {
        view.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0.f, 0.5f, 1.f);
        ObjectAnimator.ofFloat(view, "alpha", 0.f).start();
        animatorAlpha.setStartDelay(150);
        animatorAlpha.setDuration(150);
        animatorSet.play(animatorAlpha);
        animatorSet.start();
    }

}

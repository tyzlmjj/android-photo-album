package me.majiajie.photoalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 用于处理缩放动画的布局
 */
public class ScaleFrameLayout extends FrameLayout {

    private ValueAnimator mAnimator;

    public ScaleFrameLayout(@NonNull Context context) {
        this(context,null);
    }

    public ScaleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScaleFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAnimator = ValueAnimator.ofFloat(1.0f,0.8f);
        mAnimator.setDuration(150);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float n = (float) valueAnimator.getAnimatedValue();
                ScaleFrameLayout.this.setScaleX(n);
                ScaleFrameLayout.this.setScaleY(n);
            }
        });
    }

    public void startAnim(){
        mAnimator.start();
    }

    public void resetAnim(){
        mAnimator.reverse();
    }

    public void setChecked(boolean checked) {
        float n = checked ? 0.8f : 1.0f;
        setScaleX(n);
        setScaleY(n);
    }

}

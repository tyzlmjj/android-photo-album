package me.majiajie.photoalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 相册列表的图片视图,缩放直接调用
 */
public class ScaleImageView extends AppCompatImageView {

    private ValueAnimator mAnimator;

    public ScaleImageView(Context context) {
        this(context,null);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAnimator = ValueAnimator.ofFloat(1.0f,0.8f);
        mAnimator.setDuration(150);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float n = (float) valueAnimator.getAnimatedValue();
                ScaleImageView.this.setScaleX(n);
                ScaleImageView.this.setScaleY(n);
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

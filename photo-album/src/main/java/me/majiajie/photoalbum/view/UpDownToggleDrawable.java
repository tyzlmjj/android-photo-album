package me.majiajie.photoalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;
import android.widget.Checkable;


/**
 * 上下箭头
 */
public class UpDownToggleDrawable extends Drawable implements Checkable {

    /**
     * 箭头向上
     */
    public static final int STATE_UP = 0x1;

    /**
     * 箭头向下
     */
    public static final int STATE_DOWN = 0x2;

    /**
     * 长宽大小
     */
    private static final int SIZE = 24;//dp

    /**
     * 内边距
     */
    private static final int PADDING = 5;//dp

    /**
     * 线条宽
     */
    private static final int STROKE_WIDTH = 2;//dp

    /**
     * 动画时间
     */
    private static final int ANIM_TIME = 300;

    /**
     * 开始状态
     */
    private int mStartState;

    /**
     * 当前状态
     */
    private int mState;

    /**
     * 动画
     */
    private ValueAnimator mAnimator;

    /**
     * 动画系数
     */
    private float mValue;

    /**
     * 长宽大小(像素)
     */
    private float mSize;

    /**
     * 内边距（像素）
     */
    private float mPadding;

    private Path mPath_up;
    private Path mPath_down;

    private float tem;

    private Paint mPaint;

    /**
     * @param context   上下文
     * @param start     {@link UpDownToggleDrawable#STATE_UP} or {@link UpDownToggleDrawable#STATE_DOWN}
     * @param color     箭头颜色
     */
    public UpDownToggleDrawable(Context context, int start, int color) {

        mPaint = new Paint();

        mState = mStartState = start;

        setupAnim();

        mSize = dp2px(context, SIZE);
        mPadding = dp2px(context, PADDING);

        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStrokeCap(Paint.Cap.ROUND);//直线头尾圆滑
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp2px(context, STROKE_WIDTH));

        mPath_down = new Path();
        mPath_down.moveTo(mPadding, mPadding + (mSize - 2 * mPadding) / 2f / 2f);
        mPath_down.lineTo(mPadding + (mSize - 2 * mPadding) / 2f, mPadding + (mSize - 2 * mPadding) / 2f * 1.5f);
        mPath_down.lineTo(mSize - mPadding, mPadding + (mSize - 2 * mPadding) / 2f / 2f);

        mPath_up = new Path();
        mPath_up.moveTo(mPadding, mPadding + (mSize - 2 * mPadding) / 2f * 1.5f);
        mPath_up.lineTo(mPadding + (mSize - 2 * mPadding) / 2f, mPadding + (mSize - 2 * mPadding) / 2f / 2f);
        mPath_up.lineTo(mSize - mPadding, mPadding + (mSize - 2 * mPadding) / 2f * 1.5f);

        tem = (mSize - 2 * mPadding) / 2f / 2f;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mValue == 0f) {
            drawDown(canvas);
        } else if (mValue == 1f) {
            drawUp(canvas);
        } else {
            drawAnim(canvas, mValue);
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) mSize;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) mSize;
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked) {
            mState = mStartState == STATE_DOWN ? STATE_UP : STATE_DOWN;
            mAnimator.start();
        } else {
            mState = mStartState;
            mAnimator.reverse();
        }
    }

    @Override
    public boolean isChecked() {
        return mState != mStartState;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    private void drawAnim(Canvas canvas, float value) {
        canvas.save();
        canvas.translate(mPadding + tem, mSize / 2f);//设置画布上的(0,0)位置，也就是旋转的中心点
        canvas.rotate(90 * value);
        canvas.drawLine(-tem, -tem, tem, tem, mPaint);
        canvas.restore();//恢复状态

        canvas.save();
        canvas.translate(mPadding + tem * 3, mSize / 2f);
        canvas.rotate(-90 * value);
        canvas.drawLine(-tem, tem, tem, -tem, mPaint);
        canvas.restore();
    }

    private void drawDown(Canvas canvas) {
        canvas.drawPath(mPath_down, mPaint);
    }

    private void drawUp(Canvas canvas) {
        canvas.drawPath(mPath_up, mPaint);
    }

    private void setupAnim() {
        ValueAnimator animator;
        if (mStartState == STATE_DOWN) {
            mValue = 0f;
            animator = ValueAnimator.ofFloat(1.0f);
        } else {
            mValue = 1.0f;
            animator = ValueAnimator.ofFloat(1.0f, 0f);
        }

        animator.setDuration(ANIM_TIME);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mValue = (float) animation.getAnimatedValue();
                invalidateSelf();
            }
        });

        mAnimator = animator;
    }

    private float dp2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }
}

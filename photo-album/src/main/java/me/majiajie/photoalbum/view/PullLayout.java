package me.majiajie.photoalbum.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.majiajie.photoalbum.utils.DampingUtils;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * 左右边缘拖拽布局
 */
public class PullLayout extends FrameLayout {

    /**
     * 视图最大左右拉动距离(DP)
     */
    private final int MAX_VIEW_MOVE = 100;

    /**
     * 阻尼系数
     */
    private final float DAMP = 3f;

    /**
     * 视图最大左右拉动距离（px）
     */
    private float mMaxViewMove;

    /**
     * 需要左右拉动的子视图
     */
    private View mChildView;

    /**
     * 记录当前活跃的指针
     */
    private int mActivePointerId = INVALID_POINTER_ID;

    /**
     * 记录手指最后触摸点Y
     */
    private float mLastTouchX;

    /**
     * 记录手指在Y轴上的累计移动距离
     */
    private float mTmpX;

    /**
     * 需要拉动的子视图需要实现的接口
     */
    public interface PullChild{}

    public PullLayout(@NonNull Context context) {
        this(context,null);
    }

    public PullLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PullLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaxViewMove = MAX_VIEW_MOVE * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            View view = getChildAt(i);
            if (view instanceof PullChild){
                mChildView = view;
                break;
            }
        }
        if (mChildView == null){
            if (n > 0){
                mChildView = getChildAt(0);
            } else {
                throw new ClassCastException("should be have a child");
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isShouldIntercept = false;
        int action = ev.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = ev.getActionIndex();
                mLastTouchX = ev.getX(pointerIndex);
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1) {
                    final float x = ev.getX(pointerIndex);
                    float deltaX = x - mLastTouchX;

                    if (deltaX > 8){
                        isShouldIntercept = !mChildView.canScrollHorizontally(-1);
                    } else if (deltaX < -8){
                        isShouldIntercept = !mChildView.canScrollHorizontally(1);
                    }

                    mLastTouchX = x;
                    mActivePointerId = ev.getPointerId(0);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        if (isShouldIntercept){
            // 暂停可能存在的回复动画
            ValueAnimator animator = getGoBackAnim();
            if (animator.isStarted()){
                animator.cancel();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        // 用于判断是否需要销毁触摸事件
        boolean shouldMove = true;

        int action = ev.getAction();
        switch(action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1) {
                    final float x = ev.getX(pointerIndex);

                    mTmpX += x - mLastTouchX;
                    mLastTouchX = x;

                    // 视图左右移动距离计算
                    int move;
                    if (mTmpX < 0){
                        move = -(int) DampingUtils.getViewMove(-mTmpX,DAMP,mMaxViewMove);
                    } else {
                        move = (int) DampingUtils.getViewMove(mTmpX,DAMP,mMaxViewMove);
                    }

                    // 控制视图左右移动
                    mChildView.layout(move,0,mChildView.getWidth() + move,mChildView.getHeight());
                }
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                shouldMove = false;
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
            default:{
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1) {
                    mLastTouchX = ev.getX(pointerIndex);
                }
                mActivePointerId = ev.getPointerId(0);
            }
        }

        if (!shouldMove){// 如果不需要再拖动，就将视图移回初始位置
            mTmpX = 0f;
            goBack();
        }
        return shouldMove;
    }

    /**
     * 回到顶部
     */
    private void goBack() {
        if (mChildView != null){
            getGoBackAnim().start();
        }
    }

    private ValueAnimator mGobackAnimator;

    /**
     * 回到顶部部的动画
     */
    private ValueAnimator getGoBackAnim(){
        if (mGobackAnimator == null){
            ValueAnimator animator = ValueAnimator.ofFloat(1f,0f);
            animator.setDuration(300);
            animator.addUpdateListener(mGoBackListener);
            mGobackAnimator = animator;
        }
        return mGobackAnimator;
    }

    /**
     * 视图回弹动画监听
     */
    ValueAnimator.AnimatorUpdateListener mGoBackListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float n = (float) valueAnimator.getAnimatedValue();
            int move = (int) (mChildView.getLeft() * n);
            mChildView.layout(move,0,mChildView.getWidth() + move,mChildView.getHeight());
        }
    };
}

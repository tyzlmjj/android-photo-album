package me.majiajie.photoalbum.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * 可监听越界滑动事件
 */
public class MyViewPager extends ViewPager implements PullLayout.PullChild{

    /**
     * 判断是否在最右侧
     */
    private boolean mIsRight;

    /**
     * 判断是否在最左侧
     */
    private boolean mIsLeft;

    /**
     * 判断是否在最后一项
     */
    private boolean mIsOnLastItem;

    /**
     * 记录当前活跃的指针
     */
    private int mActivePointerId = INVALID_POINTER_ID;

    /**
     * 记录手指最后触摸点Y
     */
    private float mLastTouchX;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mIsLeft = false;
                mIsRight = false;
                final int pointerIndex = ev.getActionIndex();
                mLastTouchX = ev.getX(pointerIndex);
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1 && getChildCount() > 0) {
                    final float x = ev.getX(pointerIndex);
                    float deltaX = x - mLastTouchX;
                    mLastTouchX = x;

                    // 判断是否移动到最左侧
                    mIsLeft = getScrollX() == 0 && deltaX > 0;
                    // 判断是否移动到最右侧
                    mIsRight = mIsOnLastItem && deltaX < 0;
                }
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsLeft = false;
                mIsRight = false;
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

        return !(mIsLeft || mIsRight) && super.onTouchEvent(ev);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        PagerAdapter adapter = getAdapter();
        if (adapter != null){
            mIsOnLastItem = position == adapter.getCount() - 1 && offset == 0;
        }
        super.onPageScrolled(position, offset, offsetPixels);
    }

    @Override
    public boolean isLeft() {
        return mIsLeft;
    }

    @Override
    public boolean isRight() {
        return mIsRight;
    }

}

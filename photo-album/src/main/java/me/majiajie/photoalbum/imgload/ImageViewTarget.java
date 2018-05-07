package me.majiajie.photoalbum.imgload;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.MainThread;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 借鉴Glide的ViewTarget，只保留了获取大小的部分
 */
public class ImageViewTarget {

    private final ImageViewTarget.SizeDeterminer sizeDeterminer;

    public interface SizeReadyCallback {

        /**
         * 视图大小获取成功
         */
        @MainThread
        void onSizeReady(View view,int width, int height);
    }

    ImageViewTarget(View view,boolean waitForLayout) {
        sizeDeterminer = new SizeDeterminer(view);
        sizeDeterminer.waitForLayout = waitForLayout;
    }

    /**
     * 获取View大小
     */
    public void getSize(SizeReadyCallback cb) {
        sizeDeterminer.getSize(cb);
    }

    /**
     * 移除回调
     */
    public void removeCallback(SizeReadyCallback cb) {
        sizeDeterminer.removeCallback(cb);
    }

    static final class SizeDeterminer {

        private static final int PENDING_SIZE = 0;

        private static Integer maxDisplayLength;

        private SizeDeterminerLayoutListener layoutListener;

        private final List<SizeReadyCallback> cbs = new ArrayList<>();

        private final WeakReference<View> view;

        boolean waitForLayout = false;

        SizeDeterminer(View view) {
            this.view = new WeakReference<>(view);
        }

        /**
         * 获取View的大小
         */
        void getSize(SizeReadyCallback cb) {
            View v = view.get();
            if (v == null){
                return;
            }
            int currentWidth = getTargetWidth();
            int currentHeight = getTargetHeight();
            if (isViewStateAndSizeValid(currentWidth, currentHeight)) {
                cb.onSizeReady(v,currentWidth, currentHeight);
                return;
            }

            if (!cbs.contains(cb)) {
                cbs.add(cb);
            }

            if (layoutListener == null) {
                ViewTreeObserver observer = v.getViewTreeObserver();
                layoutListener = new ImageViewTarget.SizeDeterminer.SizeDeterminerLayoutListener(this);
                observer.addOnPreDrawListener(layoutListener);
            }
        }

        /**
         * 移除一个回调
         */
        void removeCallback(SizeReadyCallback cb) {
            cbs.remove(cb);
        }

        /**
         * 清除所有回调和视图监听
         */
        void clearCallbacksAndListener() {
            View v = view.get();
            if (v == null){ return; }

            ViewTreeObserver observer = v.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnPreDrawListener(layoutListener);
            }
            layoutListener = null;
            cbs.clear();
        }

        /**
         * 获取屏幕最大长度
         */
        private int getMaxDisplayLength(Context context) {
            if (maxDisplayLength == null) {
                WindowManager windowManager =
                        (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    Display display = windowManager.getDefaultDisplay();
                    Point displayDimensions = new Point();
                    display.getSize(displayDimensions);
                    maxDisplayLength = Math.max(displayDimensions.x, displayDimensions.y);
                } else {
                    maxDisplayLength = PENDING_SIZE;
                }
            }
            return maxDisplayLength;
        }

        /**
         * 通知所有的回调
         */
        private void notifyCbs(int width, int height) {
            View v = view.get();
            for (SizeReadyCallback cb : new ArrayList<>(cbs)) {
                cb.onSizeReady(v,width, height);
            }
        }

        /**
         * 在View准备好时检查大小
         */
        void checkCurrentDimens() {
            if (cbs.isEmpty()) {
                return;
            }

            int currentWidth = getTargetWidth();
            int currentHeight = getTargetHeight();

            if (!isViewStateAndSizeValid(currentWidth, currentHeight)) {
                return;
            }

            notifyCbs(currentWidth, currentHeight);
            clearCallbacksAndListener();
        }

        private boolean isViewStateAndSizeValid(int width, int height) {
            return isDimensionValid(width) && isDimensionValid(height);
        }

        private int getTargetHeight() {
            View v = view.get();
            if (v == null){
                return PENDING_SIZE;
            }
            int verticalPadding = v.getPaddingTop() + v.getPaddingBottom();
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            int layoutParamSize = layoutParams != null ? layoutParams.height : PENDING_SIZE;
            return getTargetDimen(v.getHeight(), layoutParamSize, verticalPadding);
        }

        private int getTargetWidth() {
            View v = view.get();
            if (v == null){
                return PENDING_SIZE;
            }
            int horizontalPadding = v.getPaddingLeft() + v.getPaddingRight();
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            int layoutParamSize = layoutParams != null ? layoutParams.width : PENDING_SIZE;
            return getTargetDimen(v.getWidth(), layoutParamSize, horizontalPadding);
        }

        private int getTargetDimen(int viewSize, int paramSize, int paddingSize) {

            View v = view.get();
            if (v == null){
                return PENDING_SIZE;
            }

            int adjustedParamSize = paramSize - paddingSize;
            if (adjustedParamSize > 0) {
                return adjustedParamSize;
            }

            if (waitForLayout && v.isLayoutRequested()) {
                return PENDING_SIZE;
            }

            int adjustedViewSize = viewSize - paddingSize;
            if (adjustedViewSize > 0) {
                return adjustedViewSize;
            }

            if (!v.isLayoutRequested() && paramSize == ViewGroup.LayoutParams.WRAP_CONTENT) {
                return getMaxDisplayLength(v.getContext());
            }

            return PENDING_SIZE;
        }

        /**
         * 判断大小是否有效
         */
        private boolean isDimensionValid(int size) {
            return size > 0 || size == Integer.MIN_VALUE;
        }

        private static final class SizeDeterminerLayoutListener
                implements ViewTreeObserver.OnPreDrawListener {
            private final WeakReference<ImageViewTarget.SizeDeterminer> sizeDeterminerRef;

            SizeDeterminerLayoutListener(ImageViewTarget.SizeDeterminer sizeDeterminer) {
                sizeDeterminerRef = new WeakReference<>(sizeDeterminer);
            }

            @Override
            public boolean onPreDraw() {
                ImageViewTarget.SizeDeterminer sizeDeterminer = sizeDeterminerRef.get();
                if (sizeDeterminer != null) {
                    sizeDeterminer.checkCurrentDimens();
                }
                return true;
            }
        }
    }
}

package me.majiajie.photoalbum.imgload;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * 处理ImageView加载图片的任务调度和内存缓存的基类.
 */
public abstract class ImageWorker {

    private static final int FADE_IN_TIME = 200;

    private ImageMemoryCache mImageCache = ImageMemoryCache.getInstance();
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    private boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    private Resources mResources;

    private static final int MESSAGE_CLEAR = 0;

    ImageWorker(Context context) {
        mResources = context.getResources();
    }

    /**
     * 在子线程中加载Bitmap
     *
     * @param filePath  本地图片地址
     * @param width     ImageView宽度
     * @param height    ImageView高度
     * @param scaleType ImageView缩放类型
     * @return 处理完成的Bitmap
     */
    @WorkerThread
    protected abstract Bitmap processBitmap(String filePath, int width, int height, ImageView.ScaleType scaleType);

    /**
     * 加载本地图片到{@link ImageView},如果图片已经存在{@link ImageMemoryCache}中就会直接获取。
     * 不存在的话就会按{@link ImageView}的大小加载图片，并存在{@link ImageMemoryCache}中。
     *
     * @param filePath  本地图片地址
     * @param imageView 需要加载的图片的ImageView
     */
    public void loadImage(final String filePath, ImageView imageView) {
        if (TextUtils.isEmpty(filePath) || imageView == null) {
            return;
        }

        if (!hasSameWork(filePath, imageView)) {

            // 设置ImageView的加载任务
            final BitmapWorkerTask task = new BitmapWorkerTask(filePath, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, null, task);
            imageView.setImageDrawable(asyncDrawable);

            // 获取ImageView的大小
            task.imageViewTarget.getSize(new ImageViewTarget.SizeReadyCallback() {
                @Override
                public void onSizeReady(View view, int width, int height) {

                    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask((ImageView) view);
                    if (bitmapWorkerTask != null) {
                        final String data = bitmapWorkerTask.mData;
                        // 判断是否加载同一个图片
                        if (TextUtils.equals(data, filePath)) {

                            BitmapDrawable value = null;

                            if (mImageCache != null) {
                                value = mImageCache.getBitmapFromCache(filePath, width, height);
                            }

                            if (value != null) {// 内存中存在
                                ((ImageView) view).setImageDrawable(value);
                            } else {
                                bitmapWorkerTask.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, width, height);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 设置图像显示时,是否使用淡入的动画.默认true
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    /**
     * 设置退出/执行任务
     */
    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(exitTasksEarly);
    }

    /**
     * 判断是否有相同任务在执行，假如针对这个ImageView存在别的任务，就取消任务
     *
     * @param filePath  本地图片地址
     * @param imageView 视图
     * @return 如果存在相同的任务，返回true.如果没有相同的任务在执行,返回false
     */
    private boolean hasSameWork(String filePath, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String data = bitmapWorkerTask.mData;
            if (TextUtils.equals(data, filePath)) {
                // 已经有相同的任务在执行
                return true;
            } else {
                bitmapWorkerTask.cancel(true);
            }
        }
        return false;
    }

    /**
     * @param imageView imageView
     * @return 返回当前ImageView的图片加载任务, 没有则返回null
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * Bitmap生成任务.
     */
    private class BitmapWorkerTask extends AsyncTask<Integer, Void, BitmapDrawable> {

        private String mData;

        private final ImageViewTarget imageViewTarget;

        private final WeakReference<ImageView> imageViewReference;

        BitmapWorkerTask(String data, ImageView imageView) {
            mData = data;
            imageViewReference = new WeakReference<>(imageView);
            imageViewTarget = new ImageViewTarget(imageView,true);
        }

        @Override
        protected BitmapDrawable doInBackground(Integer... params) {

            final int width = params[0];
            final int height = params[1];
            final String dataString = String.valueOf(mData);
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // 检查任务状态,如果取消或暂停就等待
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // 加载图片
            ImageView imageView = getAttachedImageView();
            if (!isCancelled() && imageView != null
                    && !mExitTasksEarly) {
                bitmap = processBitmap(mData, width, height, imageView.getScaleType());
            }

            if (bitmap != null) {
                // 图片加载完成,添加到缓存
                drawable = new BitmapDrawable(mResources, bitmap);

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(dataString, width, height, drawable);
                }
            }

            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable value) {
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                setImageDrawable(imageView, value);
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * @return 返回这个任务中的ImageView,假如ImageView已经设置了别的任务，返回null
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * 设置ImageView的图片
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // 做一个淡入的动画
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[]{
                            new ColorDrawable(Color.TRANSPARENT),
                            drawable
                    });
            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * 设置暂停/开始执行任务.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    /**
     * 清除所有缓存
     */
    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

//    /**
//     * 自定义的{@link BitmapDrawable}用于记录任务等信息
//     */
//    private static class AsyncDrawable extends BitmapDrawable {
//        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
//
//        AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
//            super(res, bitmap);
//            bitmapWorkerTaskReference =
//                    new WeakReference<>(bitmapWorkerTask);
//        }
//
//        BitmapWorkerTask getBitmapWorkerTask() {
//            return bitmapWorkerTaskReference.get();
//        }
//    }

    /**
     * 自定义的{@link BitmapDrawable}用于记录任务等信息
     */
    private static class AsyncDrawable extends BitmapDrawable {

        private final BitmapWorkerTask bitmapWorkerTask;

        AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTask = bitmapWorkerTask;
        }

        BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTask;
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer) params[0]) {
                case MESSAGE_CLEAR:
                    if (mImageCache != null) {
                        mImageCache.clearCache();
                    }
                    break;
            }
            return null;
        }
    }

}

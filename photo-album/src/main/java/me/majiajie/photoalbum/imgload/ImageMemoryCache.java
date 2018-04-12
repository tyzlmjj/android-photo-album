package me.majiajie.photoalbum.imgload;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.LruCache;

import java.util.Locale;

/**
 * 图像内存缓存,使用自带的LruCache
 */
public class ImageMemoryCache {

    private static final ImageMemoryCache ourInstance = new ImageMemoryCache();

    public static ImageMemoryCache getInstance() {
        return ourInstance;
    }

    private LruCache<String, BitmapDrawable> mCache;

    private ImageMemoryCache() {

        int maxMemory = Math.round(0.25f * Runtime.getRuntime().maxMemory() / 1024);

        mCache = new LruCache<String, BitmapDrawable>(maxMemory) {

            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                final int bitmapSize = getBitmapSize(value) / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }
        };
    }

    /**
     * 计算Bitmap的大小
     */
    private int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }

        return bitmap.getByteCount();
    }

    /**
     * 从内存中获取Bitmap
     *
     * @param filePath 本地图片地址
     * @param width    图片的宽
     * @param height   图片的高
     * @return 如果内存中不存在，返回null
     */
    BitmapDrawable getBitmapFromCache(String filePath, int width, int height) {
        BitmapDrawable memValue = null;

        if (mCache != null) {
            memValue = mCache.get(String.format(Locale.CHINA, "%s%d%d", filePath, width, height));
        }

        return memValue;
    }

    /**
     * 增加一个Bitmap到内存缓存中
     *
     * @param filePath 本地图片地址
     * @param width    图片的宽
     * @param height   图片的高
     * @param drawable 图片资源
     */
    void addBitmapToCache(String filePath, int width, int height, BitmapDrawable drawable) {
        if (filePath == null || drawable == null) {
            return;
        }

        if (mCache != null) {
            mCache.put(String.format(Locale.CHINA, "%s%d%d", filePath, width, height), drawable);
        }
    }

    /**
     * 清除缓存
     */
    void clearCache() {
        if (mCache != null) {
            mCache.evictAll();
        }
    }
}

package me.majiajie.photoalbum.imgload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.io.IOException;

/**
 * 继承{@link ImageWorker} 用于加载本地图片，并对应ImageView适配大小
 */
public class ImageLoader extends ImageWorker {

    public ImageLoader(Context context) {
        super(context);
    }

    @Override
    @WorkerThread
    protected Bitmap processBitmap(String filePath, int width, int height, ImageView.ScaleType scaleType) {
        // 先读取图片的参数
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // 计算图像缩小的值
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // 如果ImageView的缩放类型是CENTER_CROP就去裁剪
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if (options.inSampleSize == 1) {// 如果计算得出图像不需要缩放,就直接加载图片的一部分，然后缩放

                // 如果图片本身像素和ImageView相同,直接加载
                if (options.outHeight == height && options.outWidth == width){
                    // 图片像素颜色配置
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    // 设置解码bitmap
                    options.inJustDecodeBounds = false;
                    return BitmapFactory.decodeFile(filePath, options);
                }

                try {
                    BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(filePath, false);
                    BitmapFactory.Options regionDecoderOptions = new BitmapFactory.Options();
                    regionDecoderOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                    Rect rect;
                    if (((float) options.outWidth / (float) width * (float) height) > options.outHeight) {
                        int left = (int) (options.outWidth - ((float) options.outHeight / (float) height * (float) width)) / 2;
                        int right = options.outWidth - left;
                        rect = new Rect(left, 0, right, options.outHeight);
                    } else {
                        int top = (int) (options.outHeight - ((float) options.outWidth / (float) width * (float) height)) / 2;
                        int bottom = options.outHeight - top;
                        rect = new Rect(0, top, options.outWidth, bottom);
                    }

                    Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, regionDecoderOptions);

                    if (bitmap == null){// 假如Rect计算错误可能造成无法截取到图片。
                        return null;
                    }

                    if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
                        return bitmap;
                    } else {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        bitmap.recycle();
                        return scaledBitmap;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {// 如果计算得出图像需要缩放,-->加载图片-->裁剪-->缩放
                // 图片像素颜色配置
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                // 设置解码bitmap
                options.inJustDecodeBounds = false;
                // 获取Bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
                    return bitmap;
                } else {
                    // 按比例裁剪Bitmap
                    Bitmap cropBitmap;

                    if (((float) bitmap.getWidth() / (float) width * (float) height) > bitmap.getHeight()) {
                        int x = (int) (bitmap.getWidth() - ((float) bitmap.getHeight() / (float) height * (float) width)) / 2;
                        int w = bitmap.getWidth() - x * 2;
                        cropBitmap = Bitmap.createBitmap(bitmap, x, 0, w, bitmap.getHeight());
                    } else {
                        int y = (int) (bitmap.getHeight() - ((float) bitmap.getWidth() / (float) width * (float) height)) / 2;
                        int h = bitmap.getHeight() - y * 2;
                        cropBitmap = Bitmap.createBitmap(bitmap, 0, y, bitmap.getWidth(), h);
                    }

                    if (cropBitmap.getWidth() == width && cropBitmap.getHeight() == height) {
                        return cropBitmap;
                    } else {
                        // 对裁剪过的Bitmap进行缩放
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropBitmap, width, height, true);
                        bitmap.recycle();
                        if (!cropBitmap.isRecycled()) {
                            cropBitmap.recycle();
                        }
                        return scaledBitmap;
                    }
                }
            }

        } else {// 如果ImageView的缩放类型不是CENTER_CROP就直接按计算的缩放比例加载
            // 图片像素颜色配置
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            // 设置解码bitmap
            options.inJustDecodeBounds = false;
            // 获取Bitmap
            return BitmapFactory.decodeFile(filePath, options);
        }

        return null;
    }

    /**
     * 计算图片缩放比例
     * https://developer.android.com/topic/performance/graphics/load-bitmap.html#load-bitmap
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}

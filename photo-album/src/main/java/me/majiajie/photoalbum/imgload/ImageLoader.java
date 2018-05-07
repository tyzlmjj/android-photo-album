package me.majiajie.photoalbum.imgload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
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
        Bitmap result = null;

        // 检查图片旋转角度
        int orientation = readPictureDegree(filePath);

        // 先读取图片的参数
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // 计算图像缩小的值
        options.inSampleSize = calculateInSampleSize(orientation, options, width, height);

        // 如果ImageView的缩放类型是CENTER_CROP就去裁剪
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if (options.inSampleSize == 1) {// 如果计算得出图像不需要缩放,就直接加载图片的一部分，然后缩放

                // 旋转了90度或者270度就需要调换宽高
                int bitmapWidth = orientation / 90 % 2 > 0 ? options.outHeight : options.outWidth;
                int bitmapHeight = orientation / 90 % 2 > 0 ? options.outWidth : options.outHeight;

                // 如果图片本身像素和ImageView相同,直接加载
                if (bitmapHeight == height && bitmapWidth == width) {
                    // 图片像素颜色配置
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    // 设置解码bitmap
                    options.inJustDecodeBounds = false;
                    result = BitmapFactory.decodeFile(filePath, options);
                    // 图片旋转角度不为0时矫正
                    if (result != null && Math.abs(orientation) > 0) {
                        result = rotaingImageView(orientation, result);
                    }
                } else {

                    try {
                        BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(filePath, false);
                        BitmapFactory.Options regionDecoderOptions = new BitmapFactory.Options();
                        regionDecoderOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                        Rect rect;
                        if (((float) bitmapWidth / (float) width * (float) height) > bitmapHeight) {
                            int left = (int) (bitmapWidth - ((float) bitmapHeight / (float) height * (float) width)) / 2;
                            int right = bitmapWidth - left;
                            rect = new Rect(left, 0, right, bitmapHeight);
                        } else {
                            int top = (int) (bitmapHeight - ((float) bitmapWidth / (float) width * (float) height)) / 2;
                            int bottom = bitmapHeight - top;
                            rect = new Rect(0, top, bitmapWidth, bottom);
                        }

                        if (orientation / 90 % 2 > 0){
                            rect = new Rect(rect.top,bitmapWidth - rect.right,rect.bottom,bitmapWidth - rect.left);
                        }

                        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, regionDecoderOptions);

                        // rect 假如计算异常可能为null
                        if (bitmap != null) {

                            // 旋转图片
                            if (Math.abs(orientation) > 0) {
                                bitmap = rotaingImageView(orientation, bitmap);
                            }

                            if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
                                result = bitmap;
                            } else {
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                                bitmap.recycle();
                                result = scaledBitmap;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {// 如果计算得出图像需要缩放,-->加载图片-->裁剪-->缩放
                // 图片像素颜色配置
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                // 设置解码bitmap
                options.inJustDecodeBounds = false;
                // 获取Bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                if (Math.abs(orientation) > 0){
                    bitmap = rotaingImageView(orientation, bitmap);
                }

                if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
                    result = bitmap;
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
                        result = cropBitmap;
                    } else {
                        // 对裁剪过的Bitmap进行缩放
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropBitmap, width, height, true);
                        bitmap.recycle();
                        if (!cropBitmap.isRecycled()) {
                            cropBitmap.recycle();
                        }
                        result = scaledBitmap;
                    }
                }
            }

        } else {// 如果ImageView的缩放类型不是CENTER_CROP就直接按计算的缩放比例加载
            // 图片像素颜色配置
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            // 设置解码bitmap
            options.inJustDecodeBounds = false;
            // 获取Bitmap
            result = BitmapFactory.decodeFile(filePath, options);

            // 图片旋转角度不为0时矫正
            if (result != null && Math.abs(orientation) > 0) {
                result = rotaingImageView(orientation, result);
            }
        }

        return result;
    }

//    private Bitmap old(String filePath, int width, int height, ImageView.ScaleType scaleType){
//        Bitmap result = null;
//
//        // 先读取图片的参数
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, options);
//
//        // 计算图像缩小的值
//        options.inSampleSize = calculateInSampleSize(options, width, height);
//
//        // 如果ImageView的缩放类型是CENTER_CROP就去裁剪
//        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
//            if (options.inSampleSize == 1) {// 如果计算得出图像不需要缩放,就直接加载图片的一部分，然后缩放
//
//                // 如果图片本身像素和ImageView相同,直接加载
//                if (options.outHeight == height && options.outWidth == width){
//                    // 图片像素颜色配置
//                    options.inPreferredConfig = Bitmap.Config.RGB_565;
//                    // 设置解码bitmap
//                    options.inJustDecodeBounds = false;
//                    result = BitmapFactory.decodeFile(filePath, options);
//                } else {
//
//                    try {
//                        BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(filePath, false);
//                        BitmapFactory.Options regionDecoderOptions = new BitmapFactory.Options();
//                        regionDecoderOptions.inPreferredConfig = Bitmap.Config.RGB_565;
//
//                        Rect rect;
//                        if (((float) options.outWidth / (float) width * (float) height) > options.outHeight) {
//                            int left = (int) (options.outWidth - ((float) options.outHeight / (float) height * (float) width)) / 2;
//                            int right = options.outWidth - left;
//                            rect = new Rect(left, 0, right, options.outHeight);
//                        } else {
//                            int top = (int) (options.outHeight - ((float) options.outWidth / (float) width * (float) height)) / 2;
//                            int bottom = options.outHeight - top;
//                            rect = new Rect(0, top, options.outWidth, bottom);
//                        }
//
//                        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, regionDecoderOptions);
//
//                        // rect 假如计算异常可能为null
//                        if (bitmap != null) {
//                            if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
//                                result = bitmap;
//                            } else {
//                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
//                                bitmap.recycle();
//                                result = scaledBitmap;
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            } else {// 如果计算得出图像需要缩放,-->加载图片-->裁剪-->缩放
//                // 图片像素颜色配置
//                options.inPreferredConfig = Bitmap.Config.RGB_565;
//                // 设置解码bitmap
//                options.inJustDecodeBounds = false;
//                // 获取Bitmap
//                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
//
//                if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
//                    result = bitmap;
//                } else {
//                    // 按比例裁剪Bitmap
//                    Bitmap cropBitmap;
//
//                    if (((float) bitmap.getWidth() / (float) width * (float) height) > bitmap.getHeight()) {
//                        int x = (int) (bitmap.getWidth() - ((float) bitmap.getHeight() / (float) height * (float) width)) / 2;
//                        int w = bitmap.getWidth() - x * 2;
//                        cropBitmap = Bitmap.createBitmap(bitmap, x, 0, w, bitmap.getHeight());
//                    } else {
//                        int y = (int) (bitmap.getHeight() - ((float) bitmap.getWidth() / (float) width * (float) height)) / 2;
//                        int h = bitmap.getHeight() - y * 2;
//                        cropBitmap = Bitmap.createBitmap(bitmap, 0, y, bitmap.getWidth(), h);
//                    }
//
//                    if (cropBitmap.getWidth() == width && cropBitmap.getHeight() == height) {
//                        result = cropBitmap;
//                    } else {
//                        // 对裁剪过的Bitmap进行缩放
//                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropBitmap, width, height, true);
//                        bitmap.recycle();
//                        if (!cropBitmap.isRecycled()) {
//                            cropBitmap.recycle();
//                        }
//                        result = scaledBitmap;
//                    }
//                }
//            }
//
//        } else {// 如果ImageView的缩放类型不是CENTER_CROP就直接按计算的缩放比例加载
//            // 图片像素颜色配置
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
//            // 设置解码bitmap
//            options.inJustDecodeBounds = false;
//            // 获取Bitmap
//            result = BitmapFactory.decodeFile(filePath, options);
//        }
//
//        if (result != null){
//            // 检查图片旋转角度
//            int orientation = readPictureDegree(filePath);
//            if(Math.abs(orientation) > 0){
//                result =  rotaingImageView(orientation, result);
//            }
//        }
//
//        return result;
//    }

    /**
     * 计算图片缩放比例
     * https://developer.android.com/topic/performance/graphics/load-bitmap.html#load-bitmap
     */
    private int calculateInSampleSize(int orientation, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = orientation / 90 % 2 > 0 ? options.outWidth : options.outHeight;
        int width = orientation / 90 % 2 > 0 ? options.outHeight : options.outWidth;

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

    /**
     * 获取图片旋转角度
     */
    private int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     */
    private Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}

package me.majiajie.photoalbum.utils;

import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * 图片加载
 */
public class ImageLoader {

    public static void loadFileAndCenterCrop(ImageView imageView, String path){
        if (imageView == null || TextUtils.isEmpty(path)){
            return;
        }
        Glide.with(imageView.getContext())
                .load(path)
                .apply(RequestOptions.centerCropTransform().dontAnimate())
                .transition(withCrossFade())
                .into(imageView);
    }

    public static void loadFile(ImageView imageView, String path){
        if (imageView == null || TextUtils.isEmpty(path)){
            return;
        }
        Glide.with(imageView.getContext()).load(path).transition(withCrossFade()).into(imageView);
    }
}

package me.majiajie.androidphotoalbum;

import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import me.majiajie.photoalbum.IAlbumImageLoader;

/**
 * Created by mjj on 2019-09-06
 */
public class PhotoAlbumImageLoader implements IAlbumImageLoader {

    @Override
    public void loadLocalImageOrVideo(@NonNull ImageView imageView,@NonNull  String path) {
        GlideApp.with(imageView).load(path).into(imageView);
    }

    @Override
    public void loadLocalImageOrVideo(@NonNull ImageView imageView,@NonNull  Uri uri) {
        GlideApp.with(imageView).load(uri).into(imageView);
    }

}

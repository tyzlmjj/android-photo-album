package me.majiajie.androidphotoalbum;

import android.app.Application;

import me.majiajie.photoalbum.Album;

public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化相册
        Album.init(new PhotoAlbumImageLoader());
    }
}

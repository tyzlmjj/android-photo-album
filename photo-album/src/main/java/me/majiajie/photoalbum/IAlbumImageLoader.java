package me.majiajie.photoalbum;

import android.net.Uri;
import android.os.Build;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public interface IAlbumImageLoader {

    /**
     * 加载本地图片(或者视频的第一帧)
     *
     * @param imageView 视图
     * @param path      本地图片/视频地址
     */
    void loadLocalImageOrVideo(@NonNull ImageView imageView,@NonNull String path);

    /**
     * 加载本地图片(或者视频的第一帧)
     *
     * @param imageView 视图
     * @param uri       本地图片/视频地址
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    void loadLocalImageOrVideo(@NonNull ImageView imageView,@NonNull Uri uri);

}

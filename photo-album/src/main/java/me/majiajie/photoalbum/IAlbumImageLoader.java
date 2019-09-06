package me.majiajie.photoalbum;

import android.widget.ImageView;

public interface IAlbumImageLoader {

    /**
     * 加载本地图片(或者视频的第一帧)
     *
     * @param imageView 视图
     * @param path      本地图片/视频地址
     */
    public void loadLocalImageOrVideo(ImageView imageView, String path);

}

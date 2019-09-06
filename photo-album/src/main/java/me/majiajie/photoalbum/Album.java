package me.majiajie.photoalbum;

/**
 * 初始化类
 */
public class Album {

    public static IAlbumImageLoader LOADER;

    public static void init(IAlbumImageLoader loader){
        LOADER = loader;
    }
}

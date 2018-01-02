package me.majiajie.photoalbum.photo;

import java.util.ArrayList;

/**
 * 图片文件夹
 */
public class PhotosFolder {

    private String name;

    private String path;

    private String firstImage;

    private ArrayList<Photo> images;

    public PhotosFolder(String path) {
        setPath(path);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhotosFolder) {
            return getPath().equalsIgnoreCase(((PhotosFolder) obj).getPath());
        }
        return super.equals(obj);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFirstImage() {
        return firstImage;
    }

    public void setFirstImage(String firstImage) {
        this.firstImage = firstImage;
    }

    public ArrayList<Photo> getImages() {
        return images;
    }

    public void setImages(ArrayList<Photo> images) {
        this.images = images;
    }
}

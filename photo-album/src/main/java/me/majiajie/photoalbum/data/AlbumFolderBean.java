package me.majiajie.photoalbum.data;

import java.util.ArrayList;

/**
 * 图片文件夹
 */
public class AlbumFolderBean {

    private String name;

    private String path;

    private String firstImage;

    private ArrayList<AlbumFileBean> files;

    public AlbumFolderBean(String path) {
        setPath(path);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlbumFolderBean) {
            return getPath().equalsIgnoreCase(((AlbumFolderBean) obj).getPath());
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

    public ArrayList<AlbumFileBean> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<AlbumFileBean> files) {
        this.files = files;
    }
}

package me.majiajie.photoalbum.data;

import java.util.ArrayList;

/**
 * 图片文件夹
 */
public class AlbumFolderBean {

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 文件夹路径
     */
    private String path;

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

    public ArrayList<AlbumFileBean> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<AlbumFileBean> files) {
        this.files = files;
    }

}

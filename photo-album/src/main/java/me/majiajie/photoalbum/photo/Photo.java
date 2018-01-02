package me.majiajie.photoalbum.photo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 图片文件信息
 */
public class Photo implements Parcelable{

    /**
     * 在系统数据库中的id
     */
    private long id;

    /**
     * 路径
     */
    private String path;

    /**
     * 图片名字
     */
    private String name;

    /**
     * 类型
     */
    private String mime_type;

    /**
     * 宽
     */
    private long width;

    /**
     * 高
     */
    private long height;

    /**
     * 大小
     */
    private long size;

    /**
     * 创建时间
     */
    private long date_add;

    /**
     * 修改时间
     */
    private long date_modified;

    public Photo(long id, String path, String name, String mime_type, long width, long height, long size, long date_add, long date_modified) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.mime_type = mime_type;
        this.width = width;
        this.height = height;
        this.size = size;
        this.date_add = date_add;
        this.date_modified = date_modified;
    }

    protected Photo(Parcel in) {
        id = in.readLong();
        path = in.readString();
        name = in.readString();
        mime_type = in.readString();
        width = in.readLong();
        height = in.readLong();
        size = in.readLong();
        date_add = in.readLong();
        date_modified = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeString(mime_type);
        dest.writeLong(width);
        dest.writeLong(height);
        dest.writeLong(size);
        dest.writeLong(date_add);
        dest.writeLong(date_modified);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Photo){
            return TextUtils.equals(path,((Photo) obj).getPath());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate_add() {
        return date_add;
    }

    public void setDate_add(long date_add) {
        this.date_add = date_add;
    }

    public long getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(long date_modified) {
        this.date_modified = date_modified;
    }
}

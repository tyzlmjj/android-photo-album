package me.majiajie.photoalbum.data;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.IOException;

/**
 * 图片文件信息
 */
public class AlbumFileBean implements Parcelable {

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

    /**
     * 是否为视频
     */
    private boolean video;

    private int voideTime = -1;

    public AlbumFileBean(long id, String path, String name, String mime_type, long width, long height, long size, long date_add, long date_modified, boolean video) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.mime_type = mime_type;
        this.width = width;
        this.height = height;
        this.size = size;
        this.date_add = date_add;
        this.date_modified = date_modified;
        this.video = video;
    }


    protected AlbumFileBean(Parcel in) {
        id = in.readLong();
        path = in.readString();
        name = in.readString();
        mime_type = in.readString();
        width = in.readLong();
        height = in.readLong();
        size = in.readLong();
        date_add = in.readLong();
        date_modified = in.readLong();
        video = in.readByte() != 0;
        voideTime = in.readInt();
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
        dest.writeByte((byte) (video ? 1 : 0));
        dest.writeInt(voideTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlbumFileBean> CREATOR = new Creator<AlbumFileBean>() {
        @Override
        public AlbumFileBean createFromParcel(Parcel in) {
            return new AlbumFileBean(in);
        }

        @Override
        public AlbumFileBean[] newArray(int size) {
            return new AlbumFileBean[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AlbumFileBean) {
            return TextUtils.equals(path, ((AlbumFileBean) obj).getPath());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * 获取视频时长
     */
    public int getVoideTime(Context context) {
        if (voideTime < 0) {
            MediaPlayer player = new MediaPlayer();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    player.setDataSource(context, getUri());
                } else {
                    player.setDataSource(path);
                }
                player.prepare();
                voideTime = (int) Math.max(1, Math.ceil(player.getDuration() / 1000.0));
            } catch (IOException e) {
                e.printStackTrace();
                voideTime = 0;
            }
            player.release();
        }
        return voideTime;
    }

    /**
     * 获取文件Uri
     */
    @TargetApi(Build.VERSION_CODES.Q)
    public Uri getUri() {
        if (video) {
            return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getId());
        } else {
            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, getId());
        }
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

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }
}

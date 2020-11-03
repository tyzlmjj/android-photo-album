package me.majiajie.photoalbum.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.io.File;
import java.util.ArrayList;

import me.majiajie.photoalbum.R;

/**
 * 手机本地照片/视频的数据加载
 */
public class PhotoAndVideoDataLoadFragment extends Fragment {

    private static final String ARG_FILTER = "ARG_FILTER";

    private static final String ARG_SHOW = "ARG_SHOW";

    private static final String ARG_SHOW_VIDEO = "ARG_SHOW_VIDEO";

    private static final String ARG_SHOW_PHOTO = "ARG_SHOW_PHOTO";

    private Context mContext;

    /**
     * 需要过滤的文件后缀
     */
    private String[] mFilter;

    /**
     * 需要显示的文件后缀
     */
    private String[] mShow;

    /**
     * 是否显示视频
     */
    private boolean mShowVideo;

    /**
     * 是否显示图片
     */
    private boolean mShowPhoto;

    private PhotosLoadCallBack mPhotosCallBack;

    /**
     * 数据加载回调
     */
    public interface PhotosLoadCallBack {

        /**
         * 图片数据加载完成
         */
        void onPhotosLoadFinished(ArrayList<AlbumFolderBean> photosFolders);
    }

    /**
     * 创建图片列表加载的实例
     *
     * @param show      需要显示的图片类型(优先)
     * @param filter    需要过滤的图片类型
     * @param showVideo 是否显示视频
     * @param showPhoto 是否显示图片
     */
    public static PhotoAndVideoDataLoadFragment newInstance(String[] show, String[] filter, boolean showVideo, boolean showPhoto) {
        PhotoAndVideoDataLoadFragment fragment = new PhotoAndVideoDataLoadFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(ARG_FILTER, filter);
        bundle.putStringArray(ARG_SHOW, show);
        bundle.putBoolean(ARG_SHOW_VIDEO, showVideo);
        bundle.putBoolean(ARG_SHOW_PHOTO, showPhoto);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof PhotosLoadCallBack) {
            mPhotosCallBack = (PhotosLoadCallBack) context;
        } else {
            throw new ClassCastException(context.toString() + " must implemented PhotosLoadCallBack");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mFilter = bundle.getStringArray(ARG_FILTER);
        mShow = bundle.getStringArray(ARG_SHOW);
        mShowVideo = bundle.getBoolean(ARG_SHOW_VIDEO);
        mShowPhoto = bundle.getBoolean(ARG_SHOW_PHOTO);
        LoaderManager.getInstance(this).initLoader(0, null, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        mPhotosCallBack = null;
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final int ID = 0;
        private final int DATA = 1;
        private final int DATE_ADDED = 2;
        private final int DATE_MODIFIED = 3;
        private final int DISPLAY_NAME = 4;
        private final int HEIGHT = 5;
        private final int WIDTH = 6;
        private final int MIME_TYPE = 7;
        private final int SIZE = 8;
        private final int MEDIA_TYPE = 9;

        private final String[] FILE_PROJECTION = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.HEIGHT,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };

        private final String[] IMAGE_PROJECTION = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.HEIGHT,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE
        };

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (mShowVideo) {
                String selection;

                if (mShowPhoto) {
                    selection = "( " + FILE_PROJECTION[MEDIA_TYPE] + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                            + FILE_PROJECTION[MEDIA_TYPE] + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + " ) AND "
                            + MediaStore.MediaColumns.SIZE + "> 0";
                } else {
                    selection = FILE_PROJECTION[MEDIA_TYPE] + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO +
                            " AND " + MediaStore.MediaColumns.SIZE + "> 0";
                }

                Uri queryUri = MediaStore.Files.getContentUri("external");

                String sortOrder = FILE_PROJECTION[DATE_MODIFIED] + " DESC";

                return new CursorLoader(mContext, queryUri, FILE_PROJECTION, selection, null, sortOrder);
            } else {
                String selection = MediaStore.MediaColumns.SIZE + "> 0";

                Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                String sortOrder = IMAGE_PROJECTION[DATE_MODIFIED] + " DESC";

                //数据库查询
                return new CursorLoader(mContext, queryUri, IMAGE_PROJECTION, selection, null, sortOrder);
            }
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> arg0, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                //目录
                ArrayList<AlbumFolderBean> folderList = new ArrayList<>();
                //所有文件
                ArrayList<AlbumFileBean> allFileList = new ArrayList<>();
                // 视频文件夹
                AlbumFolderBean videoFloder = new AlbumFolderBean("**/video/**");
                videoFloder.setName(getString(R.string.photoalbum_folder_all_video));

                do {
                    String path = cursor.getString(DATA);
                    String mime_type = cursor.getString(MIME_TYPE);
                    String name = cursor.getString(DISPLAY_NAME);
                    long date_add = cursor.getLong(DATE_ADDED);
                    long date_modified = cursor.getLong(DATE_MODIFIED);
                    long id = cursor.getLong(ID);
                    long size = cursor.getLong(SIZE);
                    long width = cursor.getLong(WIDTH);
                    long height = cursor.getLong(HEIGHT);
                    boolean video = false;

                    if (mShowVideo) {
                        video = cursor.getInt(MEDIA_TYPE) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                    }

                    if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(name) && !isFilter(mime_type, video)) {

                        AlbumFileBean file = new AlbumFileBean(id, path, name, mime_type, width, height, size, date_add, date_modified, video);
                        allFileList.add(file);

                        if (file.isVideo()) {
                            if (videoFloder.getFiles() == null) {
                                videoFloder.setFiles(new ArrayList<AlbumFileBean>());
                            }
                            videoFloder.getFiles().add(file);
                        } else {
                            File imageParentFile = new File(path).getParentFile();
                            if (imageParentFile == null) {
                                continue;
                            }
                            AlbumFolderBean floder = new AlbumFolderBean(imageParentFile.getAbsolutePath());

                            if (folderList.contains(floder)) {// 存在文件夹
                                AlbumFolderBean f = folderList.get(folderList.indexOf(floder));
                                f.getFiles().add(file);
                            } else {// 新文件夹
                                floder.setName(imageParentFile.getName());

                                ArrayList<AlbumFileBean> newList = new ArrayList<>();
                                newList.add(file);
                                floder.setFiles(newList);

                                folderList.add(floder);
                            }
                        }
                    }
                } while (cursor.moveToNext());

                // 如果存在视频就添加视频目录
                if (videoFloder.getFiles() != null && !videoFloder.getFiles().isEmpty()) {
                    folderList.add(0, videoFloder);
                }

                //将所有文件添加到第一个文件夹
                if (mShowPhoto && allFileList.size() > 0) {
                    AlbumFolderBean floder = new AlbumFolderBean("**/storage/**");//这个目录随便写的(但是不要与别的目录重复)
                    floder.setName(mShowVideo ? getResources().getString(R.string.photoalbum_folder_all_file) : getResources().getString(R.string.photoalbum_folder_all_photo));
                    floder.setFiles(allFileList);

                    folderList.add(0, floder);
                }

                mPhotosCallBack.onPhotosLoadFinished(folderList);

            } else {
                if (mContext != null) {
                    //没有数据或异常
                    Toast.makeText(mContext, R.string.photoalbum_hint_no_photo, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> arg0) {
        }
    };

    /**
     * 判断是否过滤
     *
     * @param mime_type 类型
     * @param video     是否为视频
     * @return true 过滤
     */
    private boolean isFilter(String mime_type, boolean video) {

        if (video) {
            return false;
        }

        if (mShow != null) {
            for (String show : mShow) {
                if (TextUtils.equals(mime_type, show)) {
                    return false;
                }
            }
        } else if (mFilter != null) {
            for (String filte : mFilter) {
                if (TextUtils.equals(mime_type, filte)) {
                    return true;
                }
            }
        }

        return mShow != null;
    }
}

package me.majiajie.photoalbum.photo;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import me.majiajie.photoalbum.R;

/**
 * 手机本地照片的数据加载
 */
public class PhotoDataLoadFragment extends Fragment {

    private static final String ARG_FILTER = "ARG_FILTER";

    private static final String ARG_SHOW = "ARG_SHOW";

    private Context mContext;

    /**
     * 需要过滤的文件后缀
     */
    private String[] mFilter;

    /**
     * 需要显示的文件后缀
     */
    private String[] mShow;

    private ArrayList<PhotosFolder> mPhotosFolderList;

    private PhotosLoadCallBack mPhotosCallBack;

    /**
     * 数据加载回调
     */
    public interface PhotosLoadCallBack {

        /**
         * 图片数据加载完成
         */
        void onPhotosLoadFinished(ArrayList<PhotosFolder> photosFolders);
    }

    /**
     * 创建图片列表加载的实例
     * @param show      需要显示的图片类型(优先)
     * @param filter    需要过滤的图片类型
     */
    public static PhotoDataLoadFragment newInstance(String[] show,String[] filter) {
        PhotoDataLoadFragment fragment = new PhotoDataLoadFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(ARG_FILTER, filter);
        bundle.putStringArray(ARG_SHOW, show);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof PhotosLoadCallBack){
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
        ((FragmentActivity) mContext).getSupportLoaderManager().initLoader(0, null, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
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

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            //数据库查询
            return new CursorLoader(mContext,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    MediaStore.Images.Media.SIZE + ">?", new String[]{"0"}, IMAGE_PROJECTION[DATE_MODIFIED] + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                //分文件夹的所有图片
                ArrayList<PhotosFolder> photosFolderList = new ArrayList<>();
                //所有图片
                ArrayList<Photo> allPhotoList = new ArrayList<>();

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

                    if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(name) && !isFilter(path)) {
                        Photo photo = new Photo(id,path,name,mime_type,width,height,size,date_add,date_modified);
                        allPhotoList.add(photo);

                        File imageParentFile = new File(path).getParentFile();
                        PhotosFolder floder = new PhotosFolder(imageParentFile.getAbsolutePath());

                        if (photosFolderList.contains(floder)) {// 存在文件夹
                            PhotosFolder f = photosFolderList.get(photosFolderList.indexOf(floder));
                            f.getImages().add(photo);
                        } else {// 新文件夹
                            floder.setName(imageParentFile.getName());
                            floder.setFirstImage(photo.getPath());

                            ArrayList<Photo> newList = new ArrayList<>();
                            newList.add(photo);
                            floder.setImages(newList);

                            photosFolderList.add(floder);
                        }
                    }
                } while (cursor.moveToNext());

                //将所有图片添加到第一个文件夹
                if (allPhotoList.size() > 0) {
                    PhotosFolder floder = new PhotosFolder("**/storage/**");//这个目录随便写的(但是不要与别的目录重复)
                    floder.setName(getResources().getString(R.string.photoalbum_folder_all_photo));
                    floder.setFirstImage(allPhotoList.get(0).getPath());
                    floder.setImages(allPhotoList);

                    photosFolderList.add(0, floder);
                }

                mPhotosFolderList = photosFolderList;

                if (mPhotosCallBack != null) {
                    mPhotosCallBack.onPhotosLoadFinished(mPhotosFolderList);
                }

            } else {
                //没有数据或异常
                Toast.makeText(mContext, R.string.photoalbum_hint_no_photo, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {}
    };

    /**
     * 判断是否过滤
     * @param path 图片地址
     * @return true 过滤
     */
    private boolean isFilter(@NonNull String path) {

        if (!(new File(path).exists())) {
            return false;
        }

        if (mShow != null) {
            for (String show : mShow) {
                if (path.toLowerCase().endsWith(show)) {
                    return false;
                }
            }
        } else if (mFilter != null) {
            for (String filte : mFilter) {
                if (path.toLowerCase().endsWith(filte)) {
                    return true;
                }
            }
        }

        return mShow != null;
    }
}

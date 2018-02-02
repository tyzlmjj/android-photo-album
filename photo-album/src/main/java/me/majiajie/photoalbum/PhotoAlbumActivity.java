package me.majiajie.photoalbum;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import me.majiajie.photoalbum.helper.RequestPermissionFragment;
import me.majiajie.photoalbum.photo.Photo;
import me.majiajie.photoalbum.photo.PhotoDataLoadFragment;
import me.majiajie.photoalbum.photo.PhotoListFragment;
import me.majiajie.photoalbum.photo.PhotosFolder;

/**
 * 自定义相册
 */
public class PhotoAlbumActivity extends AppCompatActivity implements PhotoDataLoadFragment.PhotosLoadCallBack, RequestPermissionFragment.RequestPermissionsCallback, PhotoListFragment.PhotoListListener {

    public static final int REQUEST_CODE = 111;

    private static final String ARG_DATA = "ARG_DATA";

    private static final String ARG_RESULT = "ARG_RESULT";

    private final String TAG_PHOTO_LOAD = "TAG_PHOTO_LOAD";

    private final String TAG_REQUEST_PERMISSION = "TAG_REQUEST_PERMISSION";

    /**
     * 读取外部文件的权限
     */
    private final String[] READ_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private Button mBtnDone;

    /**
     * 用于检查读取权限
     */
    private RequestPermissionFragment mRequestPermissionFragment;

    /**
     * 用于显示图片
     */
    private PhotoListFragment mPhotoListFragment;

    /**
     * 选中的图片
     */
    private ArrayList<Photo> mSelectedPhotos = new ArrayList<>();

    /**
     * 请求的参数
     */
    private RequestData mRequestData;

    /**
     * 启动自定义相册页面
     *
     * @param fragment {@link Fragment}
     * @param data     请求的参数
     */
    public static void startActivityForResult(Fragment fragment, RequestData data) {
        startActivityForResult(fragment,data,REQUEST_CODE);
    }

    /**
     * 启动自定义相册页面
     *
     * @param fragment {@link Fragment}
     * @param data     请求的参数
     * @param requestCode 请求代码
     */
    public static void startActivityForResult(Fragment fragment, RequestData data,int requestCode) {
        Intent intent = new Intent(fragment.getContext(), PhotoAlbumActivity.class);
        intent.putExtra(ARG_DATA, data);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动自定义相册页面
     *
     * @param activity {@link Activity}
     * @param data     请求的参数
     */
    public static void startActivityForResult(Activity activity, RequestData data) {
        startActivityForResult(activity, data, REQUEST_CODE);
    }

    /**
     * 启动自定义相册页面
     *
     * @param activity    {@link Activity}
     * @param data        请求的参数
     * @param requestCode 请求代码
     */
    public static void startActivityForResult(Activity activity, RequestData data, int requestCode) {
        Intent intent = new Intent(activity, PhotoAlbumActivity.class);
        intent.putExtra(ARG_DATA, data);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 获取返回数据
     *
     * @param data {@link Activity#onActivityResult(int, int, Intent)}的第三个参数
     * @return 图片和相关数据
     */
    public static ResultData getResult(Intent data) {
        return data.getParcelableExtra(ARG_RESULT);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof PhotoListFragment) {
            mPhotoListFragment = (PhotoListFragment) fragment;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // 传入数据解析
        mRequestData = getIntent().getParcelableExtra(ARG_DATA);
        // 设置主题
        setTheme(mRequestData.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoalbum_activity_photoalbum);

        initToolbar();

        mBtnDone = findViewById(R.id.btn_done);

        // 添加请求权限的Fragment
        mRequestPermissionFragment = (RequestPermissionFragment) getSupportFragmentManager().findFragmentByTag(TAG_REQUEST_PERMISSION);
        if (mRequestPermissionFragment == null) {
            mRequestPermissionFragment = RequestPermissionFragment.newInstance(READ_PERMISSION, getString(R.string.photoalbum_hint_need_read_permission));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(mRequestPermissionFragment, TAG_REQUEST_PERMISSION).commit();
        }

        // 点击完成按钮
        mBtnDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                done(mSelectedPhotos);
            }
        });

        mPhotoListFragment.showFullImageBtn(mRequestData.isShowFullImageBtn());
    }

    private boolean once = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (once) {
            once = false;
            // 检查读取权限
            if (mRequestPermissionFragment.checkPermissions()) {
                addLoadDataFragment();
            } else {
                mRequestPermissionFragment.requestPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoPreviewActivity.REQUEST_CODE) {

            PhotoPreviewActivity.PhotoPreviewResult previewResult = PhotoPreviewActivity.getResult(data);

            mSelectedPhotos = previewResult.getSelectedPhoto();
            mPhotoListFragment.modifySelectedPhotos(mSelectedPhotos);
            mPhotoListFragment.setUseFullImage(previewResult.isUseOriginal());
            refreshDoneBtnText(mSelectedPhotos.size());

            if (resultCode == Activity.RESULT_OK) {
                done(mSelectedPhotos);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mPhotoListFragment.canBack()) {
            mPhotoListFragment.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(boolean grantResult) {
        if (grantResult) {
            addLoadDataFragment();
        } else {
            Toast.makeText(this, R.string.photoalbum_hint_no_read_permission, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPhotosLoadFinished(ArrayList<PhotosFolder> photosFolders) {
        mPhotoListFragment.setPhotoList(photosFolders, mRequestData.getMaxPhotoNumber());
    }

    @Override
    public void onSelectedPhotoChanged(ArrayList<Photo> selectedList) {
        mSelectedPhotos = selectedList;
        refreshDoneBtnText(mSelectedPhotos.size());
    }

    @Override
    public void onClickPreview() {
        PhotoPreviewActivity.RequestData requestData = new PhotoPreviewActivity.RequestData(mSelectedPhotos, mRequestData.getMaxPhotoNumber(), mPhotoListFragment.isUseFullImage());
        requestData.setTheme(mRequestData.getTheme());
        requestData.setShowUseFullImageBtn(mRequestData.isShowFullImageBtn());
        PhotoPreviewActivity.startActivityForResult(this, requestData);
    }

    /**
     * 完成
     */
    private void done(ArrayList<Photo> photos) {
        if (photos != null && photos.size() > 0) {
            ResultData resultData = new ResultData(photos, mPhotoListFragment.isUseFullImage());
            Intent intent = new Intent();
            intent.putExtra(ARG_RESULT, resultData);
            setResult(Activity.RESULT_OK, intent);
            PhotoAlbumActivity.this.finish();
        } else {
            //TODO 没有选择图片

        }
    }

    /**
     * 初始化顶部栏
     */
    private void initToolbar() {
        // 设置Toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        // 显示返回按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 刷新完成按钮的文字
     *
     * @param size 选中图片的数量
     */
    private void refreshDoneBtnText(int size) {
        if (size == 0) {
            mBtnDone.setText(R.string.photoalbum_text_done);
            mBtnDone.setEnabled(false);
        } else {
            mBtnDone.setText(String.format(Locale.getDefault(), "%s(%d/%d)", getString(R.string.photoalbum_text_done), size, mRequestData.getMaxPhotoNumber()));
            mBtnDone.setEnabled(true);
        }
    }

    /**
     * 添加加载图片数据的Fragment
     */
    private void addLoadDataFragment() {
        PhotoDataLoadFragment photoDataLoadFragment = (PhotoDataLoadFragment) getSupportFragmentManager().findFragmentByTag(TAG_PHOTO_LOAD);
        if (photoDataLoadFragment == null) {
            photoDataLoadFragment = PhotoDataLoadFragment.newInstance(mRequestData.getShowImageMimeType(), mRequestData.getFilterImageMimeType());
            getSupportFragmentManager().beginTransaction()
                    .add(photoDataLoadFragment, TAG_PHOTO_LOAD).commitAllowingStateLoss();
        }
    }

    /**
     * 返回数据
     */
    public static class ResultData implements Parcelable {

        /**
         * 选择的图片数据
         */
        private ArrayList<Photo> photos;

        /**
         * 是否使用原图
         */
        private boolean fullImage;

        ResultData(ArrayList<Photo> photos, boolean fullImage) {
            this.photos = photos;
            this.fullImage = fullImage;
        }

        protected ResultData(Parcel in) {
            photos = in.createTypedArrayList(Photo.CREATOR);
            fullImage = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(photos);
            dest.writeByte((byte) (fullImage ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ResultData> CREATOR = new Creator<ResultData>() {
            @Override
            public ResultData createFromParcel(Parcel in) {
                return new ResultData(in);
            }

            @Override
            public ResultData[] newArray(int size) {
                return new ResultData[size];
            }
        };

        public ArrayList<Photo> getPhotos() {
            return photos;
        }

        public void setPhotos(ArrayList<Photo> photos) {
            this.photos = photos;
        }

        public boolean isFullImage() {
            return fullImage;
        }

        public void setFullImage(boolean fullImage) {
            this.fullImage = fullImage;
        }
    }

    /**
     * {@link PhotoAlbumActivity} 的请求数据
     */
    public static class RequestData implements Parcelable {

        /**
         * 可选择的最大图片数量
         */
        private int maxPhotoNumber = 9;

        /**
         * Activity主题
         */
        @StyleRes
        private int theme = R.style.PhotoAlbumTheme;

        /**
         * 是否显示选择"原图"的按钮
         */
        private boolean showFullImageBtn = true;

        /**
         * 需要显示的图片MIME类型.常用的类型：
         * <p>
         * JPG图片: image/jpeg
         * PNG图片: image/png
         * GIF图片: image/gif
         * </p>
         */
        private String[] showImageMimeType;

        /**
         * 需要过滤的图片MIME类型.如果{@link RequestData#showImageMimeType}不为空,此字段无效
         */
        private String[] filterImageMimeType;

        public RequestData(){}

        protected RequestData(Parcel in) {
            maxPhotoNumber = in.readInt();
            theme = in.readInt();
            showFullImageBtn = in.readByte() != 0;
            showImageMimeType = in.createStringArray();
            filterImageMimeType = in.createStringArray();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(maxPhotoNumber);
            dest.writeInt(theme);
            dest.writeByte((byte) (showFullImageBtn ? 1 : 0));
            dest.writeStringArray(showImageMimeType);
            dest.writeStringArray(filterImageMimeType);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<RequestData> CREATOR = new Creator<RequestData>() {
            @Override
            public RequestData createFromParcel(Parcel in) {
                return new RequestData(in);
            }

            @Override
            public RequestData[] newArray(int size) {
                return new RequestData[size];
            }
        };

        public int getMaxPhotoNumber() {
            return maxPhotoNumber;
        }

        public void setMaxPhotoNumber(int maxPhotoNumber) {
            this.maxPhotoNumber = maxPhotoNumber;
        }

        public int getTheme() {
            return theme;
        }

        public void setTheme(int theme) {
            this.theme = theme;
        }

        public boolean isShowFullImageBtn() {
            return showFullImageBtn;
        }

        public void setShowFullImageBtn(boolean showFullImageBtn) {
            this.showFullImageBtn = showFullImageBtn;
        }

        public String[] getShowImageMimeType() {
            return showImageMimeType;
        }

        public void setShowImageMimeType(String[] showImageMimeType) {
            this.showImageMimeType = showImageMimeType;
        }

        public String[] getFilterImageMimeType() {
            return filterImageMimeType;
        }

        public void setFilterImageMimeType(String[] filterImageMimeType) {
            this.filterImageMimeType = filterImageMimeType;
        }
    }

}

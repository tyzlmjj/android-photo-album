package me.majiajie.photoalbum;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Locale;

import me.majiajie.photoalbum.helper.RequestPermissionFragment;
import me.majiajie.photoalbum.data.AlbumFileBean;
import me.majiajie.photoalbum.data.PhotoAndVideoDataLoadFragment;
import me.majiajie.photoalbum.view.AlbumListFragment;
import me.majiajie.photoalbum.data.AlbumFolderBean;

/**
 * 自定义相册
 */
public class AlbumActivity extends AppCompatActivity implements PhotoAndVideoDataLoadFragment.PhotosLoadCallBack, RequestPermissionFragment.RequestPermissionsCallback, AlbumListFragment.PhotoListListener {

    public static final int REQUEST_CODE = 111;

    private static final String ARG_DATA = "ARG_DATA";

    private static final String ARG_RESULT = "ARG_RESULT";

    private static final String TAG_PHOTO_LOAD = "TAG_PHOTO_LOAD";

    private static final String TAG_REQUEST_PERMISSION = "TAG_REQUEST_PERMISSION";

    private static final String TAG_COMPLETE_FRAGMENT = "TAG_COMPLETE_FRAGMENT";

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
    private AlbumListFragment mPhotoListFragment;

    /**
     * 选中的图片
     */
    private ArrayList<AlbumFileBean> mSelectedPhotos = new ArrayList<>();

    /**
     * 请求的参数
     */
    private RequestData mRequestData;

    /**
     * 用于处理结果的Fragment(不一定存在)
     */
    private BaseCompleteFragment mBaseCompleteFragment;

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
        Intent intent = new Intent(fragment.getContext(), AlbumActivity.class);
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
        Intent intent = new Intent(activity, AlbumActivity.class);
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
        if (fragment instanceof AlbumListFragment) {
            mPhotoListFragment = (AlbumListFragment) fragment;
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

        // 设置显示或隐藏原图按钮
        mPhotoListFragment.showFullImageBtn(mRequestData.isShowFullImageBtn());

        // 如果传入了Fragment类名字段就添加Fragment
        if (!TextUtils.isEmpty(mRequestData.getFragmentClassName())){
            try {
                mBaseCompleteFragment = (BaseCompleteFragment) getSupportFragmentManager().findFragmentByTag(TAG_COMPLETE_FRAGMENT);
                if (mBaseCompleteFragment == null){
                    mBaseCompleteFragment = (BaseCompleteFragment) Class.forName(mRequestData.getFragmentClassName()).newInstance();
                    getSupportFragmentManager().beginTransaction()
                            .add(mBaseCompleteFragment,TAG_COMPLETE_FRAGMENT)
                            .commit();
                }
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
        if (data != null && requestCode == PhotoPreviewActivity.REQUEST_CODE) {

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
            AlbumActivity.this.finish();
        }
    }

    @Override
    public void onPhotosLoadFinished(ArrayList<AlbumFolderBean> photosFolders) {
        mPhotoListFragment.setPhotoList(photosFolders, mRequestData);
    }

    @Override
    public void onSelectedPhotoChanged(ArrayList<AlbumFileBean> selectedList) {
        mSelectedPhotos = selectedList;
        refreshDoneBtnText(mSelectedPhotos.size());
    }

    @Override
    public void onSingleSelected(AlbumFileBean file) {
        ArrayList<AlbumFileBean> files = new ArrayList<>();
        files.add(file);
        setResult(files);
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
    private void done(ArrayList<AlbumFileBean> photos) {
        if (photos != null && photos.size() > 0) {
            setResult(photos);
        }
    }

    /**
     * 返回数据
     */
    private void setResult(ArrayList<AlbumFileBean> photos) {
        ResultData resultData = new ResultData(photos, mPhotoListFragment.isUseFullImage());
        if (mBaseCompleteFragment != null){
            mBaseCompleteFragment.onResultData(resultData);
        } else {
            Intent intent = new Intent();
            intent.putExtra(ARG_RESULT, resultData);
            setResult(Activity.RESULT_OK, intent);
            AlbumActivity.this.finish();
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
            int maxNumber;
            if (mRequestData.isSinglePhoto() && mRequestData.isSingleVideo()){
                // 这种情况不会出现，因为数据会直接返回
                maxNumber = 1;
            } else if(mRequestData.getMaxNumber() > 0){
                maxNumber = mRequestData.getMaxNumber();
            } else {
                int maxPhotoNumber = mRequestData.isSinglePhoto() ? 1 : mRequestData.maxPhotoNumber;
                int maxVoideNumber = mRequestData.isSingleVideo() ? 1 : mRequestData.maxVideoNumber;
                maxNumber = maxPhotoNumber + maxVoideNumber;
            }

            mBtnDone.setText(String.format(Locale.getDefault(), "%s(%d/%d)", getString(R.string.photoalbum_text_done), size, maxNumber));
            mBtnDone.setEnabled(true);
        }
    }

    /**
     * 添加加载图片数据的Fragment
     */
    private void addLoadDataFragment() {
        if (mRequestData.isShowPhoto() || mRequestData.isShowVideo()){
            PhotoAndVideoDataLoadFragment photoDataLoadFragment = (PhotoAndVideoDataLoadFragment) getSupportFragmentManager().findFragmentByTag(TAG_PHOTO_LOAD);
            if (photoDataLoadFragment == null) {
                photoDataLoadFragment = PhotoAndVideoDataLoadFragment.newInstance(mRequestData.getShowImageMimeType(), mRequestData.getFilterImageMimeType(), mRequestData.isShowVideo(),mRequestData.isShowPhoto());
                getSupportFragmentManager().beginTransaction()
                        .add(photoDataLoadFragment, TAG_PHOTO_LOAD).commitAllowingStateLoss();
            }
        } else {
            Toast.makeText(this,"选择数量都为零！你在逗我吗？",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 返回数据
     */
    public static class ResultData implements Parcelable {

        /**
         * 选择的图片数据
         */
        private ArrayList<AlbumFileBean> photos;

        /**
         * 是否使用原图
         */
        private boolean fullImage;

        ResultData(ArrayList<AlbumFileBean> photos, boolean fullImage) {
            this.photos = photos;
            this.fullImage = fullImage;
        }

        protected ResultData(Parcel in) {
            photos = in.createTypedArrayList(AlbumFileBean.CREATOR);
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

        public ArrayList<AlbumFileBean> getPhotos() {
            return photos;
        }

        public void setPhotos(ArrayList<AlbumFileBean> photos) {
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
     * {@link AlbumActivity} 的请求数据
     */
    public static class RequestData implements Parcelable {

        /**
         * 可选的最大数量(包括图片和视频)
         * 注意: 当此变量小于等于0时，使用maxPhotoNumber和maxVideoNumber。
         *      当此变量大于0时，忽略maxPhotoNumber和maxVideoNumber。
         */
        private int maxNumber = 0;

        /**
         * 可选择的最大图片数量
         */
        private int maxPhotoNumber = 9;

        /**
         * 可选的最大视频数量
         */
        private int maxVideoNumber = 0;

        /**
         * 单独选择一张图片的模式
         * true,图片项不会显示选择标记,点击图片直接返回
         */
        private boolean singlePhoto = false;

        /**
         * 单独选择一个视频的模式
         * true,视频项不会显示选择标记,点击视频直接返回
         */
        private boolean singleVideo = false;

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

        /**
         * 一个继承{@link BaseCompleteFragment}的Fragment的全名,用于对选择图片的结果进行处理，
         * 而不是默认的返回到上一个Activity
         */
        private String fragmentClassName;

        public RequestData(){}

        public boolean isShowVideo() {
            return isSingleVideo() || maxVideoNumber > 0 || maxNumber > 0;
        }

        public boolean isShowPhoto() {
            return isSinglePhoto() || maxPhotoNumber > 0 || maxNumber > 0;
        }

        protected RequestData(Parcel in) {
            maxNumber = in.readInt();
            maxPhotoNumber = in.readInt();
            maxVideoNumber = in.readInt();
            singlePhoto = in.readByte() != 0;
            singleVideo = in.readByte() != 0;
            theme = in.readInt();
            showFullImageBtn = in.readByte() != 0;
            showImageMimeType = in.createStringArray();
            filterImageMimeType = in.createStringArray();
            fragmentClassName = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(maxNumber);
            dest.writeInt(maxPhotoNumber);
            dest.writeInt(maxVideoNumber);
            dest.writeByte((byte) (singlePhoto ? 1 : 0));
            dest.writeByte((byte) (singleVideo ? 1 : 0));
            dest.writeInt(theme);
            dest.writeByte((byte) (showFullImageBtn ? 1 : 0));
            dest.writeStringArray(showImageMimeType);
            dest.writeStringArray(filterImageMimeType);
            dest.writeString(fragmentClassName);
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

        public int getMaxNumber() {
            return maxNumber;
        }

        public void setMaxNumber(int maxNumber) {
            this.maxNumber = Math.max(0,maxNumber);
        }

        public int getMaxPhotoNumber() {
            return maxPhotoNumber;
        }

        public void setMaxPhotoNumber(int maxPhotoNumber) {
            this.maxPhotoNumber = Math.max(0,maxPhotoNumber);
        }

        public int getMaxVideoNumber() {
            return maxVideoNumber;
        }

        public void setMaxVideoNumber(int maxVideoNumber) {
            this.maxVideoNumber = Math.max(0,maxVideoNumber);
        }

        public boolean isSinglePhoto() {
            return singlePhoto;
        }

        public void setSinglePhoto(boolean singlePhoto) {
            this.singlePhoto = singlePhoto;
        }

        public boolean isSingleVideo() {
            return singleVideo;
        }

        public void setSingleVideo(boolean singleVideo) {
            this.singleVideo = singleVideo;
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

        public String getFragmentClassName() {
            return fragmentClassName;
        }

        public void setFragmentClassName(String fragmentClassName) {
            this.fragmentClassName = fragmentClassName;
        }
    }

}

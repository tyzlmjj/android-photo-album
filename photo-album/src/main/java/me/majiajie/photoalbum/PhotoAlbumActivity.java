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
public class PhotoAlbumActivity extends AppCompatActivity implements PhotoDataLoadFragment.PhotosLoadCallBack, RequestPermissionFragment.RequestPermissionsCallback,PhotoListFragment.PhotoListListener{

    public static final int REQUEST_CODE = 111;

    private static final String ARG_DATA = "ARG_DATA";

    private final String TAG_PHOTO_LOAD = "TAG_PHOTO_LOAD";

    private final String TAG_REQUEST_PERMISSION = "TAG_REQUEST_PERMISSION";

    /**
     * 读取外部文件的权限
     */
    private final String[] READ_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private Button mBtnDone;

    /**
     * 用于检查读取权限
     */
    private RequestPermissionFragment mRequestPermissionFragment;

    /**
     * 用于加载手机图片数据
     */
    private PhotoDataLoadFragment mPhotoDataLoadFragment;

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
     * @param activity  {@link Activity}
     * @param data      请求的参数
     */
    public static void startActivityForResult(Activity activity,RequestData data){
        startActivityForResult(activity,data,REQUEST_CODE);
    }

    /**
     * 启动自定义相册页面
     * @param activity      {@link Activity}
     * @param data          请求的参数
     * @param requestCode   请求代码
     */
    public static void startActivityForResult(Activity activity,RequestData data,int requestCode){
        Intent intent = new Intent(activity,PhotoAlbumActivity.class);
        intent.putExtra(ARG_DATA,data);
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof PhotoListFragment){
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
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoPreviewActivity.REQUEST_CODE){
            PhotoPreviewActivity.PhotoPreviewResult previewResult = PhotoPreviewActivity.getResult(data);
            if (previewResult.isDone()){

            } else {
                mSelectedPhotos = previewResult.getSelectedPhoto();
                mPhotoListFragment.modifySelectedPhotos(mSelectedPhotos);
                mPhotoListFragment.setUseOriginal(previewResult.isUseOriginal());
                refreshDoneBtnText(mSelectedPhotos.size());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mPhotoListFragment.canBack()){
            mPhotoListFragment.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(boolean grantResult) {
        if (grantResult){
            addLoadDataFragment();
        } else {
            Toast.makeText(this,R.string.photoalbum_hint_no_read_permission,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPhotosLoadFinished(ArrayList<PhotosFolder> photosFolders) {
        mPhotoListFragment.setPhotoList(photosFolders,mRequestData.getMaxPhotoNumber());
    }

    @Override
    public void onSelectedPhotoChanged(ArrayList<Photo> selectedList) {
        mSelectedPhotos = selectedList;
        refreshDoneBtnText(mSelectedPhotos.size());
    }

    @Override
    public void onClickPreview() {
        PhotoPreviewActivity.RequestData requestData = new PhotoPreviewActivity.RequestData(mSelectedPhotos,mRequestData.getMaxPhotoNumber(),mPhotoListFragment.useOriginal());
        requestData.setTheme(mRequestData.getTheme());
        PhotoPreviewActivity.startActivityForResult(this,requestData);
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
     * @param size 选中图片的数量
     */
    private void refreshDoneBtnText(int size) {
        if (size == 0){
            mBtnDone.setText(R.string.photoalbum_text_done);
            mBtnDone.setEnabled(false);
        } else {
            mBtnDone.setText(String.format(Locale.getDefault(),"%s(%d/%d)",getString(R.string.photoalbum_text_done),size,mRequestData.getMaxPhotoNumber()));
            mBtnDone.setEnabled(true);
        }
    }

    /**
     * 添加加载图片数据的Fragment
     */
    private void addLoadDataFragment() {
        PhotoDataLoadFragment photoDataLoadFragment = (PhotoDataLoadFragment) getSupportFragmentManager().findFragmentByTag(TAG_PHOTO_LOAD);
        if (photoDataLoadFragment == null){
            photoDataLoadFragment = PhotoDataLoadFragment.newInstance(new String[]{"gif"},null);
            getSupportFragmentManager().beginTransaction()
                    .add(photoDataLoadFragment,TAG_PHOTO_LOAD).commitAllowingStateLoss();
        }
        mPhotoDataLoadFragment = photoDataLoadFragment;
    }

    /**
     * {@link PhotoAlbumActivity} 的请求数据
     */
    public static class RequestData implements Parcelable{

        /**
         * 可选择的最大图片数量
         */
        private int maxPhotoNumber;

        /**
         * Activity主题
         */
        @StyleRes
        private int theme;

        public RequestData() {
            maxPhotoNumber = 9;
            theme = R.style.PhotoAlbumTheme;
        }

        protected RequestData(Parcel in) {
            maxPhotoNumber = in.readInt();
            theme = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(maxPhotoNumber);
            dest.writeInt(theme);
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
    }

}

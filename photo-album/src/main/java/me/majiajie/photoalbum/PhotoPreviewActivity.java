package me.majiajie.photoalbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Locale;

import me.majiajie.photoalbum.imgload.ImageLoader;
import me.majiajie.photoalbum.photo.Photo;
import me.majiajie.photoalbum.utils.AttrUtils;

/**
 * 图片选择时预览
 */
public class PhotoPreviewActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 112;

    public static final String ARG_REQUEST_DATA = "ARG_REQUEST_DATA";

    public static final String ARG_RESULT = "ARG_RESULT";

    private Button mBtnDone;
    private ViewPager mViewPager;
    private FrameLayout mLayoutBottom;
    private AppCompatCheckBox mCheckboxOriginal;
    private LinearLayout mBtnSelect;
    private ImageView mImgSelect;

    private ImageLoader mImageLoader;

    /**
     * 选中的图片
     */
    private ArrayList<Photo> mSelectedPhotos;

    /**
     * 传入的请求数据
     */
    private RequestData mRequestData;

    /**
     * 启动图片预览页面。这里默认了一个请求码{@link PhotoPreviewActivity#REQUEST_CODE REQUEST_CODE}
     * @param activity      {@link Activity}
     * @param requestData   选中的图片
     */
    public static void startActivityForResult(Activity activity, RequestData requestData){
        startActivityForResult(activity,requestData,REQUEST_CODE);
    }

    /**
     * 启动图片预览页面。
     * @param activity      {@link Activity}
     * @param requestData   选中的图片
     * @param requestCode   请求码
     */
    public static void startActivityForResult(Activity activity, RequestData requestData,int requestCode){
        Intent intent = new Intent(activity,PhotoPreviewActivity.class);
        intent.putExtra(ARG_REQUEST_DATA,requestData);
        activity.startActivityForResult(intent,requestCode);
    }

    /**
     * 获取返回结果
     * @param data  {@link Activity#onActivityResult(int, int, Intent)}的第三个参数
     * @return  预览界面的修改结果
     */
    public static PhotoPreviewResult getResult(Intent data){
        return data.getParcelableExtra(ARG_RESULT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 解析传入数据
        mRequestData = getIntent().getParcelableExtra(ARG_REQUEST_DATA);
        // 设置主题
        setTheme(mRequestData.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoalbum_activity_photopreview);
        initToolbar();

        mBtnDone = findViewById(R.id.btn_done);
        mViewPager = findViewById(R.id.viewPager);
        mLayoutBottom = findViewById(R.id.layout_bottom);
        mCheckboxOriginal = findViewById(R.id.checkbox_full_image);
        mBtnSelect = findViewById(R.id.btn_select);
        mImgSelect = findViewById(R.id.img_select);

        mImageLoader = new ImageLoader(this);

        // 选中的图片集合分开处理（因为只会传入选中的图片，所以这样简单处理了）
        mSelectedPhotos = new ArrayList<>(mRequestData.getPhotos());

        // 设置原图选择
        mCheckboxOriginal.setChecked(mRequestData.isUseFullImage());
        mCheckboxOriginal.setVisibility(mRequestData.isShowUseFullImageBtn() ? View.VISIBLE : View.INVISIBLE);

        // 显示图片
        mViewPager.setAdapter(new PhotosPagerAdapter());

        // 图片滑动切换监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                refreshTitle(position);
                Photo photo = mRequestData.getPhotos().get(position);
                changeSelecteImage(mSelectedPhotos.contains(photo));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // 选择按钮点击
        mBtnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Photo photo = mRequestData.getPhotos().get(mViewPager.getCurrentItem());
                if (mSelectedPhotos.contains(photo)){
                    mSelectedPhotos.remove(photo);
                    changeSelecteImage(false);
                } else {
                    mSelectedPhotos.add(photo);
                    changeSelecteImage(true);
                }
                refreshDoneText();
            }
        });

        // 完成按钮
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backResult(true);
            }
        });

        refreshDoneText();
        refreshTitle(0);
        changeSelecteImage(true);
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
        backResult(false);
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
     * 刷新标题
     */
    private void refreshTitle(int position) {
        setTitle(String.format(Locale.getDefault(),"%d/%d",position + 1,mRequestData.getPhotos().size()));
    }

    /**
     * 刷新完成按钮的文字
     */
    private void refreshDoneText() {
        int size = mSelectedPhotos.size();
        if (size == 0){
            mBtnDone.setText(R.string.photoalbum_text_done);
            mBtnDone.setEnabled(false);
        } else {
            mBtnDone.setText(String.format(Locale.getDefault(),"%s(%d/%d)",getString(R.string.photoalbum_text_done),size,mRequestData.getMaxPhotoNumber()));
            mBtnDone.setEnabled(true);
        }
    }

    /**
     * 改变选中按钮的图片
     */
    private void changeSelecteImage(boolean selected) {
        mImgSelect.setImageResource(selected ?
                AttrUtils.getResourceId(this,R.attr.photoalbum_checked_circle) :
                AttrUtils.getResourceId(this,R.attr.photoalbum_unchecked_circle));
    }

    /**
     * 返回结果
     * @param done 是否是点击完成按钮的返回
     */
    private void backResult(boolean done) {
        Intent i = new Intent();
        i.putExtra(ARG_RESULT,new PhotoPreviewResult(mSelectedPhotos,mCheckboxOriginal.isChecked()));
        setResult(done ? Activity.RESULT_OK : Activity.RESULT_CANCELED,i);
        PhotoPreviewActivity.this.finish();
    }

    /**
     * 图片适配器
     */
    private class PhotosPagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return mRequestData.getPhotos() == null ? 0 : mRequestData.getPhotos().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Photo photo = mRequestData.getPhotos().get(position);
            PhotoViewHolder holder = new PhotoViewHolder(container.getContext());
            container.addView(holder.itemView);
            mImageLoader.loadImage(photo.getPath(),holder.imageView);
            return holder.itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    class PhotoViewHolder{

        private View itemView;
        private ImageView imageView;

        PhotoViewHolder(Context context) {
            itemView = LayoutInflater.from(context).inflate(R.layout.photoalbum_item_preview_photo,null);
            imageView = itemView.findViewById(R.id.img_photo);
        }
    }

    /**
     * {@link PhotoPreviewActivity} 的请求数据
     */
    public static class RequestData implements Parcelable{

        /**
         * 需要预览的图片
         */
        private ArrayList<Photo> photos;

        /**
         * 可选择的最大图片数量
         */
        private int maxPhotoNumber;

        /**
         * 是否使用原图
         */
        private boolean useFullImage;

        /**
         * 是否显示选择"原图"的按钮
         */
        private boolean showUseFullImageBtn;

        /**
         * 主题
         */
        @StyleRes
        private int theme;

        public RequestData(ArrayList<Photo> photos, int maxPhotoNumber, boolean useFullImage) {
            this.photos = photos;
            this.maxPhotoNumber = maxPhotoNumber;
            this.useFullImage = useFullImage;
            theme = R.style.PhotoAlbumTheme;
        }


        protected RequestData(Parcel in) {
            photos = in.createTypedArrayList(Photo.CREATOR);
            maxPhotoNumber = in.readInt();
            useFullImage = in.readByte() != 0;
            showUseFullImageBtn = in.readByte() != 0;
            theme = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(photos);
            dest.writeInt(maxPhotoNumber);
            dest.writeByte((byte) (useFullImage ? 1 : 0));
            dest.writeByte((byte) (showUseFullImageBtn ? 1 : 0));
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

        public boolean isShowUseFullImageBtn() {
            return showUseFullImageBtn;
        }

        public void setShowUseFullImageBtn(boolean showUseFullImageBtn) {
            this.showUseFullImageBtn = showUseFullImageBtn;
        }

        public int getTheme() {
            return theme;
        }

        public void setTheme(int theme) {
            this.theme = theme;
        }

        public ArrayList<Photo> getPhotos() {
            return photos;
        }

        public void setPhotos(ArrayList<Photo> photos) {
            this.photos = photos;
        }

        public int getMaxPhotoNumber() {
            return maxPhotoNumber;
        }

        public void setMaxPhotoNumber(int maxPhotoNumber) {
            this.maxPhotoNumber = maxPhotoNumber;
        }

        public boolean isUseFullImage() {
            return useFullImage;
        }

        public void setUseFullImage(boolean useFullImage) {
            this.useFullImage = useFullImage;
        }
    }

    /**
     * 图片预览的返回数据
     */
    public static class PhotoPreviewResult implements Parcelable{

        /**
         * 选中的图片
         */
        private ArrayList<Photo> selectedPhoto;

        /**
         * 是否使用原图
         */
        private boolean useOriginal;

        PhotoPreviewResult(ArrayList<Photo> selectedPhoto, boolean useOriginal) {
            this.selectedPhoto = selectedPhoto;
            this.useOriginal = useOriginal;
        }

        protected PhotoPreviewResult(Parcel in) {
            selectedPhoto = in.createTypedArrayList(Photo.CREATOR);
            useOriginal = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(selectedPhoto);
            dest.writeByte((byte) (useOriginal ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PhotoPreviewResult> CREATOR = new Creator<PhotoPreviewResult>() {
            @Override
            public PhotoPreviewResult createFromParcel(Parcel in) {
                return new PhotoPreviewResult(in);
            }

            @Override
            public PhotoPreviewResult[] newArray(int size) {
                return new PhotoPreviewResult[size];
            }
        };

        public ArrayList<Photo> getSelectedPhoto() {
            return selectedPhoto;
        }

        public void setSelectedPhoto(ArrayList<Photo> selectedPhoto) {
            this.selectedPhoto = selectedPhoto;
        }

        public boolean isUseOriginal() {
            return useOriginal;
        }

        public void setUseOriginal(boolean useOriginal) {
            this.useOriginal = useOriginal;
        }

    }
}

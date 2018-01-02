package me.majiajie.photoalbum.photo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.majiajie.photoalbum.R;
import me.majiajie.photoalbum.utils.AttrUtils;
import me.majiajie.photoalbum.utils.ImageLoader;
import me.majiajie.photoalbum.view.ScaleImageView;
import me.majiajie.photoalbum.view.UpDownToggleDrawable;

/**
 * 相册图片显示
 */
public class PhotoListFragment extends Fragment {

    private Context mContext;

    private RecyclerView mRecyclerViewPhotos;
    private View mFolderLayoutBackground;
    private RecyclerView mRecyclerViewFolder;
    private Button mBtnSelectFolder;
    private AppCompatCheckBox mCheckboxOriginal;
    private Button mBtnPreview;

    /**
     * 底部上下变化的箭头
     */
    private UpDownToggleDrawable mUpDownToggleDrawable;

    /**
     * 所有图片目录
     */
    private ArrayList<PhotosFolder> mPhotoData;

    /**
     * 当前显示的目录中的图片数据
     */
    private List<Photo> mPhotos;

    /**
     * 记录选中的图片
     */
    private ArrayList<Photo> mPhotoSelectedList = new ArrayList<>();

    /**
     * 文件夹动画
     */
    private ValueAnimator mFolderAnim;

    /**
     * 记录选中的目录
     */
    private int mSelectedFolderIndex;

    /**
     * 最大可选数量
     */
    private int mMaxPhotos;

    /**
     * 图片列表事件
     */
    private PhotoListListener mListener;

    /**
     * 图片列表事件监听
     */
    public interface PhotoListListener {

        /**
         * 选中的图片变更
         */
        void onSelectedPhotoChanged(ArrayList<Photo> selectedList);

        /**
         * 点击预览按钮
         */
        void onClickPreview();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof PhotoListListener){
            mListener = (PhotoListListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implemented PhotoListListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photoalbum_fragment_photolist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerViewPhotos = view.findViewById(R.id.recyclerView_photos);
        mFolderLayoutBackground = view.findViewById(R.id.folder_layout_background);
        mRecyclerViewFolder = view.findViewById(R.id.recyclerView_folder);
        mBtnSelectFolder = view.findViewById(R.id.btn_select_folder);
        mCheckboxOriginal = view.findViewById(R.id.checkbox_original);
        mBtnPreview = view.findViewById(R.id.btn_preview);

        // 分隔线
        mRecyclerViewFolder.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerViewPhotos.setAdapter(new PhotoAdapter());
        mRecyclerViewFolder.setAdapter(new FolderAdapter());
        setupAnim();
        initBottomLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    /**
     * 设置图片数据源
     * @param photosFolderList  图片分目录数据
     * @param maxPhotos         最大图片选择数量
     */
    public void setPhotoList(ArrayList<PhotosFolder> photosFolderList,int maxPhotos) {
        mPhotoData = photosFolderList;
        mMaxPhotos = maxPhotos;

        mSelectedFolderIndex = 0;

        if (mPhotoData != null && mPhotoData.size() > 0) {
            mPhotos = mPhotoData.get(mSelectedFolderIndex).getImages();

            //刷新数据
            mRecyclerViewPhotos.getAdapter().notifyDataSetChanged();
            mRecyclerViewFolder.getAdapter().notifyDataSetChanged();

            //设置按钮显示的文件夹名字
            mBtnSelectFolder.setText(mPhotoData.get(mSelectedFolderIndex).getName());
        }
    }

    /**
     * 是否使用原图
     * @return true 使用原图
     */
    public boolean useOriginal(){
        return mCheckboxOriginal.isChecked();
    }

    /**
     * 设置是否使用原图
     */
    public void setUseOriginal(boolean useOriginal){
        mCheckboxOriginal.setChecked(useOriginal);
    }

    /**
     * 修改选中的图片
     */
    public void modifySelectedPhotos(ArrayList<Photo> selectedPhotos) {
        mPhotoSelectedList = selectedPhotos;
        mRecyclerViewPhotos.getAdapter().notifyDataSetChanged();
        refreshPreviewBtnText();
    }

    /**
     * 是否可回退
     */
    public boolean canBack(){
        return mUpDownToggleDrawable.isChecked();
    }

    /**
     * 回退
     */
    public void goBack(){
        mFolderAnim.reverse();
    }

    /**
     * 底部布局初始化
     */
    private void initBottomLayout() {
        // 设置箭头图标
        mUpDownToggleDrawable =
                new UpDownToggleDrawable(mContext, UpDownToggleDrawable.STATE_UP, ContextCompat.getColor(mContext,AttrUtils.getResourceId(mContext,R.attr.colorControlNormal)));
        mUpDownToggleDrawable.setBounds(0, 0,
                mUpDownToggleDrawable.getIntrinsicWidth(), mUpDownToggleDrawable.getIntrinsicHeight());
        mBtnSelectFolder.setCompoundDrawables(mUpDownToggleDrawable, null, null, null);

        // 点击目录按钮
        mBtnSelectFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoData != null) {
                    if (mUpDownToggleDrawable.isChecked()) {
                        mFolderAnim.reverse();
                    } else {
                        mFolderAnim.start();
                    }
                }
            }
        });

        // 点击背景-收回隐藏选择目录的视图
        mFolderLayoutBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFolderAnim.reverse();
            }
        });

        // 点击预览
        mBtnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onClickPreview();
                }
            }
        });
    }

    /**
     * 选择文件夹布局的显示隐藏动画
     */
    private void setupAnim() {
        //获取recyclerViewFiles的高并隐藏
        ViewTreeObserver vto = mRecyclerViewFolder.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRecyclerViewFolder.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                //先将recyclerViewFiles移动到底部
                mRecyclerViewFolder.setTranslationY(mRecyclerViewFolder.getHeight());
            }
        });

        //创建动画
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float n = (float) animation.getAnimatedValue();
                mRecyclerViewFolder.setTranslationY(mRecyclerViewFolder.getHeight() * n);
                mFolderLayoutBackground.setAlpha((1 - n) * 0.7f);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mUpDownToggleDrawable.toggle();
                super.onAnimationStart(animation);
                mRecyclerViewFolder.setVisibility(View.VISIBLE);
                mFolderLayoutBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mFolderLayoutBackground.getAlpha() == 0f) {
                    mRecyclerViewFolder.setVisibility(View.GONE);
                    mFolderLayoutBackground.setVisibility(View.GONE);
                }
            }
        });
        mFolderAnim = animator;
    }

    /**
     * 文件夹变更
     */
    private void folderChanged(int position) {
        mRecyclerViewFolder.getAdapter().notifyItemChanged(mSelectedFolderIndex);
        // 选中文件
        mSelectedFolderIndex = position;
        PhotosFolder folder = mPhotoData.get(mSelectedFolderIndex);
        // 设置按钮显示的文件夹名字
        mBtnSelectFolder.setText(folder.getName());
        // 记录文件夹图片
        mPhotos = folder.getImages();
        // 刷新图片适配器
        mRecyclerViewPhotos.getAdapter().notifyDataSetChanged();
        mFolderAnim.reverse();
    }

    /**
     * 选中图片
     */
    private void checkedPhoto(Photo photo) {
        mPhotoSelectedList.add(photo);
        if (mListener != null){
            mListener.onSelectedPhotoChanged(mPhotoSelectedList);
        }
        refreshPreviewBtnText();
    }

    /**
     * 取消选中图片
     */
    private void uncheckedPhoto(Photo photo) {
        mPhotoSelectedList.remove(photo);
        if (mListener != null){
            mListener.onSelectedPhotoChanged(mPhotoSelectedList);
        }
        refreshPreviewBtnText();
    }

    /**
     * 刷新预览按钮的文字
     */
    private void refreshPreviewBtnText() {
        int size = mPhotoSelectedList.size();
        if (size == 0){
            mBtnPreview.setText(R.string.photoalbum_text_preview);
            mBtnPreview.setEnabled(false);
        } else {
            mBtnPreview.setText(String.format(Locale.getDefault(),"%s(%d)",getString(R.string.photoalbum_text_preview),size));
            mBtnPreview.setEnabled(true);
        }
    }

    /**
     * 图片列表适配器
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.photoalbum_item_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final PhotoViewHolder holder, int position) {
            Photo photo = mPhotos.get(position);

            // 选中状态
            if (mPhotoSelectedList.contains(photo)){
                holder.check.setImageResource(AttrUtils.getResourceId(mContext,R.attr.photoalbum_checked_circle));
                holder.image.setChecked(true);
                holder.imageForground.setVisibility(View.INVISIBLE);
            } else {
                holder.check.setImageResource(R.drawable.photoalbum_unchecked_circle_white_24dp);
                holder.image.setChecked(false);
                holder.imageForground.setVisibility(View.VISIBLE);
            }
            ImageLoader.loadFileAndCenterCrop(holder.image,photo.getPath());

            // 点击选中
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Photo p = mPhotos.get(holder.getLayoutPosition());
                    if (mPhotoSelectedList.contains(p)){
                        holder.check.setImageResource(R.drawable.photoalbum_unchecked_circle_white_24dp);
                        holder.image.resetAnim();
                        holder.imageForground.setVisibility(View.VISIBLE);
                        uncheckedPhoto(p);
                    } else {
                        if (mPhotoSelectedList.size() < mMaxPhotos) {
                            holder.check.setImageResource(AttrUtils.getResourceId(mContext,R.attr.photoalbum_checked_circle));
                            holder.image.startAnim();
                            holder.imageForground.setVisibility(View.INVISIBLE);
                            checkedPhoto(p);
                        } else {
                            Toast.makeText(mContext,getString(R.string.photoalbum_hint_selected_max,mMaxPhotos),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPhotos == null ? 0 : mPhotos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {

            private ScaleImageView image;
            private View imageForground;
            private ImageView check;

            PhotoViewHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
                check = itemView.findViewById(R.id.check);
                imageForground = itemView.findViewById(R.id.image_forground);
            }
        }
    }

    /**
     * 文件夹适配器
     */
    private class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder>{

        @Override
        public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.photoalbum_item_folder, parent, false);
            return new FolderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final FolderViewHolder holder, int position) {
            PhotosFolder folder = mPhotoData.get(position);

            ImageLoader.loadFileAndCenterCrop(holder.imgPhoto,folder.getFirstImage());
            holder.tvName.setText(folder.getName());
            int number = folder.getImages() == null ? 0 : folder.getImages().size();
            holder.tvCount.setText(getString(R.string.photoalbum_text_photos_number,number));
            holder.radioSelect.setChecked(mSelectedFolderIndex == position);

            // 点击Item
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.radioSelect.setChecked(true);
                }
            });

            // 选中事件
            holder.radioSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        int n = holder.getLayoutPosition();
                        if (n != mSelectedFolderIndex) {
                            folderChanged(n);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPhotoData == null ? 0 : mPhotoData.size();
        }

        class FolderViewHolder extends RecyclerView.ViewHolder {
            private ImageView imgPhoto;
            private TextView tvName;
            private TextView tvCount;
            private RadioButton radioSelect;

            FolderViewHolder(View itemView) {
                super(itemView);
                imgPhoto = itemView.findViewById(R.id.img_photo);
                tvName = itemView.findViewById(R.id.tv_name);
                tvCount = itemView.findViewById(R.id.tv_count);
                radioSelect = itemView.findViewById(R.id.radio_select);
            }
        }
    }
}

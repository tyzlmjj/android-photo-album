package me.majiajie.photoalbum.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.majiajie.photoalbum.IAlbumImageLoader;
import me.majiajie.photoalbum.Album;
import me.majiajie.photoalbum.AlbumActivity;
import me.majiajie.photoalbum.R;
import me.majiajie.photoalbum.data.AlbumFileBean;
import me.majiajie.photoalbum.data.AlbumFolderBean;
import me.majiajie.photoalbum.utils.AttrUtils;

/**
 * 相册图片显示
 */
public class AlbumListFragment extends Fragment {

    private Context mContext;

    private RecyclerView mRecyclerViewPhotos;
    private View mFolderLayoutBackground;
    private RecyclerView mRecyclerViewFolder;
    private Button mBtnSelectFolder;
    private AppCompatCheckBox mCheckboxOriginal;
    private Button mBtnPreview;

    private IAlbumImageLoader mImageLoader;

    /**
     * 底部上下变化的箭头
     */
    private UpDownToggleDrawable mUpDownToggleDrawable;

    /**
     * 所有文件的目录
     */
    private ArrayList<AlbumFolderBean> mData;

    /**
     * 当前显示的目录中的文件数据
     */
    private List<AlbumFileBean> mFiles;

    /**
     * 记录选中的图片
     */
    private ArrayList<AlbumFileBean> mFileSelectedList = new ArrayList<>();

    /**
     * 文件夹动画
     */
    private ValueAnimator mFolderAnim;

    /**
     * 记录选中的目录
     */
    private int mSelectedFolderIndex;

    /**
     * 判断数据
     */
    private AlbumActivity.RequestData mRequestData;

    /**
     * 图片列表事件
     */
    private PhotoListListener mListener;

    /**
     * 图片列表事件监听
     */
    public interface PhotoListListener {

        /**
         * 选中的文件变更
         */
        void onSelectedPhotoChanged(ArrayList<AlbumFileBean> selectedList);

        /**
         * 当单选时，直接返回
         */
        void onSingleSelected(AlbumFileBean file);

        /**
         * 点击预览按钮
         */
        void onClickPreview();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof PhotoListListener) {
            mListener = (PhotoListListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implemented PhotoListListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photoalbum_fragment_photolist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerViewPhotos = view.findViewById(R.id.recyclerView_photos);
        mFolderLayoutBackground = view.findViewById(R.id.folder_layout_background);
        mRecyclerViewFolder = view.findViewById(R.id.recyclerView_folder);
        mBtnSelectFolder = view.findViewById(R.id.btn_select_folder);
        mCheckboxOriginal = view.findViewById(R.id.checkbox_full_image);
        mBtnPreview = view.findViewById(R.id.btn_preview);

        // 分隔线
        mRecyclerViewFolder.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageLoader = Album.LOADER;

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
     * 设置文件数据源
     *
     * @param folderList  分目录数据
     * @param requestData 用于判断的数据
     */
    public void setPhotoList(ArrayList<AlbumFolderBean> folderList, AlbumActivity.RequestData requestData) {
        mData = folderList;
        mRequestData = requestData;

        mSelectedFolderIndex = 0;

        if (mData != null && mData.size() > 0) {
            mFiles = mData.get(mSelectedFolderIndex).getFiles();

            //刷新数据
            mRecyclerViewPhotos.getAdapter().notifyDataSetChanged();
            mRecyclerViewFolder.getAdapter().notifyDataSetChanged();

            //设置按钮显示的文件夹名字
            mBtnSelectFolder.setText(mData.get(mSelectedFolderIndex).getName());
        }

        // 点击直接返回时 隐藏预览按钮
        if (requestData.isSingle()) {
            mBtnPreview.setVisibility(View.GONE);
        }
    }

    /**
     * 设置"原图"按钮的显示隐藏
     *
     * @param show true 显示
     */
    public void showFullImageBtn(boolean show) {
        mCheckboxOriginal.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 是否使用原图
     *
     * @return true 使用原图
     */
    public boolean isUseFullImage() {
        return mCheckboxOriginal.isChecked();
    }

    /**
     * 设置是否使用原图
     */
    public void setUseFullImage(boolean useFullImage) {
        mCheckboxOriginal.setChecked(useFullImage);
    }

    /**
     * 修改选中的图片
     */
    public void modifySelectedPhotos(ArrayList<AlbumFileBean> selectedPhotos) {
        mFileSelectedList = selectedPhotos;
        mRecyclerViewPhotos.getAdapter().notifyDataSetChanged();
        refreshPreviewBtnText();
    }

    /**
     * 是否可回退
     */
    public boolean canBack() {
        return mUpDownToggleDrawable.isChecked();
    }

    /**
     * 回退
     */
    public void goBack() {
        mFolderAnim.reverse();
    }

    /**
     * 底部布局初始化
     */
    private void initBottomLayout() {
        // 设置箭头图标
        mUpDownToggleDrawable =
                new UpDownToggleDrawable(mContext, UpDownToggleDrawable.STATE_UP, ContextCompat.getColor(mContext, AttrUtils.getResourceId(mContext, R.attr.colorControlNormal)));
        mUpDownToggleDrawable.setBounds(0, 0,
                mUpDownToggleDrawable.getIntrinsicWidth(), mUpDownToggleDrawable.getIntrinsicHeight());
        mBtnSelectFolder.setCompoundDrawables(mUpDownToggleDrawable, null, null, null);

        // 点击目录按钮
        mBtnSelectFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData != null) {
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
                if (mListener != null) {
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
        AlbumFolderBean folder = mData.get(mSelectedFolderIndex);
        // 设置按钮显示的文件夹名字
        mBtnSelectFolder.setText(folder.getName());
        // 记录文件夹图片
        mFiles = folder.getFiles();
        // 刷新图片适配器
        mRecyclerViewPhotos.getAdapter().notifyDataSetChanged();
        mFolderAnim.reverse();
    }

    /**
     * 选中图片
     */
    private void checkedPhoto(AlbumFileBean photo) {
        mFileSelectedList.add(photo);
        if (mListener != null) {
            mListener.onSelectedPhotoChanged(mFileSelectedList);
        }
        refreshPreviewBtnText();
    }

    /**
     * 取消选中图片
     */
    private void uncheckedPhoto(AlbumFileBean photo) {
        mFileSelectedList.remove(photo);
        if (mListener != null) {
            mListener.onSelectedPhotoChanged(mFileSelectedList);
        }
        refreshPreviewBtnText();
    }

    /**
     * 刷新预览按钮的文字
     */
    private void refreshPreviewBtnText() {
        int size = mFileSelectedList.size();
        if (size == 0) {
            mBtnPreview.setText(R.string.photoalbum_text_preview);
            mBtnPreview.setEnabled(false);
        } else {
            mBtnPreview.setText(String.format(Locale.getDefault(), "%s(%d)", getString(R.string.photoalbum_text_preview), size));
            mBtnPreview.setEnabled(true);
        }
    }

    /**
     * 图片列表适配器
     */
    private class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int TYPE_VIDEO = 0;
        private final int TYPE_PHOTO = 1;

        @Override
        public int getItemViewType(int position) {
            return mFiles.get(position).isVideo() ? TYPE_VIDEO : TYPE_PHOTO;
        }

        @Override
        public int getItemCount() {
            return mFiles == null ? 0 : mFiles.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return viewType == TYPE_VIDEO ? VideoViewHolder.newInstance(parent) : PhotoViewHolder.newInstance(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            AlbumFileBean file = mFiles.get(position);

            if (holder instanceof PhotoViewHolder) {
                bindPhoto((PhotoViewHolder) holder, file);
            } else if (holder instanceof VideoViewHolder) {
                bindVideo((VideoViewHolder) holder, file);
            }
        }

        /**
         * 绑定图片数据
         */
        private void bindPhoto(final PhotoViewHolder holder, AlbumFileBean file) {

            if (mRequestData.isSinglePhoto()) {
                holder.check.setVisibility(View.GONE);
            } else {
                holder.check.setVisibility(View.VISIBLE);
                // 选中状态
                holder.setCheck(mContext, mFileSelectedList.contains(file), false);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mImageLoader.loadLocalImageOrVideo(holder.image, file.getUri());
            } else {
                mImageLoader.loadLocalImageOrVideo(holder.image, file.getPath());
            }

            // 点击选中
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlbumFileBean p = mFiles.get(holder.getLayoutPosition());
                    if (mFileSelectedList.contains(p)) {// 已经选中
                        holder.setCheck(mContext, false, true);
                        uncheckedPhoto(p);
                    } else if (mRequestData.isSinglePhoto()) {// 当图片是单选时
                        if (mFileSelectedList.isEmpty()) {
                            mListener.onSingleSelected(p);
                        } else {
                            Toast.makeText(mContext, getString(R.string.photoalbum_hint_unable_select_image_video_same_time), Toast.LENGTH_SHORT).show();
                        }
                    } else {// 多选图片时
                        int maxNumber = mRequestData.getMaxNumber() > 0 ? mRequestData.getMaxNumber() : mRequestData.getMaxPhotoNumber();
                        if (mFileSelectedList.size() < maxNumber) {
                            holder.setCheck(mContext, true, true);
                            checkedPhoto(p);
                        } else {
                            Toast.makeText(mContext, getString(R.string.photoalbum_hint_selected_max_photo, maxNumber), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        /**
         * 绑定视频数据
         */
        private void bindVideo(final VideoViewHolder holder, AlbumFileBean file) {
            // 视频时长
            int time = file.getVoideTime(holder.itemView.getContext());
            holder.tvVideoTime.setText(String.format(Locale.CHINA, "%02d:%02d", time / 60, time % 60));

            if (mRequestData.isSingleVideo()) {
                holder.check.setVisibility(View.GONE);
            } else {
                holder.check.setVisibility(View.VISIBLE);
                // 选中状态
                holder.setCheck(mContext, mFileSelectedList.contains(file), false);
            }
            // 图片
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mImageLoader.loadLocalImageOrVideo(holder.image, file.getUri());
            } else {
                mImageLoader.loadLocalImageOrVideo(holder.image, file.getPath());
            }
            // 点击选中
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlbumFileBean p = mFiles.get(holder.getLayoutPosition());
                    if (mFileSelectedList.contains(p)) {// 已经选中
                        holder.setCheck(mContext, false, true);
                        uncheckedPhoto(p);
                    } else if (mRequestData.isSingleVideo()) {// 当图片是单选时
                        if (mFileSelectedList.isEmpty()) {
                            mListener.onSingleSelected(p);
                        } else {
                            Toast.makeText(mContext, getString(R.string.photoalbum_hint_unable_select_image_video_same_time), Toast.LENGTH_SHORT).show();
                        }
                    } else {// 多选图片时
                        int maxNumber = mRequestData.getMaxNumber() > 0 ? mRequestData.getMaxNumber() : mRequestData.getMaxVideoNumber();
                        if (mFileSelectedList.size() < maxNumber) {
                            holder.setCheck(mContext, true, true);
                            checkedPhoto(p);
                        } else {
                            Toast.makeText(mContext, getString(R.string.photoalbum_hint_selected_max_video, maxNumber), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        static PhotoViewHolder newInstance(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photoalbum_item_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        private ScaleImageView image;
        private View imageForground;
        private ImageView check;

        private PhotoViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageForground = itemView.findViewById(R.id.image_forground);
            check = itemView.findViewById(R.id.check);
        }

        private void setCheck(Context context, boolean checked, boolean anim) {
            if (checked) {
                check.setImageResource(AttrUtils.getResourceId(context, R.attr.photoalbum_checked_circle));
                if (anim) {
                    image.startAnim();
                } else {
                    image.setChecked(true);
                }
                imageForground.setVisibility(View.INVISIBLE);
            } else {
                check.setImageResource(R.drawable.photoalbum_unchecked_circle_white_24dp);
                if (anim) {
                    image.resetAnim();
                } else {
                    image.setChecked(false);
                }
                imageForground.setVisibility(View.VISIBLE);
            }
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {

        static VideoViewHolder newInstance(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photoalbum_item_video, parent, false);
            return new VideoViewHolder(view);
        }

        private ScaleFrameLayout layoutImg;
        private ImageView image;
        private TextView tvVideoTime;
        private View imageForground;
        private ImageView check;

        private VideoViewHolder(View itemView) {
            super(itemView);
            layoutImg = itemView.findViewById(R.id.layout_img);
            image = itemView.findViewById(R.id.image);
            tvVideoTime = itemView.findViewById(R.id.tv_video_time);
            imageForground = itemView.findViewById(R.id.image_forground);
            check = itemView.findViewById(R.id.check);
        }

        private void setCheck(Context context, boolean checked, boolean anim) {
            if (checked) {
                check.setImageResource(AttrUtils.getResourceId(context, R.attr.photoalbum_checked_circle));
                if (anim) {
                    layoutImg.startAnim();
                } else {
                    layoutImg.setChecked(true);
                }
                imageForground.setVisibility(View.INVISIBLE);
            } else {
                check.setImageResource(R.drawable.photoalbum_unchecked_circle_white_24dp);
                if (anim) {
                    layoutImg.resetAnim();
                } else {
                    layoutImg.setChecked(false);
                }
                imageForground.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * 文件夹适配器
     */
    private class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

        @NonNull
        @Override
        public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.photoalbum_item_folder, parent, false);
            return new FolderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final FolderViewHolder holder, int position) {
            AlbumFolderBean folder = mData.get(position);

            holder.tvName.setText(folder.getName());
            int number = folder.getFiles() == null ? 0 : folder.getFiles().size();
            holder.tvCount.setText(getString(R.string.photoalbum_text_photos_number, number));
            holder.radioSelect.setChecked(mSelectedFolderIndex == position);

            if (number > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mImageLoader.loadLocalImageOrVideo(holder.imgPhoto, folder.getFiles().get(0).getUri());
                } else {
                    mImageLoader.loadLocalImageOrVideo(holder.imgPhoto, folder.getFiles().get(0).getPath());
                }
            }

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
            return mData == null ? 0 : mData.size();
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

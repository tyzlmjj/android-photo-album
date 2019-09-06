package me.majiajie.photoalbum;

import androidx.fragment.app.Fragment;

/**
 * 用于在图像选择完成时在图像选择页面对图片进行进一步操作，而不是返回数据到前一个Activity
 */
public abstract class BaseCompleteFragment extends Fragment {

    /**
     * 返回选择的图片结果
     */
    protected abstract void onResultData(AlbumActivity.ResultData resultData);
}

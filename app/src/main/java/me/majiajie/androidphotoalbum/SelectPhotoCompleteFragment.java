package me.majiajie.androidphotoalbum;

import android.content.Context;
import android.widget.Toast;

import java.util.Locale;

import me.majiajie.photoalbum.BaseCompleteFragment;
import me.majiajie.photoalbum.PhotoAlbumActivity;

/**
 * 无UI的Fragment,用于对选择的图片进行处理
 */
public class SelectPhotoCompleteFragment extends BaseCompleteFragment {

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    protected void onResultData(PhotoAlbumActivity.ResultData resultData) {
        Toast.makeText(mContext,String.format(Locale.CHINA,"选择了%d张图片\n结果可以在无UI的Fragment中处理",resultData.getPhotos().size()),Toast.LENGTH_LONG).show();
    }


}

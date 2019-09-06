package me.majiajie.photoalbum.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import me.majiajie.photoalbum.R;

/**
 * 获取权限
 */
public class RequestPermissionFragment extends Fragment {

    private Context mContext;

    private static final String ARG_PERMISSIONS = "PERMISSIONS";
    private static final String ARG_HINT_TEXT = "ARG_HINT_TEXT";

    private static final int REQUEST_PERMISSIONS_CODE = 6666;

    /**
     * 权限数组
     */
    private String[] mPermissions;

    /**
     * 提示语
     */
    private String mHint;

    private RequestPermissionsCallback mRequestPermissionsCallback;

    /**
     * 请求权限的回调
     */
    public interface RequestPermissionsCallback {

        /**
         * 权限申请的结果
         *
         * @param grantResult true表示获得了权限
         */
        void onRequestPermissionsResult(boolean grantResult);

    }

    public static RequestPermissionFragment newInstance(@NonNull String[] permissions,@NonNull String hint) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PERMISSIONS, permissions);
        args.putString(ARG_HINT_TEXT, hint);
        RequestPermissionFragment fragment = new RequestPermissionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public RequestPermissionFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof RequestPermissionsCallback) {
            mRequestPermissionsCallback = (RequestPermissionsCallback) context;
        } else {
            throw new ClassCastException(context.toString() + " must implements RequestPermissionsCallback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mPermissions = args.getStringArray(ARG_PERMISSIONS);
            mHint = args.getString(ARG_HINT_TEXT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE && mRequestPermissionsCallback != null) {
            boolean result = true;
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    result = false;
                    break;
                }
            }
            mRequestPermissionsCallback.onRequestPermissionsResult(result);
        }
    }

    /**
     * 检查权限是否获取
     */
    public boolean checkPermissions() {
        if (mPermissions != null) {
            for (String permissionName : mPermissions) {
                if (ActivityCompat.checkSelfPermission(mContext, permissionName) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 请求权限
     */
    public void requestPermissions() {
        boolean showRationale = false;
        for (String permission:mPermissions){
            if (shouldShowRequestPermissionRationale(permission)){
                showRationale = true;
                break;
            }
        }
        if (!showRationale) {
            requestPermissions(mPermissions, REQUEST_PERMISSIONS_CODE);
            return;
        }
        showPermissionsHintDialog();
    }

    /**
     * 显示权限提醒的提示框
     */
    private void showPermissionsHintDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.photoalbum_text_notice)
                .setMessage(mHint)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(mPermissions, REQUEST_PERMISSIONS_CODE);
                    }
                })
                .show();
    }
}

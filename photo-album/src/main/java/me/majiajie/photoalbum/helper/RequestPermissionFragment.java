package me.majiajie.photoalbum.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import me.majiajie.photoalbum.R;

/**
 * 获取权限
 */
public class RequestPermissionFragment extends Fragment {

    private Context mContext;

    private static final String ARG_PERMISSIONS = "PERMISSIONS";
    private static final String ARG_HINT_TEXT = "ARG_HINT_TEXT";

    private static final int REQUEST_PERMISSIONS_CODE = 6666;

    private String[] mPermissions;

    private String mHint;

    private RequestPermissionsCallback mRequestPermissionsCallback;

    public interface RequestPermissionsCallback {

        /**
         * 权限申请的结果
         *
         * @param grantResult true表示获得了权限
         */
        void onRequestPermissionsResult(boolean grantResult);

    }

    public static RequestPermissionFragment newInstance(String[] permissions, @Nullable String hint) {
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
        if (TextUtils.isEmpty(mHint)) {
            requestPermissions(mPermissions, REQUEST_PERMISSIONS_CODE);
        } else {
            showPermissionsHintDialog();
        }
    }

    /**
     * 显示权限提醒的提示框
     */
    private void showPermissionsHintDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.photoalbum_text_notice)
                .setMessage(mHint)
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(mPermissions, REQUEST_PERMISSIONS_CODE);
                    }
                })
                .show();
    }
}

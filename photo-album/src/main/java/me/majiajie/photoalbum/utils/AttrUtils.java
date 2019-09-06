package me.majiajie.photoalbum.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.TypedArrayUtils;

/**
 * 获取自定义属性值<p>
 * 基础值都可以通过{@link TypedValue}的coerceToString()方法输出<p>
 * 自定义属性集合（declare-styleable）的操作可以使用{@link TypedArrayUtils}
 */
public class AttrUtils {

    /**
     * 获取单个自定义属性的TypedValue
     * @param context 上下文
     * @param attrRes attrid
     * @return {@link TypedValue}
     */
    private static TypedValue getTypedValue(@NonNull Context context, @AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue;
    }

    /**
     * 获取资源ID
     * @param context 上下文
     * @param attrRes attrid
     * @return ResourceId
     */
    public static int getResourceId(@NonNull Context context, @AttrRes int attrRes) {
        return getTypedValue(context, attrRes).resourceId;
    }

}

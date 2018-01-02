package me.majiajie.photoalbum.utils;

/**
 *  视图越界拉动阻尼计算
 */
public class DampingUtils {

    /**
     * 控制超越视图下拉限制的系数
     */
    private static final float OVERFLOW = 1.2f;

    /**
     * 通过手指下拉的距离计算视图下移的距离
     * @param touchLength   手指拉动长度
     * @param n             阻尼系数，值越大阻尼越大，1为平衡点
     * @param maxMove       视图下移的最大距离限制
     * @return  视图下移距离
     */
    public static float getViewMove(float touchLength,float n,float maxMove){
        if (touchLength <= 0){
            return 0f;
        } else if (touchLength < maxMove * n * OVERFLOW){
            return (float) (getMaxOverflow(maxMove) * Math.sin(Math.toRadians(touchLength/(maxMove*n*OVERFLOW)*90)));
        } else {
            return (float) getMaxOverflow(maxMove);
        }
    }

    /**
     * 获取最大越界距离
     * @param maxMove 视图下移的最大距离限制
     */
    private static double getMaxOverflow(float maxMove){
        return maxMove / Math.sin(Math.toRadians(1.0f/OVERFLOW*90));
    }
}

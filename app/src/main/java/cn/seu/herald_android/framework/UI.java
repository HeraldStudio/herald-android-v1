package cn.seu.herald_android.framework;

import android.content.Context;

import java.lang.reflect.Field;

public class UI {
    public static int dp2px(int dp) {
        float density = AppContext.instance.getResources().getDisplayMetrics().density;
        return ((int) density * dp);
    }

    public static float dp2px(float dp) {
        float density = AppContext.instance.getResources().getDisplayMetrics().density;
        return density * dp;
    }

    // 获取手机状态栏高度
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
}

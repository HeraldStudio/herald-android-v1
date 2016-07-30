package cn.seu.herald_android.framework;

public class UI {
    public static int dp2px(int dp) {
        float density = AppContext.instance.getResources().getDisplayMetrics().density;
        return ((int) density * dp);
    }

    public static float dp2px(float dp) {
        float density = AppContext.instance.getResources().getDisplayMetrics().density;
        return density * dp;
    }
}

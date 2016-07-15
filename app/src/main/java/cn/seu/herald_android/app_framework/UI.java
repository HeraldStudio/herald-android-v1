package cn.seu.herald_android.app_framework;

public class UI {
    public static int dp2px(int dp) {
        float density = AppContext.currentContext.$get().getResources().getDisplayMetrics().density;
        return ((int) density * dp);
    }
}

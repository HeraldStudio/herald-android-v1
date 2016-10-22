package cn.seu.herald_android.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.widget.Toast;

import java.util.Stack;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_secondary.LoginActivity;

/**
 * 静态单例的 Context 封装类, 用于保存当前界面的 Context
 * 应用程序初始化时为 Application 的总 Context, 待应用界面出现和切换时则不断被当前 BaseActivity 抢占
 * <p>
 * 注意此类需要在 Manifest 中绑定到应用程序 (<application
 * android:name="cn.seu.herald_android.app_framework.AppContext") 才能正常工作
 * <p>
 * 调用方法: 在各工具函数中用 AppContext.instance 代替 Context 即可帮助工具函数静态化
 */
public class AppContext extends Application implements Application.ActivityLifecycleCallbacks {

    public static Application instance = null;

    public static Stack<Activity> foregroundActivities = new Stack<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(this);
        super.onTerminate();
    }

    public static Activity getForegroundActivity() {
        if (foregroundActivities.size() > 0) {
            return foregroundActivities.peek();
        }
        return null;
    }

    public static Context getCurrentContext() {
        if (foregroundActivities.size() > 0) {
            return foregroundActivities.peek();
        }
        return instance;
    }

    public static void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instance.startActivity(intent);
    }

    public static <T extends Activity> void startActivitySafely(Class<T> activityClass) {
        Intent intent = new Intent(instance, activityClass);
        startActivitySafely(intent);
    }

    public static void showLogin() {
        startActivitySafely(LoginActivity.class);
    }

    public static void showToast(String message) {
        Toast.makeText(instance, message, Toast.LENGTH_SHORT).show();
    }

    public static void showMessage(String message) {
        Context context = getCurrentContext();
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).showSnackBar(message);
        } else {
            showToast(message);
        }
    }

    public static void showMessage(String message, String actionText, Runnable action) {
        Context context = getCurrentContext();
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).showSnackBar(message, actionText, action);
        } else {
            Toast.makeText(instance, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void openUrlInBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppContext.instance.startActivity(intent);
    }

    /**
     * 获取该场景下主题的主色调，返回对应的颜色值（不是资源id）
     **/

    @ColorInt
    public static int getColorPrimary(Context context) {
        try {
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            return ContextCompat.getColor(context, typedValue.resourceId);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return ContextCompat.getColor(context, R.color.colorPrimary);
        }
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        foregroundActivities.push(activity);
    }

    public void onActivityPaused(Activity activity) {
        if (foregroundActivities.peek() == activity) {
            foregroundActivities.pop();
        }
    }

    public void onActivityStopped(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
}
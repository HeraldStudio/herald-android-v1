package cn.seu.herald_android.app_framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.Toast;

import java.util.Vector;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.mod_auth.LoginActivity;

/**
 * 静态单例的 Context 封装类, 用于保存当前界面的 Context
 * 应用程序初始化时为 Application 的总 Context, 待应用界面出现和切换时则不断被当前 BaseActivity 抢占
 *
 * 注意此类需要在 Manifest 中绑定到应用程序 (<application
 *     android:name="cn.seu.herald_android.app_framework.AppContext") 才能正常工作
 *
 * 调用方法: 在各工具函数中用 AppContext.instance 代替 Context 即可帮助工具函数静态化
 */
public class AppContext extends Application {

    public static Application applicationContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
    }

    public static $<Context> currentContext = new $<Context>() {
        @Override
        public Context get() {
            while (activities.size() != 0 &&
                    (activities.lastElement() == null || activities.lastElement().isFinishing())){
                activities.remove(activities.size() - 1);
            }
            if (activities.size() == 0) {
                return applicationContext;
            } else {
                return activities.lastElement();
            }
        }
    };

    public static Vector<BaseActivity> activities = new Vector<>();

    public static void closeAllActivities() {
        for (Activity activity : activities) {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    public static void showLogin() {
        Context baseContext = currentContext.get().getApplicationContext();
        closeAllActivities();

        Intent intent = new Intent(baseContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        baseContext.startActivity(intent);
    }

    public static void showMain() {
        Context baseContext = currentContext.get().getApplicationContext();
        closeAllActivities();

        Intent intent = new Intent(baseContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        baseContext.startActivity(intent);
    }

    public static void showMessage(String message) {
        if (currentContext.get() instanceof BaseActivity) {
            ((BaseActivity) currentContext.get()).showSnackBar(message);
        } else {
            Toast.makeText(currentContext.get(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showMessage(String message, String actionText, Runnable action) {
        if (currentContext.get() instanceof BaseActivity) {
            ((BaseActivity) currentContext.get()).showSnackBar(message, actionText, action);
        } else {
            Toast.makeText(currentContext.get(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取该场景下主题的主色调，返回对应的颜色资源id
     **/
    public static int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        currentContext.get().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.resourceId;
    }
}
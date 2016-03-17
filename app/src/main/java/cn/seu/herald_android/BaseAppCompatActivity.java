package cn.seu.herald_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.custom.CustomSnackBar;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;

public class BaseAppCompatActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private ApiHelper apiHelper;
    private CacheHelper cacheHelper;
    private SettingsHelper settingsHelper;
    private ServiceHelper serviceHelper;

    /**
     * 实现将任何与尺寸有关的任务延迟到启动完毕后进行。
     * 这些任务可以通过调用runMeasurementDependentTask(Runnable)来执行，
     * 该方法将自动判断当前是否可以获取尺寸，如果不可以，自动将该任务推迟到可以
     * 获取尺寸时再执行；如果当前可以获取尺寸，则将立即执行该任务。
     */
    private List<Runnable> onLoadTasks = new ArrayList<>();
    private boolean firstCreate = true;

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);
        return statusView;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //此处apihelper用到activity跳转，所以需要传入activity为参数
        this.apiHelper = new ApiHelper(this);
        this.cacheHelper = new CacheHelper(getBaseContext());
        this.settingsHelper = new SettingsHelper(getBaseContext());
        this.serviceHelper = new ServiceHelper(this);

        NetworkLoginHelper.getInstance(this).registerReceiver();

        //加载刷新对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("加载中，请稍候…");
    }

    @Override
    protected void onDestroy() {
        NetworkLoginHelper.getInstance(this).unregisterReceiver();
        super.onDestroy();
    }

    protected ApiHelper getApiHelper() {
        return apiHelper;
    }

    public CacheHelper getCacheHelper() {
        return cacheHelper;
    }


    protected ServiceHelper getServiceHelper() {
        return serviceHelper;
    }

    protected SettingsHelper getSettingsHelper() {
        return settingsHelper;
    }

    public void showMsg(String msg) {
        showSnackBar(msg);
    }

    protected void showProgressDialog() {
        progressDialog.show();
    }

    protected void hideProgressDialog() {
        progressDialog.dismiss();
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     */
    protected void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                // 生成一个状态栏大小的矩形
                View statusView = createStatusView(activity, color);
                // 添加 statusView 到布局中
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                decorView.addView(statusView);
            }
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    void runMeasurementDependentTask(Runnable task) {
        if (firstCreate) {
            onLoadTasks.add(task);
        } else {
            task.run();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (firstCreate) {
            for (; onLoadTasks.size() > 0; ) {
                onLoadTasks.get(0).run();
                onLoadTasks.remove(0);
            }
        }
        firstCreate = false;
    }

    // 显示一个SnackBar
    protected void showSnackBar(String message) {
        // 首先关闭软键盘，防止被软键盘遮挡
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // 获取该主题下的主色调
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);

        // 获取根视图
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        // 使用改色调构建SnackBar
        new CustomSnackBar().view(rootView)
                .backgroundColor(ContextCompat.getColor(this, typedValue.resourceId))
                .text(message, null)
                .textColors(Color.WHITE, ContextCompat.getColor(this, R.color.colorAccent))
                .duration(CustomSnackBar.SnackBarDuration.LONG).show();
    }
}
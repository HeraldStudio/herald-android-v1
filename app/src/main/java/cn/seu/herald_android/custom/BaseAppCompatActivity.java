package cn.seu.herald_android.custom;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_webmodule.WebShowActivity;

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

    /**
     * 注意！！！！！！
     * 继承自此类的Activity务必运行一次enableSwipeBack，否则根布局将全透明
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

        //加载刷新对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("加载中，请稍候…");
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

    public void showProgressDialog() {
        progressDialog.show();
    }

    public void hideProgressDialog() {
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

    protected void enableSwipeBack() {
        // 设置根布局的参数
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        rootView.setBackgroundColor(Color.WHITE);
        Slidr.attach(this, new SlidrConfig.Builder().edge(true).edgeSize(.05f).build());
    }

    public void runMeasurementDependentTask(Runnable task) {
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
    public void showSnackBar(String message) {
        // 首先关闭软键盘，防止被软键盘遮挡
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // 获取根视图
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        // 使用改色调构建SnackBar
        new CustomSnackBar().view(rootView)
                .backgroundColor(ContextCompat.getColor(this, ContextUtils.getColorPrimary(this)))
                .text(message, null)
                .textColors(Color.WHITE, ContextCompat.getColor(this, R.color.colorAccent))
                .duration(CustomSnackBar.SnackBarDuration.LONG).show();
    }

    // 显示一个带按钮的SnackBar
    public void showSnackBar(String message, String actionTitle, Runnable action) {
        // 首先关闭软键盘，防止被软键盘遮挡
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // 获取根视图
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        // 使用改色调构建SnackBar
        new CustomSnackBar().view(rootView)
                .backgroundColor(ContextCompat.getColor(this, ContextUtils.getColorPrimary(this)))
                .text(message, actionTitle)
                .textColors(Color.WHITE, Color.WHITE)
                .setOnClickListener(true, view -> action.run())
                .duration(CustomSnackBar.SnackBarDuration.LONG).show();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        //切换动画
        overridePendingTransition(R.anim.activity_right_in, R.anim.activity_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        //切换动画
        overridePendingTransition(R.anim.activity_left_in, R.anim.activity_out_right);
    }



}
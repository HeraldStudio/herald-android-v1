package cn.seu.herald_android.app_framework;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.custom.CustomSnackBar;
import cn.seu.herald_android.mod_auth.LoginActivity;

/**
 * 应用程序中所有 Activity 必须继承此类, 以保证能够正常使用静态化的工具函数和工具类.
 **/
public class BaseActivity extends AppCompatActivity {

    /***************************************
     * 配合 AppContext 类, 实现 Context 静态化
     ***************************************/

    /** 当界面被创建时, 向 AppContext 类注册当前 Context, 以便正常显示提示消息等 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContext.activities.add(this);

        // 加载刷新对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("加载中，请稍候…");
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        // 设置背景色为白色
        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
        if (contentView != null) {
            ViewGroup rootView = (ViewGroup) contentView.getChildAt(0);

            rootView.setBackgroundColor(Color.WHITE);
        }

        // Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
                finish();
            });
        }

        // 滑动返回和状态栏沉浸
        if (!(this instanceof MainActivity) && !(this instanceof LoginActivity)) {
            setStatusBarColor(ContextCompat.getColor(this, AppContext.getColorPrimary()));
            Slidr.attach(this, new SlidrConfig.Builder().edge(true).edgeSize(.05f).build());
        } else if (this instanceof MainActivity) {
            setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // 切换动画
        overridePendingTransition(R.anim.activity_right_in, R.anim.activity_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        // 切换动画
        overridePendingTransition(R.anim.activity_left_in, R.anim.activity_out_right);
    }

    /**************************************************************
     * 实现将任何与尺寸有关的任务延迟到启动完毕后进行。
     * 这些任务可以通过调用 runMeasurementDependentTask(Runnable) 来执行,
     * 该方法将自动判断当前是否可以获取尺寸, 如果不可以, 自动将该任务推迟到可以
     * 获取尺寸时再执行; 如果当前可以获取尺寸, 则将立即执行该任务.
     **************************************************************/

    /** 加载完成后需要运行的任务 */
    private List<Runnable> onLoadTasks = new ArrayList<>();

    /** 处于刚启动, 未加载完成的状态标记 */
    private boolean firstCreate = true;

    /** 运行任务的函数 */
    public void runMeasurementDependentTask(Runnable task) {
        if (firstCreate) {
            onLoadTasks.add(task);
        } else {
            task.run();
        }
    }

    /** 绑定界面加载完成的事件 */
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

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    protected static View createStatusView(Activity activity, int color) {
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

    protected ProgressDialog progressDialog;

    public void showProgressDialog() {
        progressDialog.show();
    }

    public void hideProgressDialog() {
        progressDialog.dismiss();
    }

    static View statusView;

    static Window window;

    /**
     * 设置状态栏颜色
     *
     * @param color    状态栏颜色值
     */
    protected void setStatusBarColor(int color) {

        /// 若已经设置过 4.4 ~< 6.0 的沉浸, 直接改变颜色
        if (statusView!=null) {
            statusView.setBackgroundColor(color);
        }

        /// 若已经设置过 6.0 ~ 的沉浸, 直接改变颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (window != null) {
                window.setStatusBarColor(color);
            }
        }

        /// 判断版本在 4.4 以上, 低于 4.4 的不设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            // 设置状态栏透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // 判断版本低于 5.0, 否则用新方法
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                // 生成一个状态栏大小的矩形
                View statusView = createStatusView(this, color);

                // 添加 statusView 到布局中
                ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
                decorView.addView(statusView);
            } else {
                // 5.0 以上通过window来设置颜色
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }

            // 设置根布局的参数
            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
            if (contentView != null) {
                ViewGroup rootView = (ViewGroup) contentView.getChildAt(0);

                // 5.0以上有伸缩布局和伸缩视差的地方必须在xml文件里设置根布局 FitsSystemWindows 为 false，
                // 其他的地方依然为 true
                rootView.setFitsSystemWindows(true);
                rootView.setClipToPadding(true);
            }
        }
    }

    // 显示一个SnackBar
    public void showSnackBar(String message) {

        // 首先关闭软键盘，防止被软键盘遮挡
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
        if (contentView != null) {
            ViewGroup rootView = (ViewGroup) contentView.getChildAt(0);

            // 使用改色调构建SnackBar
            new CustomSnackBar().view(rootView)
                    .backgroundColor(ContextCompat.getColor(this, AppContext.getColorPrimary()))
                    .text(message, null)
                    .textColors(Color.WHITE, ContextCompat.getColor(this, R.color.colorAccent))
                    .duration(CustomSnackBar.SnackBarDuration.LONG).show();
        }

    }

    // 显示一个带按钮的SnackBar
    public void showSnackBar(String message, String actionTitle, Runnable action) {

        // 首先关闭软键盘，防止被软键盘遮挡
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
        if (contentView != null) {
            ViewGroup rootView = (ViewGroup) contentView.getChildAt(0);

            // 使用改色调构建SnackBar
            new CustomSnackBar().view(rootView)
                    .backgroundColor(ContextCompat.getColor(this, AppContext.getColorPrimary()))
                    .text(message, actionTitle)
                    .textColors(Color.WHITE, Color.WHITE)
                    .setOnClickListener(true, view -> action.run())
                    .duration(CustomSnackBar.SnackBarDuration.LONG).show();
        }
    }
}
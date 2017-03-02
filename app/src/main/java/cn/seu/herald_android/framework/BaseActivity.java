package cn.seu.herald_android.framework;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CustomAppBarLayout;
import cn.seu.herald_android.custom.CustomSnackBar;

/**
 * 应用程序中所有 Activity 必须继承此类。
 **/
public class BaseActivity extends AppCompatActivity {

    public interface NoSwipeBack {
        /** 空接口，用于标示某一 Activity 不允许滑动返回 */
    }

    LinearLayout mRootView;
    CustomAppBarLayout mToolbarContainer;
    Toolbar mToolbar;
    FrameLayout mContent;
    View mCustomToolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.custom__activity_framework);

        mRootView = (LinearLayout) findViewById(R.id.base_activity_linear);
        mToolbarContainer = (CustomAppBarLayout) findViewById(R.id.base_activity_toolbar_container);
        mToolbar = (Toolbar) findViewById(R.id.base_activity_toolbar);
        mContent = (FrameLayout) findViewById(R.id.base_activity_content);

        // 加载刷新对话框
        progressDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        // 初始化标题栏
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        mToolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        // 初始化滑动返回
        if (!(this instanceof NoSwipeBack)) {
            Slidr.attach(this, new SlidrConfig.Builder().edge(true).edgeSize(.05f).build());
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mContent.removeAllViews();
        getLayoutInflater().inflate(layoutResID, mContent);
    }

    public void setCustomToolbar(@LayoutRes int layoutResID) {
        showToolbar();
        setDefaultToolbar();
        mToolbar.setVisibility(View.GONE);
        mCustomToolbar = getLayoutInflater().inflate(layoutResID, mToolbarContainer);
        if (mRootView.getBackground() instanceof ColorDrawable) {
            mCustomToolbar.setBackgroundColor(((ColorDrawable) mRootView.getBackground()).getColor());
        }
    }

    public void setDefaultToolbar() {
        showToolbar();
        mToolbarContainer.removeView(mCustomToolbar);
        mCustomToolbar = null;
        mToolbar.setVisibility(View.VISIBLE);
    }

    public void hideToolbar() {
        mToolbarContainer.setVisibility(View.GONE);
    }

    public void showToolbar() {
        mToolbarContainer.setVisibility(View.VISIBLE);
    }

    public void changeToolbarColor(@ColorInt int color) {
        mToolbar.setBackgroundColor(color);
        if (mCustomToolbar != null) {
            mCustomToolbar.setBackgroundColor(color);
        }
        mRootView.setBackgroundColor(color);
    }

    /**************************************************************
     * 实现将任何与尺寸有关的任务延迟到启动完毕后进行。
     * 这些任务可以通过调用 runMeasurementDependentTask(Runnable) 来执行,
     * 该方法将自动判断当前是否可以获取尺寸, 如果不可以, 自动将该任务推迟到可以
     * 获取尺寸时再执行; 如果当前可以获取尺寸, 则将立即执行该任务.
     **************************************************************/

    /**
     * 加载完成后需要运行的任务
     */
    private List<Runnable> onLoadTasks = new ArrayList<>();

    /**
     * 处于刚启动, 未加载完成的状态标记
     */
    private boolean firstCreate = true;

    /**
     * 运行任务的函数
     */
    public void runMeasurementDependentTask(Runnable task) {
        if (firstCreate) {
            onLoadTasks.add(task);
        } else {
            task.run();
        }
    }

    /**
     * 绑定界面加载完成的事件
     */
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

    protected KProgressHUD progressDialog;

    public void showProgressDialog() {
        progressDialog.show();
    }

    public void hideProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // Do nothing
        }
    }

    private Window window;

    // 显示一个SnackBar
    public void showSnackBar(String message) {
        Toast.makeText(this, message, message.length() <= 20 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();

//        // 首先关闭软键盘，防止被软键盘遮挡
//        View v = getCurrentFocus();
//        if (v != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//
//        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
//        if (contentView != null) {
//            ViewGroup rootView = (ViewGroup) contentView.getChildAt(0);
//
//            // 使用改色调构建SnackBar
//            new CustomSnackBar().view(rootView)
//                    .backgroundColor(Color.WHITE)
//                    .text(message, null)
//                    .textColors(ContextCompat.getColor(this, R.color.colorAccent),
//                            ContextCompat.getColor(this, R.color.colorAccent))
//                    .duration(CustomSnackBar.SnackBarDuration.LONG).show();
//        }
    }

    // 显示一个带按钮的SnackBar
//    public void showSnackBar(String message, String actionTitle, Runnable action) {
//
//        // 首先关闭软键盘，防止被软键盘遮挡
//        View v = getCurrentFocus();
//        if (v != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//
//        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
//        if (contentView != null) {
//            ViewGroup rootView = (ViewGroup) contentView.getChildAt(0);
//
//            // 使用改色调构建SnackBar
//            new CustomSnackBar().view(rootView)
//                    .backgroundColor(Color.WHITE)
//                    .text(message, actionTitle)
//                    .textColors(ContextCompat.getColor(this, R.color.colorAccent),
//                            ContextCompat.getColor(this, R.color.colorAccent))
//                    .setOnClickListener(true, view -> action.run())
//                    .duration(CustomSnackBar.SnackBarDuration.LONG).show();
//        }
//    }
}
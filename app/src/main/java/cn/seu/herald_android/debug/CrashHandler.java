package cn.seu.herald_android.debug;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.lang.Thread.UncaughtExceptionHandler;

public class CrashHandler implements UncaughtExceptionHandler {
    private Context mContext;
    private static CrashHandler mCrashHandler = new CrashHandler();

    public static CrashHandler getInstance() {
        return mCrashHandler;
    }

    /**
     * 设置当线程由于未捕获到异常而突然终止的默认处理程序。
     */
    public void setCrashHandler(Context context) {
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当发生Crash时调用该方法
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        //2 提示Crash信息
        showCrashTipToast(Utils.saveCrashInfoToSDCard(mContext, throwable));
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
        //3 退出应用
        System.exit(0);
    }

    private void showCrashTipToast(String info) {
        new Thread(() -> {
            Looper.prepare();
            ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(info);
            Toast.makeText(mContext, "错误信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }).start();
    }

}
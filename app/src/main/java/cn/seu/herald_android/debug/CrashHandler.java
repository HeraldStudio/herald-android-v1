package cn.seu.herald_android.debug;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
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
        boolean caught = showCrashTipToast(Utils.saveCrashInfoToSDCard(mContext, throwable));
        if (caught) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HeraldApp", "Stack Trace", throwable);
        }
        //3 退出应用
        System.exit(0);
    }

    private boolean showCrashTipToast(String info) {
        ClipboardManager manager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

        new Thread(() -> {
            Looper.prepare();
            manager.setText(info);

            String[] lines = info.split("\n");
            String briefInfo = "程序发生闪退\n";
            for (String line : lines) {
                if (!line.trim().startsWith("at")) {
                    briefInfo += line + "\n";
                }
            }
            briefInfo += "详细错误信息已复制到剪贴板";

            Toast.makeText(mContext, briefInfo, Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
        return true;
    }
}
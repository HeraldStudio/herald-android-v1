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
        ClipboardManager manager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        boolean GODMODE = manager.getText().toString().equals("IAmTheGodOfHerald");

        // 如果当前剪贴板中的文字是IAmTheGodOfHerald，开启上帝模式，输出调试信息到剪贴板
        if (GODMODE) {
            new Thread(() -> {
                Looper.prepare();
                manager.setText(info);

                String[] lines = info.split("\n");
                String briefInfo = "小猴偷米上帝模式\n";
                for (String line : lines) {
                    if (!line.trim().startsWith("at")) {
                        briefInfo += line + "\n";
                    }
                }
                briefInfo += "详细错误信息已复制到剪贴板";

                Toast.makeText(mContext, briefInfo, Toast.LENGTH_LONG).show();
                Looper.loop();
            }).start();
        }

        // 如果不是上帝模式，只有彩蛋的结尾会输出信息，其它错误吃掉
        else if (info.contains("showTrickyMessage")) {
            new Thread(() -> {
                Looper.prepare();
                manager.setText(info + "彩蛋到此结束，如果你喜欢玩技术，那就到小猴偷米工作室来做客吧~");

                String[] lines = info.split("\n");
                String briefInfo = "小猴卒\n";
                for (String line : lines) {
                    if (!line.trim().startsWith("at")) {
                        briefInfo += line + "\n";
                    }
                }
                briefInfo += "详细错误信息已复制到剪贴板";

                Toast.makeText(mContext, briefInfo, Toast.LENGTH_LONG).show();
                Looper.loop();
            }).start();
        }
    }
}
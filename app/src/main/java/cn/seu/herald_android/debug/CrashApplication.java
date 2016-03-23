package cn.seu.herald_android.debug;

import android.app.Application;

public class CrashApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        //指定Crash时的处理程序
        crashHandler.setCrashHandler(getApplicationContext());
    }
}
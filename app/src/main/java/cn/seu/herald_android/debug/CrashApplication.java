package cn.seu.herald_android.debug;

import cn.seu.herald_android.framework.AppContext;

public class CrashApplication extends AppContext {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        //指定Crash时的处理程序
        crashHandler.setCrashHandler(getApplicationContext());
    }
}
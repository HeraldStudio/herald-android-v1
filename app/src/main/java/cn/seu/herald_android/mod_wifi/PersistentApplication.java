package cn.seu.herald_android.mod_wifi;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by vhyme on 2015/12/25 025.
 */
public class PersistentApplication extends Application {
    PersistentReceiver receiver = new PersistentReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(receiver);
        super.onTerminate();
    }
}
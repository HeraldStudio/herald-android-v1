package cn.seu.herald_android;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.seu.herald_android.mod_wifi.NetworkService;

/**
 * Created by vhyme on 2015/12/25 025.
 */

public class PersistentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
            //检查Service状态
            boolean isServiceRunning = false;
            ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if("cn.seu.herald_android.mod_wifi.NetworkService".equals(service.service.getClassName())) {
                    isServiceRunning = true;
                }
            }
            if (!isServiceRunning) {
                Intent i = new Intent(context, NetworkService.class);
                context.startService(i);
            }
        }
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, NetworkService.class);
            context.startService(i);
        }
    }
}
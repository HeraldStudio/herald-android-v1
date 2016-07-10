package cn.seu.herald_android.app_framework;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class SystemUtil {
    public static String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager pm = AppContext.currentContext.get().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(AppContext.currentContext.get().getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }


    public static int getAppVersionCode() {
        int versionCode = 0;
        try {
            PackageManager pm = AppContext.currentContext.get().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(AppContext.currentContext.get().getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}

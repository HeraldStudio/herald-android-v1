package cn.seu.herald_android.mod_wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import cn.seu.herald_android.R;

public class NetworkShortcutHelper {

    private Context context;

    public NetworkShortcutHelper(Context context){
        this.context = context;
    }
    private boolean hasShortcut() {
        boolean isInstallShortcut = false;
        final ContentResolver cr = context.getContentResolver();
        final String AUTHORITY = "com.android.launcher.settings";
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                new String[]{"登录校园网"}, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        return isInstallShortcut;
    }

    public void addShortcut(){

        if(hasShortcut()) return;

        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        //快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, "登录校园网");
        shortcut.putExtra("duplicate", false); //不允许重复创建

        Intent shortcutIntent = new Intent();
        shortcutIntent.setClass(context.getApplicationContext(), NetworkShortcutActivity.class);
        shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
        shortcutIntent.setAction("android.intent.action.MAIN");
        shortcutIntent.setFlags(0x10200000);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        //快捷方式的图标
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.
                fromContext(context, R.mipmap.ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        context.sendBroadcast(shortcut);
    }
}

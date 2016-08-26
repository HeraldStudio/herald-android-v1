package cn.seu.herald_android.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import cn.seu.herald_android.framework.AppContext;

public class ShareHelper {
    public static void share(String content) {
        ClipboardManager manager = (ClipboardManager) AppContext.instance.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("小猴偷米分享", content));
        AppContext.showMessage("分享内容已复制到剪贴板，快去粘贴吧");
    }
}

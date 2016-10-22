package cn.seu.herald_android.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import cn.seu.herald_android.framework.AppContext;

public class ShareHelper {
    public static void share(String content) {
        copyToClipboard(content, "分享内容已复制到剪贴板，快去粘贴吧");
    }

    public static void copyToClipboard(String content, String message) {
        ClipboardManager manager = (ClipboardManager) AppContext.instance.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("来自小猴偷米App", content));
        AppContext.showMessage(message);
    }
}

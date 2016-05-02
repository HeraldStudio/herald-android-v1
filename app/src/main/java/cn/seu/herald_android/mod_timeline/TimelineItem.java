package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;

import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;

public class TimelineItem {
    // 消息是否重要，不重要的消息总在后面
    public static final int CONTENT_NOTIFY = 0, CONTENT_NO_NOTIFY = 1, NO_CONTENT = 2;
    public ArrayList<View> attachedView = new ArrayList<>();
    private String name;
    private String info;
    private long time;
    private int contentPriority;
    private View.OnClickListener onClickListener;
    private int iconRes;
    public int moduleId = -1;

    public TimelineItem(String name, String info, long time, int contentPriority, int iconRes) {
        this.name = name;
        this.info = info;
        this.time = time;
        this.contentPriority = contentPriority;
        this.iconRes = iconRes;
    }

    public TimelineItem(int module, long time, int contentPriority, String info) {
        this.name = SettingsHelper.moduleNamesTips[module];
        this.info = info;
        this.time = time;
        this.contentPriority = contentPriority;
        this.onClickListener = (v) -> {
            Intent intent = new Intent(SettingsHelper.moduleActions[module]);
            v.getContext().startActivity(intent);
        };
        this.iconRes = SettingsHelper.moduleIconsId[module];
        this.moduleId = module;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public int getIconRes() {
        return iconRes;
    }

    public long getTime() {
        return time;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public int getContentPriority() {
        return contentPriority;
    }

    @Override
    public String toString() {
        String ret = name + "|" + info + "|";
        for (View k : attachedView) {
            ret += k.toString();
        }
        return ret;
    }

    public boolean isRead(Context context) {
        String code = String.valueOf(toString().hashCode());
        String key = "herald_cards_read_" + moduleId;
        return new CacheHelper(context).getCache(key).equals(code);
    }

    public int getDisplayPriority(Context context) {
        if (contentPriority == CONTENT_NOTIFY && isRead(context)) {
            return CONTENT_NO_NOTIFY;
        }
        return contentPriority;
    }

    public void markAsRead(Context context) {
        String code = String.valueOf(toString().hashCode());
        String key = "herald_cards_read_" + moduleId;
        new CacheHelper(context).setCache(key, code);
    }
}
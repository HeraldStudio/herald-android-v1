package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CustomButton;
import cn.seu.herald_android.helper.SettingsHelper;

public class TimelineItem {
    // 消息是否重要，不重要的消息总在后面
    public static final int CONTENT_NOTIFY = 0, CONTENT_NO_NOTIFY = 1, NO_CONTENT = 2;
    public ArrayList<View> attachedView = new ArrayList<>();
    private String name;
    private String info;
    private long time;
    private int importance;
    private View.OnClickListener onClickListener;
    private int iconRes;
    public int moduleId = -1;

    // 按时间先后顺序排列
    public static Comparator<TimelineItem> comparator =
            (item1, item2) -> {
                // 不重要的消息总在后面
                return item1.importance - item2.importance;
            };

    public TimelineItem(String name, String info, long time, int importance, int iconRes) {
        this.name = name;
        this.info = info;
        this.time = time;
        this.importance = importance;
        this.iconRes = iconRes;
    }

    public TimelineItem(int module, long time, int importance, String info) {
        this.name = SettingsHelper.moduleNamesTips[module];
        this.info = info;
        this.time = time;
        this.importance = importance;
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

    public int getImportance() {
        return importance;
    }

    public void addButton(Context context, String title, View.OnClickListener onClickListener) {
        CustomButton button = new CustomButton(context);
        button.setText(title);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        button.setBackground(ContextCompat.getDrawable(context, R.drawable.timeline_attached_block_bg));
        button.setOnClickListener(onClickListener);
        attachedView.add(button);
    }
}
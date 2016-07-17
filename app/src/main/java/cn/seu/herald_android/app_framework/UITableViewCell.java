package cn.seu.herald_android.app_framework;

import android.widget.FrameLayout;

public class UITableViewCell extends FrameLayout {

    private int resId;

    public UITableViewCell(int resId) {
        super(AppContext.currentContext.$get());
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }
}

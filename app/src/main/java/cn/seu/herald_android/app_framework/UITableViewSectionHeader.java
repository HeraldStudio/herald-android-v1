package cn.seu.herald_android.app_framework;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;

public class UITableViewSectionHeader extends TextView {

    private static final int paddingBottom = 5;

    private int mHeight = 30;

    private int mLeftInset = 15, mRightInset = 15;

    public UITableViewSectionHeader(Context context) {
        super(context);
        setPadding(UI.dp2px(mLeftInset), UI.dp2px(mHeight - paddingBottom),
                UI.dp2px(mRightInset), UI.dp2px(paddingBottom));
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        setTextColor(Color.parseColor("#666666"));
    }

    public void setInsets(int leftInset, int rightInset) {
        mLeftInset = leftInset;
        mRightInset = rightInset;
        setPadding(UI.dp2px(mLeftInset), UI.dp2px(mHeight - paddingBottom),
                UI.dp2px(mRightInset), UI.dp2px(paddingBottom));
    }
}

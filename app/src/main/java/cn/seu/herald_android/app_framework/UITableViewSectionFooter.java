package cn.seu.herald_android.app_framework;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;

public class UITableViewSectionFooter extends TextView {

    private static final int paddingTop = 5;

    private int mHeight = 30;

    private int mLeftInset = 15, mRightInset = 15;

    public UITableViewSectionFooter(Context context) {
        super(context);
        setPadding(UI.dp2px(mLeftInset), UI.dp2px(paddingTop),
                UI.dp2px(mRightInset), UI.dp2px(mHeight - paddingTop));
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        setTextColor(Color.parseColor("#666666"));
    }

    public void setInsets(int leftInset, int rightInset) {
        mLeftInset = leftInset;
        mRightInset = rightInset;
        setPadding(UI.dp2px(mLeftInset), UI.dp2px(paddingTop),
                UI.dp2px(mRightInset), UI.dp2px(mHeight - paddingTop));
    }
}

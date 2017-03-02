package cn.seu.herald_android.app_main;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FakeBoldTextView extends TextView {

    FakeBoldTextView(Context context) {
        super(context);
        getPaint().setFakeBoldText(true);
    }

    FakeBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getPaint().setFakeBoldText(true);
    }
}

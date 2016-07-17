package cn.seu.herald_android.app_framework;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.AbsListView;

public class UITableViewDivider extends View {

    private int mLeftInset = 15, mRightInset = 15;

    private int mColor = Color.rgb(229, 229, 229);

    private Paint mPaint = new Paint();

    public UITableViewDivider(Context context) {
        super(context);
        setLayoutParams(new AbsListView.LayoutParams(-1, (int) Math.max(UI.dp2px(0.5f), 1)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mColor);
        canvas.drawRect(UI.dp2px(mLeftInset), 0,
                canvas.getWidth() - UI.dp2px(mLeftInset + mRightInset), canvas.getHeight(), mPaint);
    }

    public void setInsets(int leftInset, int rightInset) {
        mLeftInset = leftInset;
        mRightInset = rightInset;
        invalidate();
    }

    public void setColor(int color) {
        mColor = color;
        invalidate();
    }
}

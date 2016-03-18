package cn.seu.herald_android.custom;

import android.content.Context;
import android.util.TypedValue;
import android.widget.Toast;

import cn.seu.herald_android.R;

public class ContextUtils {
    public static void showMessage(Context context, String message) {
        if (context instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) context).showMsg(message);
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static int getColorPrimary(Context context) {
        // 获取该主题下的主色调
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.resourceId;
    }
}

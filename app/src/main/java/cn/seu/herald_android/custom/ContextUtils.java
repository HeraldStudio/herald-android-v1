package cn.seu.herald_android.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.widget.Toast;

import cn.seu.herald_android.R;

public class ContextUtils {

    /**
     * 实现根据Context类型的不同，显示不同形式的消息
     **/
    public static void showMessage(Context context, String message) {
        if (context instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) context).showSnackBar(message);
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showMessage(Context context, String message, String actionTitle, Runnable action) {
        if (context instanceof BaseAppCompatActivity) {
            ((BaseAppCompatActivity) context).showSnackBar(message, actionTitle, action);
        } else {
            new AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(actionTitle, (dialog, which) -> {
                        action.run();
                    })
                    .setNegativeButton("我知道了", null)
                    .show();
        }
    }

    /**
     * 获取该场景下主题的主色调，返回对应的颜色资源id
     **/
    public static int getColorPrimary(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.resourceId;
    }
}

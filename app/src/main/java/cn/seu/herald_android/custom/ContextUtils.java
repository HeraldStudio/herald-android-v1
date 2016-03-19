package cn.seu.herald_android.custom;

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
            ((BaseAppCompatActivity) context).showMsg(message);
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 实现有多个联网请求连续出错时，前面的消息都吃掉，吐的时候只把最后一个吐出来
     **/
    public static String eatenMessage = null;

    public static void eatMessage(String message) {
        eatenMessage = message;
    }

    public static void flushMessage(Context context) {
        if (eatenMessage != null) {
            showMessage(context, eatenMessage);
            eatenMessage = null;
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

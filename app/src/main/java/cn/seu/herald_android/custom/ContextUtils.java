package cn.seu.herald_android.custom;

import android.content.Context;
import android.widget.Toast;

import cn.seu.herald_android.BaseAppCompatActivity;

public class ContextUtils {
    public static void showMessage(Context context, String message){
        if(context instanceof BaseAppCompatActivity){
            ((BaseAppCompatActivity) context).showMsg(message);
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}

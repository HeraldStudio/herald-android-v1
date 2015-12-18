package cn.seu.herald_android.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by vhyme on 2015/12/18 018.
 */
public class ModuleHelper {
    // 各模块的序号
    public static final int MODULE_PAOCAO = 0;

    // 各模块的action名，与上面的序号顺序一致，需要在各模块Manifest中进行定义
    private static final String[] moduleActions = {
            "cn.edu.seu.herald.MODULE_PAOCAO"
    };

    // 启动一个模块的Activity
    public static boolean launchModuleActivity(Context context, int module, Bundle bundle){
        try {
            Intent intent = new Intent(moduleActions[module]);
            if(bundle != null) intent.putExtras(bundle);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

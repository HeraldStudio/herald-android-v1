package cn.seu.herald_android.helper;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import cn.seu.herald_android.exception.ModuleLoadException;

/**
 * 用于启动子查询模块的activity的工具类
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
    public static boolean launchModuleActivity(Context context, int module, Bundle bundle)throws ModuleLoadException{
        try {
            Intent intent = new Intent(moduleActions[module]);
            if(bundle != null) intent.putExtras(bundle);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            throw new ModuleLoadException("模块未安装，请先下载安装",ModuleLoadException.MODE_NOT_INSTALLED);
        }
        return true;
    }
}

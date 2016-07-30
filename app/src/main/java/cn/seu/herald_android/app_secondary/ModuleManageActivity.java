package cn.seu.herald_android.app_secondary;

import android.os.Bundle;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.framework.BaseActivity;

public class ModuleManageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_mod);

        // 获得所有模块列表
        List<AppModule> seuModuleArrayList = Arrays.asList(Module.array);
        // 根据模块列表构造列表
        ListView listView = (ListView) findViewById(R.id.lsit_edit_shortcut);
        if (listView != null) {
            listView.setAdapter(new ModuleManageAdapter(getBaseContext(),
                    R.layout.mod_mod__item, seuModuleArrayList));
        }
    }
}

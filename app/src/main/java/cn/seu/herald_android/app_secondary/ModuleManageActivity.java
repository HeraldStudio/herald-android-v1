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

    /** 为了避免卡顿，模块管理列表已停用视图回收，改用 ScrollView 嵌套无滚动的 ListView 实现 */
    /**
     * 详情请见 mod_mod__content.xml
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_mod);

        // 获得所有模块列表
        List<AppModule> seuModuleArrayList = Arrays.asList(Module.array);
        // 根据模块列表构造列表
        ListView listView = (ListView) findViewById(R.id.list_edit_shortcut);
        if (listView != null) {
            listView.setAdapter(new ModuleManageAdapter(getBaseContext(),
                    R.layout.mod_mod__item, seuModuleArrayList));
        }
    }
}
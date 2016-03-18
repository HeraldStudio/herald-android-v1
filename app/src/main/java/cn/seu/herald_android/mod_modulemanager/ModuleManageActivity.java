package cn.seu.herald_android.mod_modulemanager;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class ModuleManageActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_manage);
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimary));
        init();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        //获得所有模块列表
        ArrayList<SeuModule> seuModuleArrayList = getSettingsHelper().getSeuModuleList();
        //根据模块列表构造列表
        ListView listView = (ListView) findViewById(R.id.lsit_edit_shortcut);
        listView.setAdapter(new ShortCutBoxEditAdapter(getBaseContext(),
                R.layout.listviewitem_edit_shortcut, seuModuleArrayList));
    }
}

package cn.seu.herald_android.mod_query;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_modulemanager.SeuModule;
import cn.seu.herald_android.mod_modulemanager.ShortCutBoxEditAdapter;

public class QueryActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
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
                R.layout.listview_edit_shortcut, seuModuleArrayList));
        //设置监听
        listView.setOnItemClickListener(new ShortCutBoxEditListItemClickListener());
    }


    private class ShortCutBoxEditListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
            SwitchButton switch1 = (SwitchButton) view.findViewById(R.id.switch1);
            switch1.toggle();
        }
    }
}

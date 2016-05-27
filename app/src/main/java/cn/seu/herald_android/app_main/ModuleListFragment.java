package cn.seu.herald_android.app_main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.ModuleManageActivity;
import cn.seu.herald_android.mod_modulemanager.SeuModule;

public class ModuleListFragment extends Fragment {

    private View contentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_main_modules, container, false);
        loadModuleList();
        return contentView;
    }

    //模块管理的按钮
    private View editButton;
    private ArrayList<SeuModule> seuModuleArrayList = new ArrayList<>();

    @Override
    public void onResume() {
        // 从模块管理界面返回时,重载模块列表
        super.onResume();
        loadModuleList();
    }

    public void loadModuleList() {

        if (contentView == null) return;

        //获得所有模块列表
        seuModuleArrayList.clear();
        ArrayList<SeuModule> list = new SettingsHelper(getContext()).getSeuModuleList();
        for (SeuModule k : list) {
            //加入所有模块，如果想对加入的模块进行筛选，请在这里进行
            seuModuleArrayList.add(k);
        }

        //根据模块列表构造列表
        ListView listView = (ListView) contentView.findViewById(R.id.list_modules);

        if (editButton == null) {
            editButton = getLayoutInflater(null).inflate(R.layout.fragment_module_edit_button, null);
            editButton.setOnClickListener((v) -> {
                Intent intent = new Intent(getContext(), ModuleManageActivity.class);
                startActivity(intent);
            });
            listView.addHeaderView(editButton);
        }

        ListAdapter adapter;
        if ((adapter = listView.getAdapter()) == null) {
            listView.setAdapter(new ModuleListAdapter(getContext(),
                    R.layout.listviewitem_modules, seuModuleArrayList));
        } else {
            while (adapter instanceof HeaderViewListAdapter) {
                adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            }
            if (adapter instanceof ArrayAdapter) {
                ((ArrayAdapter) adapter).notifyDataSetChanged();
            }
        }
    }
}

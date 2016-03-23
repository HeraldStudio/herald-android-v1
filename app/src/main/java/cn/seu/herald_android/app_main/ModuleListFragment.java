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
import java.util.Collections;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.ModuleManageActivity;
import cn.seu.herald_android.mod_modulemanager.SeuModule;

public class ModuleListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_modules, container, false);
    }

    @Override
    public void onResume() {
        super.onStart();
        loadModuleList();
    }

    private View editButton;
    private ArrayList<SeuModule> seuModuleArrayList = new ArrayList<>();

    public void loadModuleList() {

        //获得所有模块列表
        seuModuleArrayList.clear();
        seuModuleArrayList.addAll(new SettingsHelper(getContext()).getSeuModuleList());
        Collections.sort(seuModuleArrayList, (module1, module2) -> {
            boolean isVisible1 = module1.isEnabledShortCut() || module1.isEnabledCard();
            boolean isVisible2 = module2.isEnabledShortCut() || module2.isEnabledCard();
            return (isVisible1 == isVisible2) ? 0 : (isVisible1 ? -1 : 1);
        });

        //根据模块列表构造列表
        ListView listView = (ListView) getView().findViewById(R.id.list_modules);

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

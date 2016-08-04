package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.seu.herald_android.R;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppModule;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.SettingsHelper;

public class ModuleListFragment extends Fragment implements ApiHelper.OnUserChangeListener,
        SettingsHelper.OnModuleSettingsChangeListener {

    private View contentView;

    private Unbinder unbinder;

    @BindView(R.id.list_modules)
    public ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.app_main__fragment_modules, container, false);
        unbinder = ButterKnife.bind(this, contentView);

        // 添加页脚以防止被透明Tab挡住
        View footer = new View(getContext());
        footer.setLayoutParams(new AbsListView.LayoutParams(-1, (int)getResources().getDimension(R.dimen.bottom_tab_height)));
        listView.addFooterView(footer);

        loadModuleList();

        // 监听用户改变事件
        ApiHelper.registerOnUserChangeListener(this);
        // 监听模块设置改变事件
        SettingsHelper.registerOnModuleSettingsChangeListener(this);

        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 防泄漏
        ApiHelper.unregisterOnUserChangeListener(this);
        SettingsHelper.unregisterOnModuleSettingsChangeListener(this);

        unbinder.unbind();
    }

    @Override
    public void onUserChange() {
        loadModuleList();
    }

    @Override
    public void onModuleSettingsChange() {
        loadModuleList();
    }

    // 模块管理的按钮
    private View editButton;
    private ArrayList<AppModule> seuModuleArrayList = new ArrayList<>();

    public void loadModuleList() {

        if (contentView == null) return;

        // 获得所有模块列表
        seuModuleArrayList.clear();
        List<AppModule> list = Arrays.asList(Module.array);
        for (AppModule k : list) {
            // 筛选已开启的模块
            if (k.getCardEnabled() || k.getShortcutEnabled()) {
                seuModuleArrayList.add(k);
            }
        }

        if (editButton == null) {
            editButton = getLayoutInflater(null).inflate(R.layout.app_main__fragment_modules__item_manage, null);
            editButton.setOnClickListener((v) -> Module.moduleManager.open());
            listView.addHeaderView(editButton);
        }

        ListAdapter adapter;
        if ((adapter = listView.getAdapter()) == null) {
            listView.setAdapter(new ModuleListAdapter(getContext(),
                    R.layout.app_main__fragment_modules__item, seuModuleArrayList));
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

package cn.seu.herald_android.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import java.util.ArrayList;

import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.SeuModule;
import cn.seu.herald_android.mod_modulemanager.ShortCutBoxDisplayAdapter;

public class ShortcutBoxView extends GridView {
    public ShortcutBoxView(Context c, AttributeSet a) {
        super(c, a);
        refresh();
    }

    // 由于需要嵌套在ListView中，要重写onMeasure()防止高度获取出错
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public void refresh() {
        //加载适配器
        //获取设置为快捷方式的查询模块
        ArrayList<SeuModule> settingArrayList = new SettingsHelper(getContext()).getSeuModuleList();
        ArrayList<SeuModule> enabledShortcutList = new ArrayList<>();
        for (SeuModule module : settingArrayList) {
            if (module.isEnabledShortCut())
                enabledShortcutList.add(module);
        }

        ShortCutBoxDisplayAdapter adapter = new ShortCutBoxDisplayAdapter(enabledShortcutList);
        //智能分配行数和列数
        int count = adapter.getCount();
        if (count > 5) {
            setNumColumns(Math.max(4, (count + 1) / ((count + 4) / 5)));
        } else {
            setNumColumns(Math.max(4, count));
        }
        //添加并且显示
        setAdapter(adapter);

        if (adapter.getCount() == 1) setVisibility(GONE);
        else setVisibility(VISIBLE);
    }
}

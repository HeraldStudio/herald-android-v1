package cn.seu.herald_android.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppModule;

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
        // 加载适配器
        // 获取设置为快捷方式的查询模块
        List<AppModule> settingArrayList = Arrays.asList(Module.array);
        List<AppModule> enabledShortcutList = new ArrayList<>();
        for (AppModule module : settingArrayList) {
            if (module.getShortcutEnabled())
                enabledShortcutList.add(module);
        }

        ShortcutBoxAdapter adapter = new ShortcutBoxAdapter(enabledShortcutList);

        // 添加并且显示
        setAdapter(adapter);

        if (adapter.getCount() == 1) setVisibility(GONE);
        else setVisibility(VISIBLE);
    }
}

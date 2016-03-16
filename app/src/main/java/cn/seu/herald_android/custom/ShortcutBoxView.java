package cn.seu.herald_android.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.SeuModule;
import cn.seu.herald_android.mod_modulemanager.ShortCutBoxDisplayAdapter;
import cn.seu.herald_android.mod_timeline.TimelineView;

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
        SimpleAdapter simpleAdapter = ShortCutBoxDisplayAdapter.getShortCutBoxViewSimpleAdapter(
                getContext(), settingArrayList
        );
        //智能分配行数和列数
        int count = simpleAdapter.getCount();
        if (count > 5) {
            setNumColumns(Math.max(4, (count + 1) / ((count + 4) / 5)));
        } else {
            setNumColumns(Math.max(4, count));
        }
        //添加并且显示
        setAdapter(simpleAdapter);
        //添加点击响应
        setOnItemClickListener((parent, view, position, id) -> {
            //获得点击项的map
            HashMap<String, Object> clickItemMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.setAction(clickItemMap.get("Aciton").toString());
            getContext().startActivity(intent);
        });
        //添加长按响应,询问用户是否从快捷栏上删除·
        setOnItemLongClickListener((parent, view, position, id) -> {
            HashMap<String, Object> clickItemMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
            int moduleId = (int) clickItemMap.get("ModuleId");

            if (moduleId == -1) return false; // "添加"按钮不设置长按响应

            SettingsHelper settingsHelper = new SettingsHelper(getContext());
            new AlertDialog.Builder(getContext())
                    .setMessage("确定移除此模块的快捷方式和卡片吗？\n(可在侧边栏→查询助手中找回)")
                    .setPositiveButton("确定", (dialog, which) -> {
                        //设置为不可用
                        settingsHelper.setModuleShortCutEnabled(moduleId, false);
                        //刷新整个列表
                        for (ViewParent v = getParent(); v != null; v = v.getParent()) {
                            if (v instanceof TimelineView) {
                                ((TimelineView) v).loadContent(true);
                                return;
                            }
                        }
                    })
                    .setNegativeButton("取消", (dialog1, which1) -> {

                    }).show();
            //返回true以保证setOnItemClickListener不再执行
            return true;
        });

        if (simpleAdapter.getCount() == 0) setVisibility(GONE);
        else setVisibility(VISIBLE);
    }
}

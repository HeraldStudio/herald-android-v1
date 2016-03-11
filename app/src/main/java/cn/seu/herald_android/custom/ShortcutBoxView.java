package cn.seu.herald_android.custom;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.SeuModule;
import cn.seu.herald_android.mod_modulemanager.ShortCutBoxDisplayAdapter;

public class ShortcutBoxView extends GridView {
    public ShortcutBoxView(Context c, AttributeSet a){
        super(c, a);
        refresh();
    }

    // 由于需要嵌套在ListView中，要重写onMeasure()防止高度获取出错
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void refresh(){
        //加载适配器
        //获取设置为快捷方式的查询模块
        ArrayList<SeuModule> settingArrayList = new SettingsHelper(getContext()).getSeuModuleList();
        SimpleAdapter simpleAdapter = ShortCutBoxDisplayAdapter.getShortCutBoxViewSimpleAdapter(
                getContext(), settingArrayList
        );
        //添加并且显示
        setAdapter(simpleAdapter);
        //添加响应
        setOnItemClickListener((parent, view, position, id) -> {
            //获得点击项的map
            HashMap<String, Object> clickItemMap = (HashMap<String, Object>) parent.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.setAction(clickItemMap.get("Aciton").toString());
            getContext().startActivity(intent);
        });
        if(simpleAdapter.getCount() == 0) setVisibility(GONE);
        else setVisibility(VISIBLE);
    }
}

package cn.seu.herald_android.mod_modulemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.seu.herald_android.R;

/**
 * 这个适配器类所适配的功能是：展示所给的模块列表
 * Created by heyon on 2016/3/7.
 */
public class ShortCutBoxDisplayAdapter extends ArrayAdapter<SeuModule> {
    public ShortCutBoxDisplayAdapter(Context context, int resource, List<SeuModule> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SeuModule seuModule = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_edit_shortcut, null);
        }
        //快捷方式图标
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(seuModule.getIc_id());
        //文字说明
        TextView textView = (TextView) convertView.findViewById(R.id.tv_shortcut);
        textView.setText(seuModule.getName());
        return convertView;
    }

    public static SimpleAdapter getShortCutBoxViewSimpleAdapter(Context context, ArrayList<SeuModule> list) {
        //根据传入的模块列表生成展示这些模块的GridView或者ListView的simpleAdapter
        ArrayList<HashMap<String, Object>> shorcutHashMapArrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SeuModule seuModuleItem = list.get(i);
            HashMap<String, Object> map = new HashMap<>();
            if (seuModuleItem.isEnabledShortCut()) {
                map.put("ItemIcon", seuModuleItem.getIc_id());//添加图标
                map.put("ItemName", seuModuleItem.getName());//添加按钮文字
                map.put("Aciton", seuModuleItem.getActions());//添加打开模块的动作
                map.put("ModuleId", seuModuleItem.getModuleId());//添加模块对应id
                shorcutHashMapArrayList.add(map);
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("ItemIcon", R.mipmap.ic_add);//添加图标
        map.put("ItemName", "添加");//添加按钮文字
        map.put("Aciton", "cn.seu.herald_android.MODULE_QUERY_MAIN");//添加打开模块的动作
        map.put("ModuleId", -1);//添加模块对应id
        shorcutHashMapArrayList.add(map);

        //生成适配器的元素
        SimpleAdapter simpleAdapter = new SimpleAdapter(context, shorcutHashMapArrayList,
                R.layout.gridviewitem_display_shortcut, new String[]{"ItemIcon", "ItemName"},
                new int[]{R.id.ic_shortcut, R.id.tv_shortcut});
        return simpleAdapter;
    }
}

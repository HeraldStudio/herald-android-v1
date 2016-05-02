package cn.seu.herald_android.app_main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_modulemanager.SeuModule;

/**
 * 这个适配器类所适配的功能是：展示所给的模块列表，并且允许用户设置这些模块的快捷方式是否可用
 * Created by heyon on 2016/3/8.
 */
public class ModuleListAdapter extends ArrayAdapter<SeuModule> {
    //设置为显示的快捷方式列表
    private SettingsHelper settingsHelper;

    public ModuleListAdapter(Context context, int resource, List<SeuModule> objects) {
        super(context, resource, objects);
        settingsHelper = new SettingsHelper(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SeuModule seuModule = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_modules, null);
        }

        boolean isVisible1 = position != 0 &&
                (getItem(position - 1).isEnabledShortCut() || getItem(position - 1).isEnabledCard());
        boolean isVisible2 = getItem(position).isEnabledShortCut() || getItem(position).isEnabledCard();

        TextView tv_header = (TextView) convertView.findViewById(R.id.module_item_header);
        tv_header.setVisibility((position == 0 || isVisible1 != isVisible2) ? View.VISIBLE : View.GONE);
        tv_header.setText(isVisible2 ? "显示在卡片或快捷栏的模块" : "完全隐藏的模块");
        tv_header.setOnClickListener((v) -> {
        });

        //快捷方式图标
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(seuModule.getIc_id());
        //文字标题
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_shortcut);
        tv_title.setText(seuModule.getName());
        //文字说明
        TextView tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
        tv_desc.setText(seuModule.getDescription());

        convertView.setOnClickListener((v) -> {
            getContext().startActivity(new Intent(seuModule.getAction()));
        });
        return convertView;
    }

}

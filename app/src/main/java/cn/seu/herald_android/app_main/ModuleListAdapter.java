package cn.seu.herald_android.app_main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.AppModule;

public class ModuleListAdapter extends ArrayAdapter<AppModule> {

    public ModuleListAdapter(Context context, int resource, List<AppModule> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModule seuModule = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_modules__item, null);
        }

        TextView tv_header = (TextView) convertView.findViewById(R.id.module_item_header);
        tv_header.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        tv_header.setText("已启用的模块");

        //快捷方式图标
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(seuModule.icon);
        //文字标题
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_shortcut);
        tv_title.setText(seuModule.nameTip);
        //文字说明
        TextView tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
        tv_desc.setText(seuModule.desc);

        convertView.setOnClickListener((v) -> seuModule.open());
        return convertView;
    }

}

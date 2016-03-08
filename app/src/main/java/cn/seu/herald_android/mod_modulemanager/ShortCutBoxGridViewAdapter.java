package cn.seu.herald_android.mod_modulemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.SettingsHelper;

/**
 * Created by heyon on 2016/3/7.
 */
public class ShortCutBoxGridViewAdapter extends ArrayAdapter<SettingsHelper.ShortCutSetting> {
    //设置为显示的快捷方式列表
    ArrayList<SettingsHelper.ShortCutSetting> list;
    //布局
    int resource;
    public ShortCutBoxGridViewAdapter(Context context, int resource,ArrayList<SettingsHelper.ShortCutSetting> list) {
        super(context, resource);
        this.resource = resource;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsHelper.ShortCutSetting shortCutSetting = list.get(position);
        if( convertView == null ){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.gridviewitem_shortcut,null);
        }
        //快捷方式图标
        ImageView imageView =(ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(shortCutSetting.getIc_id());
        //文字说明
        TextView textView = (TextView)convertView.findViewById(R.id.tv_shortcut);
        textView.setText(shortCutSetting.getName());
        return convertView;
    }
}

package cn.seu.herald_android.mod_modulemanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.SettingsHelper;

/**
 * 这个适配器类所适配的功能是：展示所给的模块列表，并且允许用户设置这些模块的快捷方式是否可用
 * Created by heyon on 2016/3/8.
 */
public class ShortCutBoxEditAdapter extends ArrayAdapter<SeuModule> {
    //设置为显示的快捷方式列表
    SettingsHelper settingsHelper;

    public ShortCutBoxEditAdapter(Context context, int resource, List<SeuModule> objects) {
        super(context, resource, objects);
        settingsHelper = new SettingsHelper(context);
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SeuModule seuModule = getItem(position);
        if( convertView == null ){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_edit_shortcut,null);
        }
        //快捷方式图标
        ImageView imageView =(ImageView)convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(seuModule.getIc_id());
        //文字说明
        TextView textView = (TextView)convertView.findViewById(R.id.tv_shortcut);
        textView.setText(seuModule.getName());
        //显示或者不显示的大头针，蓝色代表快捷方式启用，灰色代表不启动
        ImageButton imageViewBtn = (ImageButton)convertView.findViewById(R.id.ibtn_shortcut);
        if(settingsHelper.getModuleShortCutEnabled(seuModule.getModuleId())){
            imageViewBtn.setImageResource(R.mipmap.ic_pin);
        }else {
            imageViewBtn.setImageResource(R.mipmap.ic_pin_gray);
        }
        imageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击时修改快捷方式是否显示
                int moduleId = seuModule.getModuleId();
                //如果是启用，点击后则禁用，反之亦然
                boolean enabeled = !settingsHelper.getModuleShortCutEnabled(moduleId);
                //应用设置
                settingsHelper.setModuleShortCutEnabled(moduleId,enabeled);
                //根据设置结果重新设置图标
                if(enabeled){
                    imageViewBtn.setImageResource(R.mipmap.ic_pin);
                }else {
                    imageViewBtn.setImageResource(R.mipmap.ic_pin_gray);
                }
            }
        });
        return convertView;
    }

}

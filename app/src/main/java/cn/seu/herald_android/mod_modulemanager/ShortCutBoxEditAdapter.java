package cn.seu.herald_android.mod_modulemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.SettingsHelper;

/**
 * 这个适配器类所适配的功能是：展示所给的模块列表，并且允许用户设置这些模块的快捷方式是否可用
 * Created by heyon on 2016/3/8.
 */
public class ShortCutBoxEditAdapter extends ArrayAdapter<SeuModule> {
    //设置为显示的快捷方式列表
    private SettingsHelper settingsHelper;

    public ShortCutBoxEditAdapter(Context context, int resource, List<SeuModule> objects) {
        super(context, resource, objects);
        settingsHelper = new SettingsHelper(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SeuModule seuModule = getItem(position);
        int moduleId = seuModule.getModuleId();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_edit_shortcut, null);
        }
        //快捷方式图标
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(seuModule.getIc_id());
        //文字标题
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_shortcut);
        tv_title.setText(seuModule.getName());

        //表示卡片是否显示在主页的开关
        SwitchButton switchCard = (SwitchButton) convertView.findViewById(R.id.switch_card);
        switchCard.setVisibility(SettingsHelper.moduleHasCard[moduleId] ? View.VISIBLE : View.GONE);
        switchCard.setOnCheckedChangeListener((v, checked) -> {
            //点击时修改快捷方式是否显示
            //应用设置
            settingsHelper.setModuleCardEnabled(moduleId, checked);
        });
        switchCard.setCheckedImmediately(settingsHelper.getModuleCardEnabled(moduleId));

        //表示快捷方式是否显示在主页的开关
        SwitchButton switchShortcut = (SwitchButton) convertView.findViewById(R.id.switch_shortcut);
        switchShortcut.setOnCheckedChangeListener((v, checked) -> {
            //应用设置
            settingsHelper.setModuleShortCutEnabled(moduleId, checked);
        });
        switchShortcut.setCheckedImmediately(settingsHelper.getModuleShortCutEnabled(moduleId));

        // 只有一个开关的条目设置点击事件
        if (switchCard.getVisibility() != View.VISIBLE) {
            convertView.setOnClickListener(v -> {
                boolean oldState = switchShortcut.isChecked();
                switchShortcut.setChecked(!oldState);
            });
        } else {
            convertView.setOnClickListener(null);
        }

        return convertView;
    }

}

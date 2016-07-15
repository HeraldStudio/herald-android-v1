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
import cn.seu.herald_android.helper.AppModule;

public class ModuleManageAdapter extends ArrayAdapter<AppModule> {

    public ModuleManageAdapter(Context context, int resource, List<AppModule> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModule seuModule = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_mod__item, null);
        }
        //快捷方式图标
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(seuModule.icon);
        //文字标题
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_shortcut);
        tv_title.setText(seuModule.nameTip);

        //表示卡片是否显示在主页的开关
        SwitchButton switchCard = (SwitchButton) convertView.findViewById(R.id.switch_card);
        switchCard.setVisibility(seuModule.hasCard ? View.VISIBLE : View.GONE);
        switchCard.setOnCheckedChangeListener((v, checked) -> seuModule.cardEnabled.$set(checked));
        switchCard.setCheckedImmediately(seuModule.cardEnabled.$get());

        //表示快捷方式是否显示在主页的开关
        SwitchButton switchShortcut = (SwitchButton) convertView.findViewById(R.id.switch_shortcut);
        switchShortcut.setOnCheckedChangeListener((v, checked) -> {
            //应用设置
            seuModule.shortcutEnabled.$set(checked);
        });
        switchShortcut.setCheckedImmediately(seuModule.shortcutEnabled.$get());

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

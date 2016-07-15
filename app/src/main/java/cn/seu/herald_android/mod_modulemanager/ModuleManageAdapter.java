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

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.AppModule;

public class ModuleManageAdapter extends ArrayAdapter<AppModule> {

    static class ViewHolder {
        @BindView(R.id.ic_shortcut)
        ImageView icon;
        @BindView(R.id.tv_shortcut)
        TextView title;
        @BindView(R.id.switch_card)
        SwitchButton switchCard;
        @BindView(R.id.switch_shortcut)
        SwitchButton switchShortcut;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ModuleManageAdapter(Context context, int resource, List<AppModule> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModule seuModule = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_mod__item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.icon.setImageResource(seuModule.icon);
        holder.title.setText(seuModule.nameTip);

        //表示卡片是否显示在主页的开关
        holder.switchCard.setVisibility(seuModule.hasCard ? View.VISIBLE : View.GONE);
        holder.switchCard.setOnCheckedChangeListener((v, checked) -> seuModule.cardEnabled.$set(checked));
        holder.switchCard.setCheckedImmediately(seuModule.cardEnabled.$get());

        //表示快捷方式是否显示在主页的开关
        holder.switchShortcut.setOnCheckedChangeListener((v, checked) -> {
            seuModule.shortcutEnabled.$set(checked);
        });
        holder.switchShortcut.setCheckedImmediately(seuModule.shortcutEnabled.$get());

        return convertView;
    }
}

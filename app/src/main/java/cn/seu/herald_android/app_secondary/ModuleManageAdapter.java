package cn.seu.herald_android.app_secondary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppModule;

public class ModuleManageAdapter extends ArrayAdapter<AppModule> {

    /** 为了避免卡顿，模块管理列表已停用视图回收，改用 ScrollView 嵌套无滚动的 ListView 实现 */
    /**
     * 详情请见 mod_mod__content.xml
     */

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

        holder.icon.setImageDrawable(null);
        Picasso.with(getContext()).load(seuModule.icon).into(holder.icon);
        holder.title.setText(seuModule.nameTip);

        // 表示卡片是否显示在主页的开关
        holder.switchCard.setVisibility(seuModule.hasCard ? View.VISIBLE : View.GONE);
        holder.switchCard.setOnCheckedChangeListener((v, checked) -> seuModule.setCardEnabled(checked));
        holder.switchCard.setCheckedImmediately(seuModule.getCardEnabled());

        // 表示快捷方式是否显示在主页的开关
        holder.switchShortcut.setOnCheckedChangeListener((v, checked) -> {
            seuModule.setShortcutEnabled(checked);
        });
        holder.switchShortcut.setCheckedImmediately(seuModule.getShortcutEnabled());

        return convertView;
    }
}

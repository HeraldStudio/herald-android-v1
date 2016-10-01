package cn.seu.herald_android.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppModule;

public class ShortcutBoxAdapter extends BaseAdapter {

    private List<AppModule> modules = new ArrayList<>();

    public ShortcutBoxAdapter(List<AppModule> modules) {
        this.modules = new ArrayList<>(modules);
    }

    @Override
    public int getCount() {
        return modules.size();
    }

    @Override
    public AppModule getItem(int position) {
        if (position == modules.size()) return null;

        return modules.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position == modules.size()) return 0;

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModule module = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_main__fragment_cards__item_shortcut_box__cell, null);
        }
        // 快捷方式图标
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
        imageView.setImageResource(module.invertIcon);
        // 文字说明
        TextView textView = (TextView) convertView.findViewById(R.id.tv_shortcut);
        textView.setText(module.nameTip.split(" ")[0]);

        int columnCount = ((GridView) parent).getNumColumns();

        convertView.findViewById(R.id.notify_dot).setVisibility(
                module.getHasUpdates() ? View.VISIBLE : View.GONE
        );

        convertView.setOnClickListener((v) -> {
            if (module.getHasUpdates()) {
                module.setHasUpdates(false);
            }
            module.open();
        });
        return convertView;
    }
}

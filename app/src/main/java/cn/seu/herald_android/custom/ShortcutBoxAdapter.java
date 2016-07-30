package cn.seu.herald_android.custom;

import android.app.AlertDialog;
import android.graphics.Color;
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
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppModule;

public class ShortcutBoxAdapter extends BaseAdapter {

    private List<AppModule> modules = new ArrayList<>();

    public ShortcutBoxAdapter(List<AppModule> modules) {
        this.modules = new ArrayList<>(modules);
    }

    @Override
    public int getCount() {
        return modules.size() + 1;
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
        if (position < modules.size()) {// 模块图标
            AppModule module = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_main__fragment_cards__item_shortcut_box__cell, null);
            }
            // 快捷方式图标
            ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
            imageView.setImageResource(module.icon);
            // 文字说明
            TextView textView = (TextView) convertView.findViewById(R.id.tv_shortcut);
            textView.setText(module.nameTip.split(" ")[0]);

            int columnCount = ((GridView)parent).getNumColumns();

            convertView.setBackgroundColor(
                    (position / columnCount + position % columnCount) % 2 == 1 ? (
                        Color.WHITE
                    ) : (
                        Color.rgb(248, 248, 248)
                    )
            );

            convertView.findViewById(R.id.notify_dot).setVisibility(
                    module.getHasUpdates() ? View.VISIBLE : View.GONE
            );

            convertView.setOnClickListener((v) -> module.open());

            convertView.setOnLongClickListener((v) -> {
                new AlertDialog.Builder(parent.getContext())
                        .setMessage("确定移除此模块的快捷方式吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 设置为不可用
                            module.setShortcutEnabled(false);
                        })
                        .setNegativeButton("取消", (dialog1, which1) -> {

                        }).show();
                return true;
            });
            return convertView;
        } else {// “添加”按钮
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_main__fragment_cards__item_shortcut_box__cell, null);

            int columnCount = ((GridView)parent).getNumColumns();

            v.setBackgroundColor(
                    (position / columnCount + position % columnCount) % 2 == 1 ? (
                            Color.WHITE
                    ) : (
                            Color.rgb(250, 250, 250)
                    )
            );

            // 快捷方式图标
            ImageView imageView = (ImageView) v.findViewById(R.id.ic_shortcut);
            imageView.setImageResource(R.mipmap.ic_add);
            // 文字说明
            TextView textView = (TextView) v.findViewById(R.id.tv_shortcut);
            textView.setText("模块管理");

            v.setOnClickListener((v1) -> Module.moduleManager.open());

            return v;
        }
    }
}

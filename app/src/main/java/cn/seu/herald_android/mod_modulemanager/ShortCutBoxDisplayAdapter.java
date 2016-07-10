package cn.seu.herald_android.mod_modulemanager;

import android.app.AlertDialog;
import android.content.Intent;
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
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.helper.AppModule;

public class ShortCutBoxDisplayAdapter extends BaseAdapter {

    private List<AppModule> modules = new ArrayList<>();

    public ShortCutBoxDisplayAdapter(List<AppModule> modules) {
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
            AppModule seuModule = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridviewitem_display_shortcut, null);
            }
            //快捷方式图标
            ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
            imageView.setImageResource(seuModule.icon);
            //文字说明
            TextView textView = (TextView) convertView.findViewById(R.id.tv_shortcut);
            textView.setText(seuModule.nameTip.split(" ")[0]);

            int columnCount = ((GridView)parent).getNumColumns();

            convertView.setBackgroundColor(
                    (position / columnCount + position % columnCount) % 2 == 1 ? (
                        Color.WHITE
                    ) : (
                        Color.rgb(248, 248, 248)
                    )
            );

            convertView.setOnClickListener((v) -> seuModule.open());

            convertView.setOnLongClickListener((v) -> {
                new AlertDialog.Builder(parent.getContext())
                        .setMessage("确定移除此模块的快捷方式吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            //设置为不可用
                            seuModule.shortcutEnabled.set(false);
                            if (parent.getContext() instanceof MainActivity) {
                                ((MainActivity) parent.getContext()).syncModuleSettings();
                            }
                        })
                        .setNegativeButton("取消", (dialog1, which1) -> {

                        }).show();
                return true;
            });
            return convertView;
        } else {// “添加”按钮
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridviewitem_display_shortcut, null);

            int columnCount = ((GridView)parent).getNumColumns();

            v.setBackgroundColor(
                    (position / columnCount + position % columnCount) % 2 == 1 ? (
                            Color.WHITE
                    ) : (
                            Color.rgb(248, 248, 248)
                    )
            );

            //快捷方式图标
            ImageView imageView = (ImageView) v.findViewById(R.id.ic_shortcut);
            imageView.setImageResource(R.mipmap.ic_add);
            //文字说明
            TextView textView = (TextView) v.findViewById(R.id.tv_shortcut);
            textView.setText("模块管理");

            v.setOnClickListener((v1) -> {
                Intent intent = new Intent();
                intent.setAction("cn.seu.herald_android.MODULE_QUERY_MAIN");
                parent.getContext().startActivity(intent);
            });

            return v;
        }
    }
}

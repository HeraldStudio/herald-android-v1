package cn.seu.herald_android.mod_modulemanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.MainActivity;
import cn.seu.herald_android.helper.SettingsHelper;

/**
 * 这个适配器类所适配的功能是：展示所给的模块列表
 * Created by heyon on 2016/3/7.
 */
public class ShortCutBoxDisplayAdapter extends BaseAdapter {

    private List<SeuModule> modules = new ArrayList<>();

    public ShortCutBoxDisplayAdapter(List<SeuModule> modules) {
        this.modules = new ArrayList<>(modules);
    }

    @Override
    public int getCount() {
        return modules.size() + 1;
    }

    @Override
    public SeuModule getItem(int position) {
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
            SeuModule seuModule = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridviewitem_display_shortcut, null);
            }
            //快捷方式图标
            ImageView imageView = (ImageView) convertView.findViewById(R.id.ic_shortcut);
            imageView.setImageResource(seuModule.getIc_id());
            //文字说明
            TextView textView = (TextView) convertView.findViewById(R.id.tv_shortcut);
            textView.setText(seuModule.getName());
            //web标签
            View webmodule = convertView.findViewById(R.id.tv_webmodule);
            webmodule.setVisibility(seuModule.getAction().contains("WEBMODULE") ? View.VISIBLE : View.GONE);

            convertView.setBackgroundColor(
                    position % 2 == 1 ? (
                        Color.WHITE
                    ) : (
                        Color.rgb(248, 248, 248)
                    )
            );

            convertView.setOnClickListener((v) -> {
                Intent intent = new Intent();
                intent.setAction(seuModule.getAction());
                parent.getContext().startActivity(intent);
            });

            convertView.setOnLongClickListener((v) -> {
                SettingsHelper settingsHelper = new SettingsHelper(parent.getContext());
                new AlertDialog.Builder(parent.getContext())
                        .setMessage("确定移除此模块的快捷方式吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            //设置为不可用
                            settingsHelper.setModuleShortCutEnabled(seuModule.getModuleId(), false);
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
            ;

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

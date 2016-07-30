package cn.seu.herald_android.app_main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppModule;

public class ModuleListAdapter extends ArrayAdapter<AppModule> {

    static class ViewHolder {
        @BindView(R.id.module_item_header)
        TextView header;
        @BindView(R.id.ic_shortcut)
        ImageView icon;
        @BindView(R.id.tv_shortcut)
        TextView title;
        @BindView(R.id.tv_desc)
        TextView desc;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ModuleListAdapter(Context context, int resource, List<AppModule> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_main__fragment_modules__item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        AppModule seuModule = getItem(position);

        holder.header.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        holder.icon.setImageResource(seuModule.icon);
        holder.title.setText(seuModule.nameTip);
        holder.desc.setText(seuModule.desc);

        convertView.setOnClickListener((v) -> seuModule.open());
        return convertView;
    }
}

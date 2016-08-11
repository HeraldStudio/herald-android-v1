package cn.seu.herald_android.app_module.curriculum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.EmptyTipArrayAdapter;

class CurriculumFloatClassAdapter extends EmptyTipArrayAdapter<SidebarClassModel> {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.desc)
        TextView desc;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private int resource;

    public CurriculumFloatClassAdapter(Context context, int resource, List<SidebarClassModel> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        final SidebarClassModel model = getItem(position);
        holder.title.setText(model.getClassName());
        holder.desc.setText(model.getDesc());
        return convertView;
    }
}

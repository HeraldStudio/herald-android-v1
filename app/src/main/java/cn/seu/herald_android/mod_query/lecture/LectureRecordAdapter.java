package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

class LectureRecordAdapter extends ArrayAdapter<LectureRecordModel> {

    static class ViewHolder {
        @BindView(R.id.content)
        TextView tv_time;
        @BindView(R.id.tv_place)
        TextView tv_place;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private int resource;

    public LectureRecordAdapter(Context context, int resource, List<LectureRecordModel> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LectureRecordModel lectureRecordItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.tv_time.setText(lectureRecordItem.time);
        holder.tv_place.setText(lectureRecordItem.place);
        return convertView;
    }
}

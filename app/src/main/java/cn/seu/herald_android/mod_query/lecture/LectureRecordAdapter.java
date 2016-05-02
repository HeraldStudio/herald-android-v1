package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

class LectureRecordAdapter extends ArrayAdapter<LectureRecordItem> {
    private int resource;

    public LectureRecordAdapter(Context context, int resource, List<LectureRecordItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LectureRecordItem lectureRecordItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resource, null);
        }
        //设置列表每项的时间控件
        TextView tv_time = (TextView) convertView.findViewById(R.id.content);
        //设置列表每项的地点控件
        TextView tv_place = (TextView) convertView.findViewById(R.id.tv_place);
        tv_time.setText(lectureRecordItem.time);
        tv_place.setText(lectureRecordItem.place);
        return convertView;
    }
}

package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/3/1.
 */
public class LectureRecordAdapter extends ArrayAdapter<LectureItem> {
    int resource;
    public LectureRecordAdapter(Context context, int resource, List<LectureItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LectureItem lectureItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resource, null);
        //设置列表每项的时间控件
        TextView tv_time = (TextView)view.findViewById(R.id.tv_time);
        //设置列表每项的地点控件
        TextView tv_place = (TextView)view.findViewById(R.id.tv_place);
        tv_time.setText(lectureItem.time);
        tv_place.setText(lectureItem.place);
        return view;
    }
}

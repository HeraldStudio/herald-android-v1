package cn.seu.herald_android.mod_query.schoolbus;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.R;

class SchoolBusExpandAdapter extends BaseExpandableListAdapter {
    private String[] titles;
    private ArrayList<ArrayList<SchoolBusModel>> childViews;
    private Context context;

    public SchoolBusExpandAdapter(Context context, String[] titles, ArrayList<ArrayList<SchoolBusModel>> childViews) {
        this.titles = titles;
        this.childViews = childViews;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return titles.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childViews.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return titles[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childViews.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String title = (String) getGroup(groupPosition);
        View contentView = LayoutInflater.from(this.context).inflate(R.layout.mod_que_schoolbus__fragment__item_parent, null);
        TextView view = (TextView) contentView.findViewById(R.id.tv_direction_bus);
        view.setText(title);
        return contentView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.mod_que_schoolbus__fragment__item_child, null);
        }
        SchoolBusModel schoolBusItem = (SchoolBusModel) getChild(groupPosition, childPosition);
        TextView tv_period = (TextView) convertView.findViewById(R.id.tv_schoolbusitem_period);
        TextView tv_time = (TextView) convertView.findViewById(R.id.tv_schoolbusitem_time);
        //判断是否是当前时间的区间
        try {
            String time = schoolBusItem.getPeriod();
            Calendar dateStart = Calendar.getInstance();
            Calendar dateEnd = Calendar.getInstance();
            dateStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
            dateStart.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1].split("-")[0]));
            dateEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[1].split("-")[1]));
            dateEnd.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[2]));
            Calendar now = Calendar.getInstance();
            if (now.after(dateStart) && now.before(dateEnd)) {
                //如果是当前时间所处于的时间区间，则设置颜色
                tv_period.setTextColor(ContextCompat.getColor(context, R.color.colorSchoolBusprimary_dark));
                tv_time.setTextColor(ContextCompat.getColor(context, R.color.colorSchoolBusprimary_dark));
            } else {
                tv_period.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                tv_time.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_period.setText(schoolBusItem.getPeriod());
        tv_time.setText(schoolBusItem.getTime());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
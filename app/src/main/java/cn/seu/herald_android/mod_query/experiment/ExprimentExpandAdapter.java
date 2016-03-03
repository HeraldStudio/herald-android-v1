package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/3/3.
 */
public class ExprimentExpandAdapter extends BaseExpandableListAdapter {
    ArrayList<String> parentViews;
    ArrayList<ArrayList<ExperimentItem>> childViews;
    Context context;

    public ExprimentExpandAdapter(Context context,ArrayList<String> parentViews, ArrayList<ArrayList<ExperimentItem>> childViews) {
        this.parentViews = parentViews;
        this.childViews = childViews;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return parentViews.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childViews.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parentViews.get(groupPosition);
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
        String title = (String)getGroup(groupPosition);
        View contentView = LayoutInflater.from(this.context).inflate(R.layout.expandablelistview_parentitem_experiment,null);
        TextView view = (TextView)contentView.findViewById(R.id.tv_type_expriment);
        view.setText(title);
        return contentView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View contentView = LayoutInflater.from(this.context).inflate(R.layout.expandablelistview_childitem_experiment,null);
        ExperimentItem experimentItem = (ExperimentItem)getChild(groupPosition,childPosition);
        TextView tv_name = (TextView)contentView.findViewById(R.id.tv_name);
        TextView tv_date = (TextView)contentView.findViewById(R.id.tv_date);
        TextView tv_day = (TextView)contentView.findViewById(R.id.tv_day);
        TextView tv_teacher = (TextView)contentView.findViewById(R.id.tv_teacher);
        TextView tv_address = (TextView)contentView.findViewById(R.id.tv_address);
        TextView tv_grade = (TextView)contentView.findViewById(R.id.tv_grade);
        tv_name.setText(experimentItem.name);
        tv_date.setText("实验日期："+experimentItem.date);
        tv_day.setText("时间段："+experimentItem.day);
        tv_teacher.setText("指导老师："+experimentItem.teacher);
        tv_address.setText("实验地点："+experimentItem.address);
        tv_grade.setText(experimentItem.grade);
        return contentView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}

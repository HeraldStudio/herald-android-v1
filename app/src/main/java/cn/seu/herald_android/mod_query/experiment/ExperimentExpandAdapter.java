package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

class ExperimentExpandAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> parentViews;
    private ArrayList<ArrayList<ExperimentModel>> childViews;
    private Context context;

    public ExperimentExpandAdapter(Context context, ArrayList<String> parentViews, ArrayList<ArrayList<ExperimentModel>> childViews) {
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
        String title = (String) getGroup(groupPosition);
        View contentView = LayoutInflater.from(this.context).inflate(R.layout.mod_que_experiment__item_parent, null);
        TextView view = (TextView) contentView.findViewById(R.id.tv_type_expriment);
        view.setText(title);
        return contentView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View contentView = LayoutInflater.from(this.context).inflate(R.layout.mod_que_experiment__item_child, null);
        ExperimentModel experimentModel = (ExperimentModel) getChild(groupPosition, childPosition);
        TextView tv_name = (TextView) contentView.findViewById(R.id.title);
        TextView tv_date = (TextView) contentView.findViewById(R.id.tv_date);
        TextView tv_day = (TextView) contentView.findViewById(R.id.tv_day);
        TextView tv_teacher = (TextView) contentView.findViewById(R.id.subtitle);
        TextView tv_address = (TextView) contentView.findViewById(R.id.tv_address);
        TextView tv_grade = (TextView) contentView.findViewById(R.id.tv_grade);
        tv_name.setText(experimentModel.name);
        tv_date.setText("实验日期：" + experimentModel.date);
        tv_day.setText("时间段：" + experimentModel.time);
        tv_teacher.setText("指导老师：" + experimentModel.teacher);
        tv_address.setText("实验地点：" + experimentModel.address);
        experimentModel.grade = experimentModel.grade.replace("null", "");
        tv_grade.setText(experimentModel.grade);
        return contentView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}

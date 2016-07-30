package cn.seu.herald_android.app_module.experiment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

class ExperimentExpandAdapter extends BaseExpandableListAdapter {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView tv_name;
        @BindView(R.id.tv_date)
        TextView tv_date;
        @BindView(R.id.tv_day)
        TextView tv_day;
        @BindView(R.id.subtitle)
        TextView tv_teacher;
        @BindView(R.id.tv_address)
        TextView tv_address;
        @BindView(R.id.tv_grade)
        TextView tv_grade;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

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

        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.mod_que_experiment__item_child, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        ExperimentModel experimentModel = (ExperimentModel) getChild(groupPosition, childPosition);

        holder.tv_name.setText(experimentModel.name);
        holder.tv_date.setText("实验日期：" + experimentModel.date);
        holder.tv_day.setText("时间段：" + experimentModel.time);
        holder.tv_teacher.setText("指导老师：" + experimentModel.teacher);
        holder.tv_address.setText("实验地点：" + experimentModel.address);

        holder.tv_grade.setText(experimentModel.grade.replace("null", ""));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}

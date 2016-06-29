package cn.seu.herald_android.mod_query.jwc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_webmodule.WebShowActivity;

class JwcExpandAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> parentViews;
    private ArrayList<ArrayList<JwcItem>> childViews;
    private Context context;

    public JwcExpandAdapter(Context context, ArrayList<String> parentViews, ArrayList<ArrayList<JwcItem>> childViews) {
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
        View contentView = LayoutInflater.from(this.context).inflate(R.layout.expandablelistview_parentitem_jwc, null);
        TextView view = (TextView) contentView.findViewById(R.id.tv_type_notice);
        view.setText(title);
        return contentView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        View contentView = LayoutInflater.from(this.context).inflate(R.layout.expandablelistview_childitem_jwc, null);
        JwcItem jwcItem = (JwcItem) getChild(groupPosition, childPosition);
        TextView tv_name = (TextView) contentView.findViewById(R.id.title);
        TextView tv_date = (TextView) contentView.findViewById(R.id.tv_date);
        tv_name.setText(jwcItem.title);
        tv_date.setText("发布日期：" + jwcItem.date);
        contentView.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse(jwcItem.href);
                WebShowActivity.startWebShowActivity(context,jwcItem.title,uri,R.style.JwcTheme);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return contentView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}

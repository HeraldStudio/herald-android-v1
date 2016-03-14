package cn.seu.herald_android.mod_query.schoolbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

public class SchoolBusFragment extends Fragment {
    //展示校车方向的折叠列表
    ExpandableListView expandableListView;
    //
    String[] titles;
    ArrayList<ArrayList<SchoolBusItem>> childViews;

    public SchoolBusFragment() {

    }

    public static SchoolBusFragment newInstance(String[] titles, ArrayList<ArrayList<SchoolBusItem>> childViews) {
        SchoolBusFragment fragment = new SchoolBusFragment();
        fragment.titles = titles;
        fragment.childViews = childViews;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schoolbus, container, false);
        //控件初始化
        expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        //设定适配器
        SchoolBusExpandAdapter adapter = new SchoolBusExpandAdapter(getContext(), titles, childViews);
        expandableListView.setAdapter(adapter);
        if (adapter.getGroupCount() > 0)
            expandableListView.expandGroup(0);
        return view;
    }
}

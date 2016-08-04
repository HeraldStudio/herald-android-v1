package cn.seu.herald_android.app_module.schoolbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class SchoolBusFragment extends Fragment {
    //
    private String[] titles;
    private ArrayList<ArrayList<SchoolBusModel>> childViews;

    public SchoolBusFragment() {

    }

    public static SchoolBusFragment newInstance(String[] titles, ArrayList<ArrayList<SchoolBusModel>> childViews) {
        SchoolBusFragment fragment = new SchoolBusFragment();
        fragment.titles = titles;
        fragment.childViews = childViews;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mod_que_schoolbus__fragment, container, false);
        // 控件初始化
        ExpandableListView expandableListView = ButterKnife.findById(view, R.id.expandableListView);
        // 设定适配器
        SchoolBusExpandAdapter adapter = new SchoolBusExpandAdapter(getContext(), titles, childViews);
        expandableListView.setAdapter(adapter);
        if (adapter.getGroupCount() > 0)
            expandableListView.expandGroup(0);
        return view;
    }
}

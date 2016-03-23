package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CustomSwipeRefreshLayout;
import cn.seu.herald_android.mod_timeline.TimelineView;

public class CardsFragment extends Fragment {

    private CustomSwipeRefreshLayout srl;

    private TimelineView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_cards, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        srl = (CustomSwipeRefreshLayout) getView().findViewById(R.id.swipe_container);
        view = (TimelineView) getView().findViewById(R.id.timeline);
        srl.setOnRefreshListener(() -> view.loadContent(true));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTimelineView(false);
        view.setSrl(srl);
    }

    // 刷新时间轴和快捷方式
    // refresh 是否联网刷新
    public void loadTimelineView(boolean refresh) {
        if (refresh) srl.setRefreshing(true);
        view.loadContent(refresh);
    }
}

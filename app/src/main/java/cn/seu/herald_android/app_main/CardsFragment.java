package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.swiperefresh.CustomSwipeRefreshLayout;
import cn.seu.herald_android.mod_timeline.TimelineView;

public class CardsFragment extends Fragment {

    private CustomSwipeRefreshLayout srl;

    private TimelineView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_main_cards, container, false);

        srl = (CustomSwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        view = (TimelineView) contentView.findViewById(R.id.timeline);
        srl.setOnRefreshListener(() -> view.loadContent(true));

        return contentView;
    }

    @Override
    public void onResume() {
        // 从模块管理页面返回时,重载卡片和快捷栏
        super.onResume();
        loadTimelineView(false);
        view.setSrl(srl);
    }

    // 刷新时间轴和快捷方式
    // refresh 是否联网刷新
    public void loadTimelineView(boolean refresh) {
        if (refresh && srl != null) srl.setRefreshing(true);
        view.loadContent(refresh);
    }
}

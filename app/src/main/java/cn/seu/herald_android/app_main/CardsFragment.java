package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_timeline.TimelineView;

public class CardsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_cards, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadTimelineView(false);
    }

    // 刷新时间轴和快捷方式
    // refresh 是否联网刷新
    public void loadTimelineView(boolean refresh) {
        SwipeRefreshLayout srl = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_container);
        srl.setColorSchemeResources(R.color.colorPrimary);
        TimelineView view = (TimelineView) getView().findViewById(R.id.timeline);
        srl.setOnRefreshListener(() -> view.loadContent(true));
        view.setHideRefresh(() -> new Handler().postDelayed(() -> srl.setRefreshing(false), 1000));
        if (refresh) srl.setRefreshing(true);
        // 快捷方式刷新在这里
        view.loadContent(refresh);
    }
}

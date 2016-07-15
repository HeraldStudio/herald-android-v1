package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.swiperefresh.CustomSwipeRefreshLayout;

public class CardsFragment extends Fragment {

    private CustomSwipeRefreshLayout srl;

    private CardsListView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.app_main__fragment_cards, container, false);

        srl = (CustomSwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        view = (CardsListView) contentView.findViewById(R.id.timeline);
        view.setSrl(srl);
        srl.setOnRefreshListener(() -> view.loadContent(true));

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (view != null) view.loadContent(false);
    }
}

package cn.seu.herald_android.app_main;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CustomSwipeRefreshLayout;
import cn.seu.herald_android.framework.BaseActivity;

public class CardsFragment extends Fragment {

    @BindView(R.id.swipe_container)
    CustomSwipeRefreshLayout srl;

    @BindView(R.id.timeline)
    CardsListView view;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.app_main__fragment_cards, container, false);
        unbinder = ButterKnife.bind(this, contentView);

        if (getActivity() instanceof BaseActivity) {
            view.activity = (BaseActivity) getActivity();
        }

        srl.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> srl.setRefreshing(false), 500);
            view.loadContent(true);
        });

        new Handler().postDelayed(() -> {
            view.loadContent(true);
        }, 500);

        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        view.loadContent(false);
    }
}

package cn.seu.herald_android.mod_query.emptyroom;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_query.experiment.ExprimentExpandAdapter;

/**
 * Created by heyon on 2016/3/5.
 */
public class EmptyRoomViewPagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> pagerList;
    String[] titles = new String[]{"快捷查询","正常查询"};
    Context context;
    public EmptyRoomViewPagerAdapter(FragmentManager fm,Context context) {
        super(fm);
        this.context = context;
        pagerList = new ArrayList<>();
        FastQueryFragment fastQueryFragment = new FastQueryFragment();
        QueryFragment queryFragment = new QueryFragment();
        pagerList.add(fastQueryFragment);
        pagerList.add(queryFragment);
    }

    @Override
    public Fragment getItem(int position) {
        return pagerList.get(position);
    }

    @Override
    public int getCount() {
        return pagerList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}

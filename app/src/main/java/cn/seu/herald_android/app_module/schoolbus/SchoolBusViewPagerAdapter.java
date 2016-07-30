package cn.seu.herald_android.app_module.schoolbus;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

class SchoolBusViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragmentArrayList;
    private ArrayList<String> titleArrayList;

    public SchoolBusViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentArrayList = new ArrayList<>();
        titleArrayList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentArrayList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentArrayList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleArrayList.get(position);
    }

    public void add(Fragment fragment, String title) {
        fragmentArrayList.add(fragment);
        titleArrayList.add(title);
    }

    public void removeAll(){
        titleArrayList.clear();
        fragmentArrayList.clear();
    }
}

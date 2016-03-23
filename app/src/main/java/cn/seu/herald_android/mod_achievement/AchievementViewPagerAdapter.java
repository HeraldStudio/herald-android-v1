package cn.seu.herald_android.mod_achievement;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

public class AchievementViewPagerAdapter extends PagerAdapter {
    private ArrayList<Achievement> achievementArrayList;
    private Context context;

    public AchievementViewPagerAdapter(Context context, ArrayList<Achievement> achievementArrayList) {
        this.achievementArrayList = achievementArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return achievementArrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Achievement achievement = achievementArrayList.get(position);
        View view = LayoutInflater.from(this.context).inflate(R.layout.viewpager_achievement_experiment, null);
        //设置成就名字
        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_name.setText(achievement.getName());
        //成就描述
        TextView tv_des = (TextView) view.findViewById(R.id.tv_des);
        tv_des.setText(achievement.getDes());
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}

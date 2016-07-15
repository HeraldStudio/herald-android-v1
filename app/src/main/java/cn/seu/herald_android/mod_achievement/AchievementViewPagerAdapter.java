package cn.seu.herald_android.mod_achievement;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_query.experiment.AchievementModel;

public class AchievementViewPagerAdapter extends PagerAdapter {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.tv_des)
        TextView desc;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    private ArrayList<AchievementModel> achievements;
    private Context context;

    public AchievementViewPagerAdapter(Context context, ArrayList<AchievementModel> achievements) {
        this.achievements = achievements;
        this.context = context;
    }

    @Override
    public int getCount() {
        return achievements.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        AchievementModel achievementModel = achievements.get(position);
        View view = LayoutInflater.from(this.context)
                .inflate(R.layout.mod_que_experiment__view_pager_achievement, null);

        ViewHolder holder = new ViewHolder(view);

        //设置成就名字
        holder.title.setText(achievementModel.getName());
        //成就描述
        holder.desc.setText(achievementModel.getDes());
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}

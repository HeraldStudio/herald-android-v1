package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ShortcutBoxView;
import cn.seu.herald_android.mod_query.curriculum.CurriculumActivity;

public class TimelineView extends ListView {

    public static class Item {
        private int module;
        private long time;
        private String info;
        public ArrayList<View> attachedView = new ArrayList<>();

        public Item(int module, long time, String info) {
            this.module = module;
            this.time = time;
            this.info = info;
        }

        public static Comparator<Item> comparator =
                (item1, item2) -> (int)(item2.time - item1.time);
    }

    private ArrayList<Item> itemList;

    private BaseAppCompatActivity activity;

    public void setActivity(BaseAppCompatActivity activity) {
        this.activity = activity;
    }

    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Runnable hideRefresh = null;

    public void setHideRefresh(Runnable hideRefresh) {
        this.hideRefresh = hideRefresh;
    }

    private ShortcutBoxView shortcutBox;

    private TimelineAdapter adapter;

    private final Vector threads = new Vector();

    public void loadContent(boolean refresh) {

        itemList = new ArrayList<>();

        if (refresh) {
            threads.add(new Object());
            CurriculumActivity.remoteRefreshCache(getContext(), () -> {
                if(threads.size() > 0) threads.remove(0);
                loadContent(false);
            });
        }

        // 加载并解析课表数据
        String cache = activity.getCacheHelper().getCache("herald_curriculum");
        itemList.add(TimelineParser.getCurriculumItem(getContext(), cache));

        // 加载并解析实验数据
        //cache = activity.getCacheHelper().getCache("herald_experiment");
        //itemList.add(TimelineParser.getExperimentItem(getContext(), cache));

        Collections.sort(itemList, Item.comparator);
        if(adapter == null) {
            setAdapter(adapter = new TimelineAdapter());
        } else {
            adapter.notifyDataSetChanged();
        }
        if(hideRefresh != null && threads.size() == 0)
            hideRefresh.run();
    }

    public void refreshShortcut(){
        if(shortcutBox == null) {
            shortcutBox = (ShortcutBoxView)
                    LayoutInflater.from(getContext()).inflate(R.layout.timeline_shortcut_box, null);
            addHeaderView(shortcutBox);
        } else {
            shortcutBox.refresh();
        }
    }

    public class TimelineAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Item getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);

            View v = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, null);
            TextView name = (TextView)v.findViewById(R.id.name);
            TextView time = (TextView)v.findViewById(R.id.time);
            TextView content = (TextView)v.findViewById(R.id.content);
            ImageView avatar = (ImageView)v.findViewById(R.id.avatar);
            HorizontalScrollView hsv = (HorizontalScrollView)v.findViewById(R.id.hsv);
            ViewGroup attachedContainer = (ViewGroup)v.findViewById(R.id.attachedContainer);

            name.setText(activity.getSettingsHelper().moduleNamesTips[item.module]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.time);
            String dateTime = timeInNaturalLanguage(calendar);
            time.setText(dateTime);

            content.setText(item.info);
            avatar.setImageDrawable(getResources()
                    .getDrawable(activity.getSettingsHelper().moduleIconsId[item.module]));
            v.setOnClickListener((v1) -> {
                activity.startActivity(new Intent(activity.getSettingsHelper().moduleActions[item.module]));
            });


            if(item.attachedView.size() != 0){
                hsv.setVisibility(VISIBLE);
                for(View k : item.attachedView) {
                    if (k.getParent() != null) {
                        ((ViewGroup) k.getParent()).removeView(k);
                    }
                    attachedContainer.addView(k);
                }
            }

            return v;
        }
    }

    public static String timeInNaturalLanguage(Calendar calendar) {
        long time = calendar.getTimeInMillis();
        long now = Calendar.getInstance().getTimeInMillis();
        Calendar todayCal = Calendar.getInstance();
        todayCal = CalendarUtils.toSharpDay(todayCal);
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() + 1000 * 60 * 60 * 24);
        long tomorrow = todayCal.getTimeInMillis();
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() + 1000 * 60 * 60 * 24);
        long dayAfterTomorrow = todayCal.getTimeInMillis();
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() - 2 * 1000 * 60 * 60 * 24);
        todayCal.set(Calendar.YEAR, todayCal.get(Calendar.YEAR)+1);
        todayCal.set(Calendar.MONTH, 0);
        todayCal.set(Calendar.DATE, 1);
        long nextYear = todayCal.getTimeInMillis();

        // 允许1秒的误差
        if(now <= time + 1000) {
            if(now > time) time = now;
            // 对明天全天的事件单独处理
            if(time == tomorrow){
                return "明天";
            }
            if(time < tomorrow){
                int deltaMinute = (int)((time - now) / 1000 / 60);
                int deltaHour = deltaMinute / 60;
                deltaMinute %= 60;
                if(deltaHour != 0) return deltaHour + "小时后";
                if(deltaMinute != 0) return deltaMinute + "分钟后";
                return "现在";
            }
            if(time <= dayAfterTomorrow){
                return "明天 " + new SimpleDateFormat("H:mm").format(calendar.getTime());
            }
            if(time <= nextYear){
                return new SimpleDateFormat("M-d H:mm").format(calendar.getTime());
            }
        }
        return new SimpleDateFormat("yyyy-M-d H:mm").format(calendar.getTime());
    }
}

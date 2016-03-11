package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_query.cardextra.CardActivity;
import cn.seu.herald_android.mod_query.cardextra.CardAdapter;
import cn.seu.herald_android.mod_query.cardextra.CardItem;

public class TimelineView extends ListView {

    public static class Item {
        private int module;
        private long time;
        private String info;
        public View attachedView;

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

    public void loadContent(boolean refresh) {
        itemList = new ArrayList<>();
        if (refresh) {
            //TODO add refresh method
            CardActivity.remoteRefreshCache(getContext());
        }
        loadContentCard();

        loadContentCurriculum();

        Collections.sort(itemList, Item.comparator);
        setAdapter(new TimelineAdapter());
        if(hideRefresh != null)
            hideRefresh.run();
    }

    private void loadContentCard() {

        //尝试加载缓存
        String cache = activity.getCacheHelper().getCache("herald_card");
        if (cache.equals("")) return;

        try {
            JSONObject json_cache = new JSONObject(cache);
            JSONArray detail = json_cache.getJSONObject("content").getJSONArray("detial");
            itemList = TimelineParser.parseCardAndAddToList(detail, itemList);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void loadContentCurriculum() {

        //尝试加载缓存
        String cache = activity.getCacheHelper().getCache("herald_curriculum");
        if (cache.equals("")) return;

        try {
            JSONObject json_cache = new JSONObject(cache);
            itemList = TimelineParser.parseCurriculumAndAddToList(getContext(), json_cache, itemList);
        } catch (JSONException e){
            e.printStackTrace();
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
            ViewGroup attachedContainer = (ViewGroup)v.findViewById(R.id.attachedContainer);

            name.setText(activity.getSettingsHelper().moduleNamesTips[item.module]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.time);
            String dateTime = timeInNaturalLanguage(calendar);
            time.setText(dateTime);

            content.setText(item.info);
            avatar.setImageDrawable(getResources()
                    .getDrawable(activity.getSettingsHelper().moduleIconsId[item.module]));
            avatar.setOnClickListener((v1) -> {
                activity.startActivity(new Intent(activity.getSettingsHelper().moduleActions[item.module]));
            });
            name.setOnClickListener((v1) -> {
                activity.startActivity(new Intent(activity.getSettingsHelper().moduleActions[item.module]));
            });


            if(item.attachedView != null){
                attachedContainer.setVisibility(VISIBLE);
                if(item.attachedView.getParent() != null){
                    ((ViewGroup) item.attachedView.getParent()).removeView(item.attachedView);
                }
                attachedContainer.addView(item.attachedView);
            }

            return v;
        }
    }

    public static String timeInNaturalLanguage(Calendar calendar) {
        long time = calendar.getTimeInMillis();
        long now = Calendar.getInstance().getTimeInMillis();
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        long today = todayCal.getTimeInMillis();
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() - 1000 * 60 * 60 * 24);
        long yesterday = todayCal.getTimeInMillis();
        todayCal.setTimeInMillis(todayCal.getTimeInMillis() + 1000 * 60 * 60 * 24);
        todayCal.set(Calendar.MONTH, 0);
        todayCal.set(Calendar.DATE, 1);
        long thisYear = todayCal.getTimeInMillis();

        if(now > time) {
            if(time >= today){
                int deltaMinute = (int)((now - time) / 1000 / 60);
                int deltaHour = deltaMinute / 60;
                deltaMinute %= 60;
                if(deltaHour != 0) return deltaHour + "小时前";
                if(deltaMinute != 0) return deltaMinute + "分钟前";
                return "刚刚";
            }
            if(time >= yesterday){
                return "昨天 " + new SimpleDateFormat("H:mm").format(calendar.getTime());
            }
            if(time >= thisYear){
                return new SimpleDateFormat("M-d H:mm").format(calendar.getTime());
            }
        }
        return new SimpleDateFormat("yyyy-M-d H:mm").format(calendar.getTime());
    }
}

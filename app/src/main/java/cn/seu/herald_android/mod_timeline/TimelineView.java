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
import java.util.List;

import cn.seu.herald_android.BaseAppCompatActivity;
import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_query.cardextra.CardAdapter;
import cn.seu.herald_android.mod_query.cardextra.CardItem;

public class TimelineView extends ListView {

    public static class Item {
        private int module;
        private long time;
        private String info;

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

    public void loadContent(boolean refresh) {
        itemList = new ArrayList<>();
        if (refresh) {
            //TODO add refresh method
        }
        loadContentCard();

        loadContentCurriculum();

        Collections.sort(itemList, Item.comparator);
        setAdapter(new TimelineAdapter());
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
            itemList = TimelineParser.parseCurriculumAndAddToList(json_cache, itemList);
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
            View v = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, null);
            TextView name = (TextView)v.findViewById(R.id.name);
            TextView time = (TextView)v.findViewById(R.id.time);
            TextView content = (TextView)v.findViewById(R.id.content);
            ImageView avatar = (ImageView)v.findViewById(R.id.avatar);

            name.setText(activity.getSettingsHelper().moduleNamesTips[getItem(position).module]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getItem(position).time);
            String dateTime = new SimpleDateFormat("yyyy年M月d日 H:mm").format(calendar.getTime());
            time.setText(dateTime);

            content.setText(getItem(position).info);
            avatar.setImageDrawable(getResources()
                    .getDrawable(activity.getSettingsHelper().moduleIconsId[getItem(position).module]));
            avatar.setOnClickListener((v1) -> {
                activity.startActivity(new Intent(activity.getSettingsHelper().moduleActions[getItem(position).module]));
            });
            name.setOnClickListener((v1) -> {
                activity.startActivity(new Intent(activity.getSettingsHelper().moduleActions[getItem(position).module]));
            });

            return v;
        }
    }
}

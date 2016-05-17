package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineItem;
import cn.seu.herald_android.mod_timeline.TimelineView;

/**
 * Created by heyon on 2016/5/10.
 */
public class AfterSchoolActivityBlockLayout extends LinearLayout {
    String description;
    AfterSchoolActivityItem item;

    public AfterSchoolActivityBlockLayout(Context context, AfterSchoolActivityItem item) {
        super(context);
        this.item = item;
        View contentView = LayoutInflater.from(context).inflate(R.layout.timeline_item_row, null);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        //TextView subtitle = (TextView) contentView.findViewById(R.id.subtitle);
        TextView content = (TextView) contentView.findViewById(R.id.content);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAfterSchoolPrimary));
        title.setText(item.title);
        title.setEllipsize(TextUtils.TruncateAt.END);


        content.setText(item.activity_time + " @ " + item.location);

        //子标题为活动是否开始
        //subtitle.setText(item.getTag());

        addView(contentView);

        description = item.title + "|"
                + item.getTag() + "|"
                + item.introduciton + "|";
    }

    @Override
    public String toString() {
        return description;
    }


    /**
     * 读取热门活动缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getAfterSchoolActivityItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_afterschoolschool_hot");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            List<AfterSchoolActivityItem> afterSchoolActivityItems = AfterSchoolActivityItem.transfromJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            if (afterSchoolActivityItems.size() == 0) {
                return new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                        now, TimelineItem.NO_CONTENT, "最近没有热门活动");
            } else {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                        now, TimelineItem.CONTENT_NOTIFY, "最近有" + afterSchoolActivityItems.size() + "个热门活动");
                for (AfterSchoolActivityItem afterSchoolActivityItem : afterSchoolActivityItems) {
                    item.attachedView.add(new AfterSchoolActivityBlockLayout(host.getContext(), afterSchoolActivityItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            new CacheHelper(host.getContext()).setCache("herald_afterschoolschool_hot", "");
            return new TimelineItem(SettingsHelper.MODULE_LIVE_ACTIVITY,
                    now, TimelineItem.NO_CONTENT, "热门活动数据加载失败，请手动刷新"
            );
        }
    }
}

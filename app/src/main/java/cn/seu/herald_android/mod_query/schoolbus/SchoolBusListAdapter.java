package cn.seu.herald_android.mod_query.schoolbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/2/28.
 */
public class SchoolBusListAdapter extends ArrayAdapter<SchoolBusItem> {
    private int resourceID;
    public SchoolBusListAdapter(Context context, int resource, List<SchoolBusItem> objects) {
        super(context, resource, objects);
        this.resourceID = resource;
    }
    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        final SchoolBusItem schoolBusItem = getItem(position);
        final View view = LayoutInflater.from(getContext()).inflate(resourceID, null);//为子项加载布局
        TextView tv_period = (TextView)view.findViewById(R.id.tv_schoolbusitem_period);
        TextView tv_time = (TextView)view.findViewById(R.id.tv_schoolbusitem_time);
        //判断是否是当前时间的区间
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try{
            String time = schoolBusItem.getPeriod();
            Date dateStart = new Date();
            Date dateEnd = new Date();
            dateStart.setHours(Integer.parseInt(time.split(":")[0]));
            dateStart.setMinutes(Integer.parseInt(time.split(":")[1].split("-")[0]));
            dateEnd.setHours(Integer.parseInt(time.split(":")[1].split("-")[1]));
            dateEnd.setMinutes(Integer.parseInt(time.split(":")[2]));
            Date now = new Date();
            if(now.after(dateStart)&&now.before(dateEnd)) {
                //如果是当前时间所处于的时间区间，则设置颜色
                tv_period.setTextColor(tv_period.getResources().getColor(R.color.relaxBlue));
                tv_time.setTextColor(tv_time.getResources().getColor(R.color.relaxBlue));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        tv_period.setText(schoolBusItem.getPeriod());
        tv_time.setText(schoolBusItem.getTime());
        return view;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        //根据listview的item数目设置宽度
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}

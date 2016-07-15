package cn.seu.herald_android.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import cn.seu.herald_android.R;

public class ListViewUtils {
    public static void setHeightWithContent(ListView listView){
        //设置高度自适应
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ((ViewGroup.MarginLayoutParams)params).setMargins(10, 10, 10, 10);
        listView.setLayoutParams(params);
    }

    public static void addDefaultEmptyTipsView(Context context,ListView listView,String tipString){
        View view = LayoutInflater.from(context).inflate(R.layout.custom__view_empty_tip,null);
        view.setPadding(0,0,0,0);
        ImageView imgTip = (ImageView)view.findViewById(R.id.img_emptytip);
        TextView textView = (TextView)view.findViewById(R.id.tv_emptytip);
        Picasso.with(context).load(R.drawable.no_content).fit().into(imgTip);
        textView.setText(tipString);
        listView.addHeaderView(view);
    }
}

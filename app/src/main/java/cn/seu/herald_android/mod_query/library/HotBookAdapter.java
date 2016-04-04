package cn.seu.herald_android.mod_query.library;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/3/15.
 */
class HotBookAdapter extends ArrayAdapter<HotBook> {
    public HotBookAdapter(Context context, int resource, List<HotBook> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HotBook hotBook = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_library_hotbook,null);
        }
        TextView tv_count = (TextView)convertView.findViewById(R.id.tv_count);
        TextView tv_place = (TextView)convertView.findViewById(R.id.tv_place);
        TextView tv_name = (TextView)convertView.findViewById(R.id.tv_name);
        TextView tv_author = (TextView)convertView.findViewById(R.id.tv_author);

        tv_count.setText(hotBook.getCount()+"");
        tv_place.setText(hotBook.getPlace());
        tv_author.setText(hotBook.getAuthor());
        tv_name.setText(hotBook.getName());

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LibrarySearchActivity.class);
            intent.putExtra("q", hotBook.getName().split("\\.")[0]);
            getContext().startActivity(intent);
        });

        return convertView;
    }

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
}

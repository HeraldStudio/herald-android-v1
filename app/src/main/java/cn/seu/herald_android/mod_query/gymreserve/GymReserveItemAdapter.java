package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/5/13.
 */
public class GymReserveItemAdapter extends ArrayAdapter<GymReserveItem>{
    int resource;
    public GymReserveItemAdapter(Context context, int resource, List<GymReserveItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(resource,null);
        GymReserveItem item = getItem(position);
        TextView tv_name = (TextView)convertView.findViewById(R.id.tv_itemname);
        tv_name.setText(item.name);
        return convertView;
    }




}

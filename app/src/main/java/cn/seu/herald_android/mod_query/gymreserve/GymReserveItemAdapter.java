package cn.seu.herald_android.mod_query.gymreserve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.seu.herald_android.R;

/**
 * Created by heyon on 2016/5/13.
 */
public class GymReserveItemAdapter extends ArrayAdapter<GymReserveItem>{
    int resource;
    HashMap<String,Integer> ic_maps;
    public GymReserveItemAdapter(Context context, int resource, List<GymReserveItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
        ic_maps = new HashMap<>();
        ic_maps.put("篮球",R.drawable.ic_sport_basketball);
        ic_maps.put("乒乓球",R.drawable.ic_sport_tabletennis);
        ic_maps.put("排球",R.drawable.ic_sport_volleyball);
        ic_maps.put("健身",R.drawable.ic_sport_fitness);
        ic_maps.put("跆拳道",R.drawable.ic_sport_dao);
        ic_maps.put("武术",R.drawable.ic_sport_kungfu);
        ic_maps.put("舞蹈",R.drawable.ic_sport_dance);
        ic_maps.put("羽毛球",R.drawable.ic_sport_adminton);



    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(resource,null);
        GymReserveItem item = getItem(position);
        TextView tv_name = (TextView)convertView.findViewById(R.id.tv_itemname);

        ImageView img_sport = (ImageView)convertView.findViewById(R.id.img_sportitem);
        //根据运动名设置图标
        Picasso.with(getContext()).load(ic_maps.get(item.name)).into(img_sport);

        tv_name.setText(item.name);
        return convertView;
    }




}

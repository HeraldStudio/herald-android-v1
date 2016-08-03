package cn.seu.herald_android.app_module.gymreserve;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class GymReserveSportAdapter extends BaseAdapter {
    Context context;
    ArrayList<GymSportModel> list;

    public GymReserveSportAdapter(Context context, ArrayList<GymSportModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mod_que_gymreserve__item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        GymSportModel item = list.get(position);

        holder.tv_name.setText(item.name);

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_itemname)
        TextView tv_name;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

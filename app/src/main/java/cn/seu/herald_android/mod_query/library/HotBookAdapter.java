package cn.seu.herald_android.mod_query.library;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;

class HotBookAdapter extends ArrayAdapter<HotBookModel> {
    public HotBookAdapter(Context context, int resource, List<HotBookModel> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HotBookModel hotBookModel = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_library__item,null);
        }
        TextView tv_count = (TextView)convertView.findViewById(R.id.tv_count);
        TextView tv_place = (TextView)convertView.findViewById(R.id.tv_place);
        TextView tv_name = (TextView) convertView.findViewById(R.id.title);
        TextView tv_author = (TextView)convertView.findViewById(R.id.tv_author);

        tv_count.setText(hotBookModel.getCount() + "");
        tv_place.setText(hotBookModel.getPlace());
        tv_author.setText(hotBookModel.getAuthor());
        tv_name.setText(hotBookModel.getName());

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LibrarySearchActivity.class);
            intent.putExtra("q", hotBookModel.getName().split("\\.")[0]);
            getContext().startActivity(intent);
        });

        return convertView;
    }

}

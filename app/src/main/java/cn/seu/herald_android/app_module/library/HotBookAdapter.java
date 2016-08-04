package cn.seu.herald_android.app_module.library;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.AppContext;

class HotBookAdapter extends ArrayAdapter<HotBookModel> {

    static class ViewHolder {
        @BindView(R.id.tv_count)
        TextView tv_count;
        @BindView(R.id.tv_place)
        TextView tv_place;
        @BindView(R.id.title)
        TextView tv_name;
        @BindView(R.id.tv_author)
        TextView tv_author;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public HotBookAdapter(Context context, int resource, List<HotBookModel> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HotBookModel hotBookModel = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_library__item,null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.tv_count.setText(hotBookModel.getCount() + "");
        holder.tv_place.setText(hotBookModel.getPlace());
        holder.tv_author.setText(hotBookModel.getAuthor());
        holder.tv_name.setText(hotBookModel.getName());

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LibrarySearchActivity.class);
            intent.putExtra("q", hotBookModel.getName().split("\\.")[0]);
            AppContext.startActivitySafely(intent);
        });

        return convertView;
    }
}

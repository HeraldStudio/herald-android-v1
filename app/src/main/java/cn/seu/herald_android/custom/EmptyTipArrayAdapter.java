package cn.seu.herald_android.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import cn.seu.herald_android.R;

public abstract class EmptyTipArrayAdapter<T> extends ArrayAdapter<T> {

    public EmptyTipArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    @Override
    public final int getCount() {
        return Math.max(1, super.getCount());
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (super.getCount() == 0) {
            return LayoutInflater.from(getContext()).inflate(R.layout.custom__view_empty_tip, null);
        }

        return getView(position, convertView);
    }

    protected abstract View getView(int position, View convertView);
}

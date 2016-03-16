package cn.seu.herald_android.mod_query.srtp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.seu.herald_android.R;

public class SrtpAdapter extends RecyclerView.Adapter<SrtpAdapter.SrtpHolder> {
    private Context context;
    private ArrayList<SrtpItem> list;

    public SrtpAdapter(Context context, ArrayList<SrtpItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public SrtpHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.recyclerviewitem_srtp, null);
        return new SrtpHolder(view);
    }

    @Override
    public void onBindViewHolder(SrtpHolder holder, int position) {
        SrtpItem srtpItem = list.get(position);
        holder.tv_date.setText(srtpItem.getDate());
        holder.tv_credit.setText(srtpItem.getCredit());
        holder.tv_project.setText(srtpItem.getProject());
        if (srtpItem.getDepartment().equals("")) {
            holder.tv_department.setVisibility(View.GONE);
        } else {
            holder.tv_department.setVisibility(View.VISIBLE);
            holder.tv_department.setText("项目所属:" + srtpItem.getDepartment());
        }
        if (srtpItem.getTotalCredit().equals("")) {
            holder.tv_totalCredit.setVisibility(View.GONE);
        } else {
            holder.tv_totalCredit.setVisibility(View.VISIBLE);
            holder.tv_totalCredit.setText("总学分及工作比例:" + String.format("%s (%s)", srtpItem.getTotalCredit(), srtpItem.getProportion()));
        }
        holder.tv_type.setText("项目类型:" + srtpItem.getType());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class SrtpHolder extends RecyclerView.ViewHolder {
        TextView tv_date;
        TextView tv_credit;
        TextView tv_project;
        TextView tv_department;
        TextView tv_type;
        TextView tv_totalCredit;

        public SrtpHolder(View itemView) {
            super(itemView);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_credit = (TextView) itemView.findViewById(R.id.tv_credit);
            tv_project = (TextView) itemView.findViewById(R.id.tv_project);
            tv_department = (TextView) itemView.findViewById(R.id.tv_department);
            tv_type = (TextView) itemView.findViewById(R.id.tv_type);
            tv_totalCredit = (TextView) itemView.findViewById(R.id.tv_totalcredit);
        }
    }

}
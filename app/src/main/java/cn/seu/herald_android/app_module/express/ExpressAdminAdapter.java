package cn.seu.herald_android.app_module.express;

import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import cn.seu.herald_android.R;


/**
 * Created by corvo on 8/24/16.
 */
public class ExpressAdminAdapter extends RecyclerView.Adapter<ExpressAdminAdapter.ExpressAdminViewHolder>{


    private List<ExpressInfo> infoList;
    private ExpressAdminActivity.OnRefresh refresh;


    public ExpressAdminAdapter(List<ExpressInfo> infoList, ExpressAdminActivity.OnRefresh refresh) {
        this.infoList = infoList;
        this.refresh = refresh;
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    @Override
    public ExpressAdminViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mod_que_express__admin_item, null);

        return new ExpressAdminViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ExpressAdminViewHolder holder, int position) {
        ExpressInfo info = infoList.get(position);
        holder.detail.setText(info.getUsername() + "|" +  info.getUserphone());
        holder.sms.setText(info.getSmsInfo());
        holder.feature.setText(info.getWeight() + "|" + info.getArrival() + "|" + info.getLocate());
        holder.receive.setChecked(info.isReceived());
        holder.receive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                info.setReceived(isChecked);
            }
        });
        holder.finish.setChecked(info.isFetched());
        holder.finish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                info.setFetched(isChecked);
            }
        });
    }

    class ExpressAdminViewHolder extends RecyclerView.ViewHolder {
        public TextView detail;
        public TextView sms;
        public TextView feature;
        public CheckBox receive;
        public CheckBox finish;

        public ExpressAdminViewHolder(View v) {
            super(v);
            detail = (TextView) v.findViewById(R.id.express_admin_txt_detail);
            sms = (TextView) v.findViewById(R.id.express_admin_txt_sms);
            feature = (TextView) v.findViewById(R.id.express_admin_txt_feature);

            receive = (CheckBox) v.findViewById(R.id.express_admin_check_receive);
            finish = (CheckBox) v.findViewById(R.id.express_admin_check_finish);
        }
    }
}

package cn.seu.herald_android.app_module.express;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.OnItemClick;
import cn.seu.herald_android.R;
import static cn.seu.herald_android.helper.LogUtils.makeLogTag;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressHistoryAdapter extends RecyclerView.Adapter<ExpressHistoryViewHolder>{
    private static String TAG = makeLogTag(ExpressHistoryAdapter.class);

    private List<ExpressInfo> expressInfoList;

    public ExpressHistoryAdapter(List<ExpressInfo> infoList) {
        this.expressInfoList = infoList;
    }

    @Override
    public int getItemCount() {
        return expressInfoList.size();
    }

    @Override
    public ExpressHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.mod_que_express__history_item, parent, false);

        return new ExpressHistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ExpressHistoryViewHolder holder, int position) {
        ExpressInfo info = expressInfoList.get(position);

        holder.arrivalRecord.setText(info.getArrival());
        holder.locateRecord.setText(info.getLocate());
        holder.smsRecord.setText(info.getSmsInfo());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.submitRecord.setText(format.format(info.getSubmitTime()));
        holder.isFetchedRecord.setText(info.isFetched()? "已取":"未取");

        if (info.isFetched()) {
            holder.containerRecord.setClickable(false);
        } else {    // 若快递未取
            holder.containerRecord.setClickable(true);
            holder.containerRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Click item " + String.valueOf(position));

                }
            });
        }
    }


    public void deleteItem(int position) {

    }

}

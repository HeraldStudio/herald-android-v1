package cn.seu.herald_android.mod_query.library;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiRequest;

/**
 * 这个类里面错放了太多的 model 层逻辑, 应尽快移走
 */
class BorrowBookAdapter extends ArrayAdapter<BorrowBookModel> {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView tv_title;
        @BindView(R.id.tv_author)
        TextView tv_author;
        @BindView(R.id.tv_renderdate)
        TextView tv_renderdate;
        @BindView(R.id.tv_duedate)
        TextView tv_duedate;
        @BindView(R.id.tv_renewtime)
        TextView tv_renewtime;
        @BindView(R.id.btn_lib_renew)
        Button btn_renew;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public BorrowBookAdapter(Context context, int resource, List<BorrowBookModel> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BorrowBookModel borrowBookModel = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_library__dialog_borrow_record__item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.tv_title.setText(borrowBookModel.title);
        holder.tv_author.setText(borrowBookModel.author);
        holder.tv_renderdate.setText("借阅时间：" + borrowBookModel.renderDate);
        holder.tv_duedate.setText("应还时间：" + borrowBookModel.dueDate
                + (isOverdue(borrowBookModel.dueDate) ? "（已到期）" : ""));
        holder.tv_renewtime.setText("续借次数：" + borrowBookModel.renewTime);

        if (isDue(borrowBookModel.dueDate)) {
            //如果已经接近归还日期则标红日期
            holder.tv_duedate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorLectureprimary));
        } else {
            holder.tv_duedate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        }

        holder.btn_renew.setEnabled(borrowBookModel.renewTime.equals("0") && !isOverdue(borrowBookModel.dueDate));
        if (holder.btn_renew.isEnabled()) {
            holder.btn_renew.setTextColor(ContextCompat.getColor(getContext(), R.color.colorLibraryprimary));
            holder.btn_renew.setText("续借");
        } else {
            holder.btn_renew.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
            holder.btn_renew.setText("已超期");
        }

        holder.btn_renew.setOnClickListener(!holder.btn_renew.isEnabled() ? null : v -> {
            AppContext.showMessage("正在请求续借，请稍候…");

            new ApiRequest().api("renew").addUUID()
                    .post("barcode", borrowBookModel.barcode)
                    .onFinish((success, code, response) -> {
                        try {
                            response = new JSONObject(response).getString("content");
                            AppContext.showMessage(
                                    response.equals("success") ? "续借成功" : response);
                        } catch (JSONException e) {
                            AppContext.showMessage("续借失败");
                        }
                    }).run();
        });

        return convertView;
    }

    private boolean isDue(String due_date) {
        int due_year = Integer.parseInt(due_date.split("-")[0]);
        int due_month = Integer.parseInt(due_date.split("-")[1]);
        int due_day = Integer.parseInt(due_date.split("-")[2]);

        //如果距离归还时间只差3天，则返回真
        Calendar today = CalendarUtils.toSharpDay(Calendar.getInstance());
        Calendar dueday = CalendarUtils.toSharpDay(Calendar.getInstance());
        dueday.set(Calendar.YEAR, due_year);
        dueday.set(Calendar.MONTH, due_month);
        dueday.set(Calendar.DAY_OF_MONTH, due_day);

        return (dueday.getTimeInMillis() - today.getTimeInMillis()) <= CalendarUtils.ONE_DAY * 3;
    }

    private boolean isOverdue(String due_date) {
        int due_year = Integer.parseInt(due_date.split("-")[0]);
        int due_month = Integer.parseInt(due_date.split("-")[1]);
        int due_day = Integer.parseInt(due_date.split("-")[2]);

        Calendar today = CalendarUtils.toSharpDay(Calendar.getInstance());
        Calendar dueday = CalendarUtils.toSharpDay(Calendar.getInstance());
        dueday.set(Calendar.YEAR, due_year);
        dueday.set(Calendar.MONTH, due_month);
        dueday.set(Calendar.DAY_OF_MONTH, due_day);

        return dueday.getTimeInMillis() < today.getTimeInMillis();
    }
}

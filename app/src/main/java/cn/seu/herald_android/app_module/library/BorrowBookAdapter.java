package cn.seu.herald_android.app_module.library;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.EmptyTipArrayAdapter;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;

/**
 * 这个类里面错放了太多的 model 层逻辑, 应尽快移走
 */
class BorrowBookAdapter extends EmptyTipArrayAdapter<BorrowBookModel> {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView tv_title;
        @BindView(R.id.tv_author)
        TextView tv_author;
        @BindView(R.id.tv_renderdate)
        TextView tv_render_date;
        @BindView(R.id.tv_duedate)
        TextView tv_due_date;
        @BindView(R.id.tv_renewtime)
        TextView tv_renew_time;
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
    public View getView(int position, View convertView) {
        BorrowBookModel borrowBookModel = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_library__dialog_borrow_record__item, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.tv_title.setText(borrowBookModel.title);
        holder.tv_author.setText(borrowBookModel.author);
        holder.tv_render_date.setText("借阅时间：" + borrowBookModel.renderDate);
        holder.tv_due_date.setText("应还时间：" + borrowBookModel.dueDate
                + (isOverdue(borrowBookModel.dueDate) ? "（已到期）" : ""));
        holder.tv_renew_time.setText("续借次数：" + borrowBookModel.renewTime);

        if (isDue(borrowBookModel.dueDate)) {
            // 如果已经接近归还日期则标红日期
            holder.tv_due_date.setTextColor(ContextCompat.getColor(getContext(), R.color.relaxRed));
        } else {
            holder.tv_due_date.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        }

        holder.btn_renew.setEnabled(borrowBookModel.renewTime.equals("0") && !isOverdue(borrowBookModel.dueDate));
        if (holder.btn_renew.isEnabled()) {
            holder.btn_renew.setTextColor(ContextCompat.getColor(getContext(), R.color.relaxRed));
            holder.btn_renew.setText("续借");
        } else {
            holder.btn_renew.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
            holder.btn_renew.setText("已超期");
        }

        holder.btn_renew.setOnClickListener(!holder.btn_renew.isEnabled() ? null : v -> {
            AppContext.showMessage("正在请求续借，请稍候…");

            new ApiSimpleRequest(Method.POST).api("renew").addUuid()
                    .post("barcode", borrowBookModel.barcode)
                    .onResponse((success, code, response) -> {
                        response = new JObj(response).$s("content");
                        AppContext.showMessage(
                                response.equals("success") ? "续借成功" : response);
                    }).run();
        });

        return convertView;
    }

    private boolean isDue(String due_date) {
        int due_year = Integer.parseInt(due_date.split("-")[0]);
        int due_month = Integer.parseInt(due_date.split("-")[1]);
        int due_day = Integer.parseInt(due_date.split("-")[2]);

        // 如果距离归还时间只差3天，则返回真
        Calendar today = CalendarUtils.toSharpDay(Calendar.getInstance());
        Calendar dueDate = CalendarUtils.toSharpDay(Calendar.getInstance());
        dueDate.set(Calendar.YEAR, due_year);
        dueDate.set(Calendar.MONTH, due_month);
        dueDate.set(Calendar.DAY_OF_MONTH, due_day);

        return (dueDate.getTimeInMillis() - today.getTimeInMillis()) <= CalendarUtils.ONE_DAY * 3;
    }

    private boolean isOverdue(String due_date) {
        int due_year = Integer.parseInt(due_date.split("-")[0]);
        int due_month = Integer.parseInt(due_date.split("-")[1]);
        int due_day = Integer.parseInt(due_date.split("-")[2]);

        Calendar today = CalendarUtils.toSharpDay(Calendar.getInstance());
        Calendar dueDate = CalendarUtils.toSharpDay(Calendar.getInstance());
        dueDate.set(Calendar.YEAR, due_year);
        dueDate.set(Calendar.MONTH, due_month);
        dueDate.set(Calendar.DAY_OF_MONTH, due_day);

        return dueDate.getTimeInMillis() < today.getTimeInMillis();
    }
}

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

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;

/**
 * Created by corvo on 3/12/16.
 */
class MyBorrowBookAdapter extends ArrayAdapter<MyBorrowBook> {
    public MyBorrowBookAdapter(Context context, int resource, List<MyBorrowBook> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyBorrowBook myBorrowBook = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitem_library_borrowbook, null);
        }

        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        TextView tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        TextView tv_renderdate = (TextView) convertView.findViewById(R.id.tv_renderdate);
        TextView tv_duedate = (TextView) convertView.findViewById(R.id.tv_duedate);
        TextView tv_renewtime = (TextView) convertView.findViewById(R.id.tv_renewtime);
        Button btn_renew = (Button) convertView.findViewById(R.id.btn_lib_renew);

        tv_title.setText(myBorrowBook.getTitle());
        tv_author.setText(myBorrowBook.getAuthor());
        tv_renderdate.setText("借阅时间：" + myBorrowBook.getRenderDate());
        tv_duedate.setText("应还时间：" + myBorrowBook.getDueDate()
                + (isOverdue(myBorrowBook.getDueDate()) ? "（已到期）" : ""));
        tv_renewtime.setText("续借次数：" + myBorrowBook.getRenewTime());

        if (isDue(myBorrowBook.getDueDate())) {
            //如果已经接近归还日期则标红日期
            tv_duedate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorLectureprimary));
        } else {
            tv_duedate.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        }

        btn_renew.setEnabled(myBorrowBook.getRenewTime().equals("0") && !isOverdue(myBorrowBook.getDueDate()));

        btn_renew.setOnClickListener(!btn_renew.isEnabled() ? null : v -> {
            ContextUtils.showMessage(getContext(), "正在请求续借，请稍候…");

            new ApiRequest(getContext()).api(ApiHelper.API_RENEW).uuid()
                    .post("barcode", myBorrowBook.getBarcode())
                    .onFinish((success, code, response) -> {
                        try {
                            response = new JSONObject(response).getString("content");
                            ContextUtils.showMessage(getContext(),
                                    response.equals("success") ? "续借成功" : response);
                        } catch (JSONException e) {
                            ContextUtils.showMessage(getContext(), "续借失败");
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

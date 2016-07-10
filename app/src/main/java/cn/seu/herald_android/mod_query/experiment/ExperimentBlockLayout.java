package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;

public class ExperimentBlockLayout extends LinearLayout {

    private long time = 0;

    String description;

    public ExperimentBlockLayout(Context context, ExperimentItem item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null);
        TextView content = (TextView) contentView.findViewById(R.id.content);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        TextView subtitle = (TextView) contentView.findViewById(R.id.subtitle);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorExperimentprimary));

        title.setText(item.name);
        subtitle.setText(item.teacher);
        content.setText(item.date + " " + item.time + " @ " + item.address);
        addView(contentView);

        String[] ymdStr = item.date.replace("年", "-").replace("月", "-").replace("日", "-").split("-");
        int year = Integer.valueOf(ymdStr[0]);
        int month = Integer.valueOf(ymdStr[1]);
        int date = Integer.valueOf(ymdStr[2]);
        int hour = Integer.valueOf(item.time.split(":")[0]);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, date);
        cal = CalendarUtils.toSharpDay(cal);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        time = cal.getTimeInMillis();

        description = title.getText().toString() + "|"
                + subtitle.getText().toString() + "|"
                + content.getText().toString() + "|";
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return description;
    }
}

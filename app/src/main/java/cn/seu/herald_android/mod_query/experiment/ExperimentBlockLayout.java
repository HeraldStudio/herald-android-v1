package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;

public class ExperimentBlockLayout extends LinearLayout {

    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;

    private long time = 0;

    public ExperimentBlockLayout(Context context, ExperimentModel item) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null));
        ButterKnife.bind(this);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorExperimentprimary));

        title.setText(item.name);
        subtitle.setText(item.teacher);
        content.setText(item.date + " " + item.time + " @ " + item.address);

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
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return title.getText().toString() + "|"
                + subtitle.getText().toString() + "|"
                + content.getText().toString() + "|";
    }
}

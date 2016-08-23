package cn.seu.herald_android.app_module.cardextra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.framework.json.JArr;

class CardRecordModel {
    // 消费日期
    private String date;
    // 消费时间
    private String time;
    // 消费数目
    private String price;
    // 消费种类
    private String type;
    // 扣费系统（消费地点
    private String system;
    // 消费后余额
    private String left;

    private CardRecordModel(String date, String time, String price, String type, String system, String left) {
        this.date = date;
        this.time = time;
        this.price = price;
        this.type = type;
        this.system = system;
        this.left = left;
    }

    public static ArrayList<CardRecordModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<CardRecordModel> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            list.add(new CardRecordModel(
                    jsonArray.$o(i).$s("date").split(" ")[0],
                    jsonArray.$o(i).$s("date").split(" ")[1],
                    jsonArray.$o(i).$s("price"),
                    jsonArray.$o(i).$s("type"),
                    jsonArray.$o(i).$s("system"),
                    jsonArray.$o(i).$s("left")
            ));
        }
        return list;
    }

    public String getDate() {
        return date;
    }

    public String getDisplayDate() {
        Calendar cal = Calendar.getInstance();

        if (date.equals(new SimpleDateFormat("yyyy/MM/dd").format(cal.getTime()))){
            return "今天";
        }

        cal.roll(Calendar.DATE, -1);

        if (date.equals(new SimpleDateFormat("yyyy/MM/dd").format(cal.getTime()))){
            return "昨天";
        }

        return getDate();
    }

    public String getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String getSystem() {
        return system;
    }

    public String getLeft() {
        return left;
    }

    public String getTime() {
        return time;
    }

}

package cn.seu.herald_android.app_module.library;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

class HotBookModel {
    // 书的借阅次数
    private int count;
    // 书所在的位置
    private String place;
    // 书名
    private String name;
    // 作者
    private String author;

    private HotBookModel(int count, String place, String name, String author) {
        this.count = count;
        this.place = place;
        this.name = name;
        this.author = author;
    }

    public int getCount() {
        return count;
    }

    public String getPlace() {
        return place;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public static ArrayList<HotBookModel> transformJArrToArrayList(JArr array) {
        ArrayList<HotBookModel> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JObj objectItem = array.$o(i);
            list.add(new HotBookModel(
                    objectItem.$i("count"),
                    objectItem.$s("place"),
                    objectItem.$s("name"),
                    objectItem.$s("author")
            ));
        }
        return list;
    }
}

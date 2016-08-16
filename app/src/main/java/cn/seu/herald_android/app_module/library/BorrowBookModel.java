package cn.seu.herald_android.app_module.library;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class BorrowBookModel {
    // 应归还日期
    public String dueDate;
    // 书作者
    public String author;
    // 书编号
    public String barcode;
    // 借阅日期(此处感觉是服务端写错名字了)
    public String renderDate;
    // 书所在位置
    public String place;
    // 书的名字
    public String title;
    // 本书的续借次数
    public String renewTime;

    private BorrowBookModel(String dueDate, String author, String barcode, String renderDate, String place, String title, String renewTime) {
        this.dueDate = dueDate;
        this.author = author;
        this.barcode = barcode;
        this.renderDate = renderDate;
        this.place = place;
        this.title = title;
        this.renewTime = renewTime;
    }

    public static ArrayList<BorrowBookModel> transformJArrToArrayList(JArr array) {
        ArrayList<BorrowBookModel> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JObj object = array.$o(i);
            list.add(new BorrowBookModel(
                    object.$s("due_date"),
                    object.$s("author"),
                    object.$s("barcode"),
                    object.$s("render_date"),
                    object.$s("place"),
                    object.$s("title"),
                    object.$s("renew_time")
            ));
        }
        return list;
    }

}

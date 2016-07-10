package cn.seu.herald_android.mod_query.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyBorrowBook {
    //应归还日期
    public String dueDate;
    //书作者
    public String author;
    //书编号
    public String barcode;
    //借阅日期(此处感觉是服务端写错名字了)
    public String renderDate;
    //书所在位置
    public String place;
    //书的名字
    public String title;
    //本书的续借次数
    public String renewTime;

    private MyBorrowBook(String dueDate, String author, String barcode, String renderDate, String place, String title, String renewTime) {
        this.dueDate = dueDate;
        this.author = author;
        this.barcode = barcode;
        this.renderDate = renderDate;
        this.place = place;
        this.title = title;
        this.renewTime = renewTime;
    }

    public static ArrayList<MyBorrowBook> transformJSONArrayToArrayList(JSONArray array)throws JSONException{
        ArrayList<MyBorrowBook> list = new ArrayList<>();
        for(int i = 0 ;i<array.length();i++){
            JSONObject object = array.getJSONObject(i);
            list.add(new MyBorrowBook(
                    object.getString("due_date"),
                    object.getString("author"),
                    object.getString("barcode"),
                    object.getString("render_date"),
                    object.getString("place"),
                    object.getString("title"),
                    object.getString("renew_time")
            ));
        }
        return list;
    }

}

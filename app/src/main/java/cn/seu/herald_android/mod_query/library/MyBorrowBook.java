package cn.seu.herald_android.mod_query.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by corvo on 3/6/16.
 */
public class MyBorrowBook extends Book {
    //应归还日期
    private String dueDate;
    //书作者
    private String author;
    //书编号
    private String barcode;
    //借阅日期(此处感觉是服务端写错名字了)
    private String renderDate;
    //书所在位置
    private String place;
    //书的名字
    private String title;
    //本书的续借次数
    private String renewTime;

    public MyBorrowBook(String dueDate, String author, String barcode, String renderDate, String place, String title, String renewTime) {
        this.dueDate = dueDate;
        this.author = author;
        this.barcode = barcode;
        this.renderDate = renderDate;
        this.place = place;
        this.title = title;
        this.renewTime = renewTime;
    }

    public String getDueDate() {
        return dueDate;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getRenderDate() {
        return renderDate;
    }

    public String getPlace() {
        return place;
    }

    public String getTitle() {
        return title;
    }

    public String getRenewTime() {
        return renewTime;
    }

    public static ArrayList<MyBorrowBook> transfromJSONArrayToArrayList(JSONArray array)throws JSONException{
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

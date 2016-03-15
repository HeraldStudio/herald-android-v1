package cn.seu.herald_android.mod_query.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by corvo on 2/14/16.
 * 代表搜索图书馆藏书，所得到的图书对象
 */
public class Book {
    //书所在位置索引
    private String index;
    //书的总数
    private int all;
    //书名
    private String name;
    //书作者
    private String author;
    //出版社
    private String publish;
    //书类型
    private String type;
    //剩余书数量
    private int left;


    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublish() {
        return publish;
    }

    public void setPublish(String publish) {
        this.publish = publish;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public static ArrayList<Book> transformJSONArrayToArrayList(JSONArray array)throws JSONException{
        ArrayList<Book> resList = new ArrayList<>();
        for(int i = 0;i<array.length();i++){
            JSONObject jsonBook = array.getJSONObject(i);
            Book book = new Book();
            book.setAll(jsonBook.getInt("all"));
            book.setIndex(jsonBook.getString("index"));
            book.setName(jsonBook.getString("name"));
            book.setAuthor(jsonBook.getString("author"));
            book.setPublish(jsonBook.getString("publish"));
            book.setType(jsonBook.getString("type"));
            book.setLeft(jsonBook.getInt("left"));
            resList.add(book);
        }
        return resList;
    }

}


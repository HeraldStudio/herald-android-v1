package cn.seu.herald_android.mod_query.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by corvo on 2/14/16.
 * 代表搜索图书馆藏书，所得到的图书对象
 */
class Book {
    //书所在位置索引
    public String index;
    //书的总数
    public int all;
    //书名
    public String name;
    //书作者
    public String author;
    //出版社
    public String publish;
    //书类型
    public String type;
    //剩余书数量
    public int left;

    public static ArrayList<Book> transformJSONArrayToArrayList(JSONArray array)throws JSONException{
        ArrayList<Book> resList = new ArrayList<>();
        for(int i = 0;i<array.length();i++){
            JSONObject jsonBook = array.getJSONObject(i);
            Book book = new Book();
            book.all = (jsonBook.getInt("all"));
            book.index = (jsonBook.getString("index"));
            book.name = (jsonBook.getString("name"));
            book.author = (jsonBook.getString("author"));
            book.publish = (jsonBook.getString("publish"));
            book.type = (jsonBook.getString("type"));
            book.left = (jsonBook.getInt("left"));
            resList.add(book);
        }
        return resList;
    }

}


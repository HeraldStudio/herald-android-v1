package cn.seu.herald_android.app_module.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class SearchBookModel {
    // 书所在位置索引
    public String index;
    // 书的总数
    public int all;
    // 书名
    public String name;
    // 书作者
    public String author;
    // 出版社
    public String publish;
    // 书类型
    public String type;
    // 剩余书数量
    public int left;

    public static ArrayList<SearchBookModel> transformJSONArrayToArrayList(JSONArray array) throws JSONException {
        ArrayList<SearchBookModel> resList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonBook = array.getJSONObject(i);
            SearchBookModel bookModel = new SearchBookModel();
            bookModel.all = (jsonBook.getInt("all"));
            bookModel.index = (jsonBook.getString("index"));
            bookModel.name = (jsonBook.getString("name"));
            bookModel.author = (jsonBook.getString("author"));
            bookModel.publish = (jsonBook.getString("publish"));
            bookModel.type = (jsonBook.getString("type"));
            bookModel.left = (jsonBook.getInt("left"));
            resList.add(bookModel);
        }
        return resList;
    }

}


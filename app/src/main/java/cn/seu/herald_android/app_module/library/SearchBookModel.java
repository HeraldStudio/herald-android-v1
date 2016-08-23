package cn.seu.herald_android.app_module.library;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

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

    public static ArrayList<SearchBookModel> transformJArrToArrayList(JArr array) {
        ArrayList<SearchBookModel> resList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JObj jsonBook = array.$o(i);
            SearchBookModel bookModel = new SearchBookModel();
            bookModel.all = (jsonBook.$i("all"));
            bookModel.index = (jsonBook.$s("index"));
            bookModel.name = (jsonBook.$s("name"));
            bookModel.author = (jsonBook.$s("author"));
            bookModel.publish = (jsonBook.$s("publish"));
            bookModel.type = (jsonBook.$s("type"));
            bookModel.left = (jsonBook.$i("left"));
            resList.add(bookModel);
        }
        return resList;
    }

}


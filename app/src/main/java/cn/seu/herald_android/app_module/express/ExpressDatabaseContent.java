package cn.seu.herald_android.app_module.express;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.List;

import static cn.seu.herald_android.helper.LogUtils.makeLogTag;

/**
 * 数据库操作类
 * Created by corvo on 8/7/16.
 */
public class ExpressDatabaseContent {

    private static String TAG = makeLogTag(ExpressDatabaseContent.class);
    private static ExpressDatabaseHelper mDBHelper;

    public ExpressDatabaseContent(Context context) {
        mDBHelper = new ExpressDatabaseHelper(context, "express_history.db", null, 1);
    }

    public void dbInsert(ExpressInfo info) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", info.getUsername());
        values.put("userphone", info.getUserphone());
        values.put("sms", info.getSmsInfo());
        values.put("dest", info.getDest());
        values.put("arrival", info.getArrival());
        values.put("locate", info.getLocate());
        values.put("weight", info.getWeight());
        values.put("submit_time", info.getSubmitTime());
        values.put("is_fetched", info.isFetched());

        db.insert("express", null, values);
    }

    public List<ExpressInfo> dbQuery() {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        List<ExpressInfo> infoList = new ArrayList<>();

        Cursor cursor = db.query("express", null, null, null, null, null, "submit_time DESC"); // 按照时间逆序
        if (cursor.moveToFirst()) {
            do {
                ExpressInfo info = new ExpressInfo();
                info.setUsername(cursor.getString(1));
                info.setUserphone(cursor.getString(2));
                info.setSmsInfo(cursor.getString(3));
                info.setDest(cursor.getString(4));
                info.setArrival(cursor.getString(5));
                info.setLocate(cursor.getString(6));
                info.setWeight(cursor.getString(7));
                info.setSubmitTime(cursor.getLong(8));
                info.setFetched(cursor.getString(9).equals("1"));

                infoList.add(info);
            } while (cursor.moveToNext());
        }
        return infoList;
    }

    public void dbRefresh(String phone, Long timeStamp, boolean isFetched) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String[] args = new String[]{phone, String.valueOf(timeStamp)};
        values.put("is_fetched", isFetched);
        db.update("express", values, "userphone = ? AND submit_time = ?", args);
        Log.d(TAG, "数据库更新成功");
    }
}

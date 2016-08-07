package cn.seu.herald_android.app_module.express;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作类
 * Created by corvo on 8/7/16.
 */
public class ExpressDatabaseContent {

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
        Cursor cursor = db.query("express", null, null, null, null, null, null);
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
}

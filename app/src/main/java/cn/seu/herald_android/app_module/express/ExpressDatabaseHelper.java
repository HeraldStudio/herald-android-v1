package cn.seu.herald_android.app_module.express;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExpressDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_EXPRESS = "CREATE TABLE express("
            + "id integer primary key autoincrement, "
            + "username text, "
            + "userphone text, "
            + "sms text, "
            + "dest text, "
            + "arrival text, "
            + "locate text, "
            + "weight text, "
            + "submit_time datetime, "
            + "is_fetched text, "
            + "is_received text)";

    private Context mContext;
    public ExpressDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                                  int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EXPRESS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

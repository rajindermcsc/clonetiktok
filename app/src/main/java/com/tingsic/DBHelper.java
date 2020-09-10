package com.tingsic;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by Shubham on 08/07/20.
 */
public class DBHelper
{
    public static final String Notification_ID = "notification_id";
    public static final String User_ID = "user_id";
    public static final String User = "user";
    public static final String Notification_Title = "notification_title";
    public static final String Notification_Description = "notification_description";
    public static final String Notification_Date = "notification_date";
    private static final String TAG = "DBHelper";
    private final Context mContext;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static final String DATABASE_NAME = "Notification.db";
    private static final int DATABASE_VERSION = 1;
    private static final String Notification_TABLE = "notificationtable";
    private static final String User_TABLE = "usertable";
    private static final String CREATE_Notificatio_TABLE =
            "CREATE TABLE " + Notification_TABLE + " (" +
                    Notification_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ Notification_Title
                    +" Varchar,"+ Notification_Description +" Varchar,"+ Notification_Date +" Varchar, "
                    + User_ID + " INTEGER not null, FOREIGN KEY ("+User_ID+") REFERENCES "
                    +User_TABLE+" ("+User_ID+"));";
    private static final String CREATE_USER =
            "CREATE TABLE " + User_TABLE + " (" +
                    User_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + User +" Varchar );";
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db)
        {

            db.execSQL(CREATE_Notificatio_TABLE);
            db.execSQL(CREATE_USER);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + CREATE_Notificatio_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + User_TABLE);
            onCreate(db);
        }
    }
    public void Reset()
    {
        mDbHelper.onUpgrade(this.mDb, 1, 1);
    }

    public DBHelper(Context ctx) {
        mContext = ctx;
        mDbHelper = new DatabaseHelper(mContext);
    }
    public DBHelper open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    public void close()
    {
        mDbHelper.close();
    }
    public void insertnotification( String title, String text,String noti_date,int id) {
        //Log.e(TAG, "insertImagedefault: "+text );
        ContentValues cv2 = new ContentValues();
        cv2.put(Notification_Title,title);
        cv2.put(Notification_Description,text);
        cv2.put(Notification_Date,noti_date);
        cv2.put(User_ID,id);
        mDb.insert(Notification_TABLE, null, cv2);
    }
    public void insertUser(String user) {
        ContentValues cv2 = new ContentValues();
        cv2.put(User,user);
        mDb.insert(User_TABLE, null, cv2);
    }
}

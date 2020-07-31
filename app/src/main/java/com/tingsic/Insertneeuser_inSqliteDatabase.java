package com.tingsic;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.tingsic.Utils.Myconstant;

import static android.content.Context.MODE_PRIVATE;

public class Insertneeuser_inSqliteDatabase
{
    private static final String TAG = Insertneeuser_inSqliteDatabase.class.getSimpleName();
    Context context;
    SQLiteDatabase mDB;
    DBHelper dbHelper;
    Cursor curuser;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean userexist=true;
    public Insertneeuser_inSqliteDatabase(Context context)
    {
        this.context=context;
        sharedPreferences=context.getSharedPreferences(Myconstant.Sharedprefernce,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        dbHelper=new DBHelper(context);
        mDB= context.openOrCreateDatabase(Myconstant.DATABASE_NAME, MODE_PRIVATE, null);
    }

    public void insert_new_userindatabase()
    {
        try {
            dbHelper.open();
            curuser = mDB.rawQuery("SELECT  * FROM usertable",null);
            if (curuser.getCount()>0)
            {
                curuser.moveToFirst();
                do {
                    String userloginid = curuser.getString(curuser.getColumnIndex("user"));
                    Log.e(TAG, "savedata_tosqlite: " + userloginid);
                    if (userloginid.equalsIgnoreCase(sharedPreferences.getString(Myconstant.USER_LOGINID, "")))
                    {
                        userexist=false;
                    }
                } while (curuser.moveToNext());
            }
            else
            {
                Log.e(TAG, "new data inserted"+sharedPreferences.getString(Myconstant.USER_LOGINID,""));
                userexist=false;
                dbHelper.insertUser(sharedPreferences.getString(Myconstant.USER_LOGINID,""));

            }
            if (userexist)
            {
                Log.e(TAG, "new data inserted"+sharedPreferences.getString(Myconstant.USER_LOGINID,""));
                dbHelper.insertUser(sharedPreferences.getString(Myconstant.USER_LOGINID,""));
                userexist=false;
            }
            dbHelper.close();
            Log.e(TAG, "run: "+curuser.getCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

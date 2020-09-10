package com.tingsic.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tingsic.DBHelper;
import com.tingsic.R;
import com.tingsic.Utils.Myconstant;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Notificationadapter extends RecyclerView.Adapter
{
    private static final String TAG = "Notificationadapter";
    Context context;
    SQLiteDatabase mDb;
    DBHelper dbHelper;
    ArrayList<String> noti_id=new ArrayList<String>();
    Cursor cur;
    public Notificationadapter(Context context,  String userid)
    {
        this.context=context;
        dbHelper=new DBHelper(context);
        noti_id.clear();
        mDb= context.openOrCreateDatabase(Myconstant.DATABASE_NAME, MODE_PRIVATE, null);
        cur =  mDb.rawQuery("Select * from notificationtable where user_id='"+userid+"'ORDER BY notification_id DESC ", null);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view= LayoutInflater.from(context).inflate(R.layout.notificationadapter_layout,parent,false);
        return new MyViewholder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        cur.moveToPosition(position);
        noti_id.add(cur.getString(cur.getColumnIndex("notification_id")));
        if (cur.getString(cur.getColumnIndex("notification_title")).isEmpty())
        {
            ((MyViewholder)holder).title.setVisibility(View.GONE);
        }
        //Log.e(TAG, "onBindViewHolder: " + cur.getString(cur.getColumnIndex("notification_id")));
        ((MyViewholder)holder).title.setText(cur.getString(cur.getColumnIndex("notification_title")));
        ((MyViewholder)holder).desc.setText(cur.getString(cur.getColumnIndex("notification_description")));
        try
        {
            ((MyViewholder)holder).time.setText(cur.getString(cur.getColumnIndex("notification_date")));
        }catch(Exception e)
        {
            //Log.e(TAG, "onBindViewHolder: "+e.getMessage());
        }
        ((MyViewholder)holder).bellicon.setImageDrawable(context.getResources().getDrawable(R.drawable.bellicon));
    }
    @Override
    public int getItemCount()
    {
        return cur.getCount();
    }
    private class MyViewholder extends RecyclerView.ViewHolder
    {
        TextView title,desc,time;
        RelativeLayout mainlayout;
        ImageView bellicon;
        Boolean ismultiselection=false;
        public MyViewholder(View view) {
            super(view);
            title=view.findViewById(R.id.title);
            desc=view.findViewById(R.id.desc);
            time=view.findViewById(R.id.time);
            bellicon=view.findViewById(R.id.bellicon);
        }
    }
    public void removeItem(int position) {
        //Log.e(TAG, "removeItem: "+position );
        mDb.execSQL("delete from notificationtable where notification_id='"+noti_id.get(position)+"'");

        notifyItemRemoved(position);

    }
}

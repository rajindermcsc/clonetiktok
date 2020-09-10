package com.tingsic.Fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tingsic.Adapter.Notificationadapter;
import com.tingsic.DBHelper;
import com.tingsic.Listner.OnContestListener;
import com.tingsic.R;
import com.tingsic.Utils.Myconstant;

import static android.content.Context.MODE_PRIVATE;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    Notificationadapter notificationadapter;
    SQLiteDatabase mDb;
    DBHelper dbHelper;
    Cursor curuser;
    String userid;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Paint p = new Paint();
    TextView nodatafound;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.activity_notification, container, false);
        sharedPreferences = getContext().getSharedPreferences(Myconstant.Sharedprefernce, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        dbHelper = new DBHelper(getContext());
        // noti_id.clear();
        mDb = getContext().openOrCreateDatabase(Myconstant.DATABASE_NAME, MODE_PRIVATE, null);
        recyclerView = view.findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        try {
            getdatafrom_database();
        } catch (Exception e)
        {
            //Log.e(TAG, "onCreate1: " + e.getMessage());
        }
        nodatafound = view.findViewById(R.id.nodatafound);
        try {
            //Log.e(TAG, "onCreate: " + curuser.getCount());
            if (curuser.getCount() == 0) {
                nodatafound.setVisibility(View.VISIBLE);
            } else {
                nodatafound.setVisibility(View.GONE);
                notificationadapter = new Notificationadapter(getContext(),userid);
                recyclerView.setAdapter(notificationadapter);
                initViews();
            }
        } catch (Exception e) {
            //Log.e(TAG, "onCreate3: " + e.getMessage());
        }
        return view;
    }

    public void getdatafrom_database()
    {
        try {
            curuser = mDb.rawQuery("SELECT  * FROM usertable", null);
            //Log.e(TAG, "savedata_tosqlite: " + curuser.getCount());
            curuser.moveToFirst();
            do {
                String userloginid = curuser.getString(curuser.getColumnIndex("user"));
                //Log.e(TAG, "savedata_tosqlite: " + userloginid);
                if (userloginid.equals(sharedPreferences.getString(Myconstant.USER_LOGINID, ""))) {
                    userid = curuser.getString(curuser.getColumnIndex("user_id"));
                    try {
                        //Log.e(TAG, "userid: " + userid);
                        curuser = mDb.rawQuery("Select * from notificationtable where user_id='" + userid + "'ORDER BY notification_id DESC ", null);
                        //Log.e(TAG, "onCreate: " + curuser.getCount());
                    } catch (Exception e) {
                        //Log.e(TAG, "exception: " +e.getMessage());
                    }
                } else {
                    //Log.e(TAG, "no userfound");
                }
            } while (curuser.moveToNext());
        } catch (Exception e) {
            //Log.e(TAG, "Excetion1: " + e.getMessage());
        }
    }
    public void initViews() {
        notificationadapter.notifyDataSetChanged();
        initSwipe();
    }
    public void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                notificationadapter.removeItem(position);
                curuser = mDb.rawQuery("Select * from notificationtable ORDER BY notification_id DESC ", null);
                if (curuser.getCount() == 0) {
                    nodatafound.setVisibility(View.VISIBLE);
                    notificationadapter = new Notificationadapter(getContext(), userid);
                    recyclerView.setAdapter(notificationadapter);
                } else {
                    nodatafound.setVisibility(View.GONE);

                    notificationadapter = new Notificationadapter(getContext(),  userid);
                    recyclerView.setAdapter(notificationadapter);
                }
            }

            @SuppressLint("ResourceType")
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;
                    if (dX > 0) {
                        p.setColor(Color.parseColor(getResources().getString(R.color.redbtn)));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.delete_icon);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(Color.parseColor(getResources().getString(R.color.redbtn)));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);

                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.delete_icon);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
package com.tingsic.Adapter;

import android.app.Activity;
import android.content.Context;
//import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tingsic.POJO.sound.Sound;
import com.tingsic.POJO.sound.SoundCategory;
import com.tingsic.R;
import com.tingsic.Transformation.CircleTransform;

import java.util.ArrayList;

public class SoundsAdapter extends RecyclerView.Adapter<SoundsAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<SoundCategory> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Sound item);
    }

    public SoundsAdapter.OnItemClickListener listener;

    public SoundsAdapter(Context context, ArrayList<SoundCategory> arrayList, SoundsAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }


    @Override
    public SoundsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_category_sound_layout, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final SoundsAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);


        SoundCategory item = datalist.get(i);

        holder.title.setText(item.catagory);


        Sound_Items_Adapter adapter = new Sound_Items_Adapter(context, item.sound_list, new Sound_Items_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Sound item) {

                listener.onItemClick(view, postion, item);
            }
        });

        GridLayoutManager gridLayoutManager;


        gridLayoutManager = new GridLayoutManager(context, item.sound_list.size() != 0 ? item.sound_list.size() : 3);


        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        holder.recyclerView.setLayoutManager(gridLayoutManager);
        holder.recyclerView.setAdapter(adapter);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.findSnapView(gridLayoutManager);
        snapHelper.attachToRecyclerView(holder.recyclerView);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RecyclerView recyclerView;

        public CustomViewHolder(View view) {
            super(view);
            //  image=view.findViewById(R.id.image);
            title = view.findViewById(R.id.title);
            recyclerView = view.findViewById(R.id.horizontal_recylerview);


        }


    }


}


class Sound_Items_Adapter extends RecyclerView.Adapter<Sound_Items_Adapter.CustomViewHolder> {
    public Context context;

    ArrayList<Sound> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Sound item);
    }

    public Sound_Items_Adapter.OnItemClickListener listener;


    public Sound_Items_Adapter(Context context, ArrayList<Sound> arrayList, Sound_Items_Adapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public Sound_Items_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(getScreenWidth(viewGroup.getContext()) - 50, RecyclerView.LayoutParams.WRAP_CONTENT));
        Sound_Items_Adapter.CustomViewHolder viewHolder = new Sound_Items_Adapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final Sound_Items_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        Sound item = datalist.get(i);
        try {

            holder.bind(i, datalist.get(i), listener);

            holder.sound_name.setText(item.sound_name);
            holder.description_txt.setText(item.description);


            if (item.thum.equals("")) {
                item.thum = "Null";
            }
            Picasso.get().load(item.thum)
                    .placeholder(context.getResources().getDrawable(R.drawable.bg_black_oval))
                    .transform(new CircleTransform())
                    .into(holder.sound_image);


        } catch (Exception e) {

        }

    }

    private static int getScreenWidth(Context context) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done, fav_btn;
        TextView sound_name, description_txt;
        ImageView sound_image;

        public CustomViewHolder(View view) {
            super(view);
            //  image=view.findViewById(R.id.image);
            done = view.findViewById(R.id.done);
            fav_btn = view.findViewById(R.id.fav_btn);


            sound_name = view.findViewById(R.id.sound_name);
            description_txt = view.findViewById(R.id.description_txt);
            sound_image = view.findViewById(R.id.sound_image);

        }

        public void bind(final int pos, final Sound item, final Sound_Items_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

            fav_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, pos, item);
                }
            });

        }


    }


}
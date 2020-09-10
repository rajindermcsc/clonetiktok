package com.tingsic.Adapter;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tingsic.Listner.OnVideoListener;
import com.tingsic.POJO.Video.Video;
import com.tingsic.R;
import com.tingsic.databinding.ItemHashtagBinding;

import java.util.ArrayList;
import java.util.List;

import static com.tingsic.FourChamp.VIDEO_BASE_URL;

public class HashTagAdapter extends RecyclerView.Adapter<HashTagAdapter.HashTagViewHolder> {
    private ArrayList<Video> list = new ArrayList<>();
    private OnVideoListener videoListener;

    public void setVideoListener(OnVideoListener videoListener) {
        this.videoListener = videoListener;
    }

    @NonNull
    @Override
    public HashTagViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_hashtag, viewGroup, false);
        return new HashTagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HashTagViewHolder hashTagViewHolder, int i) {
        hashTagViewHolder.setModel(i, list.get(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<Video> videos) {
        if (!videos.isEmpty()) {
            list.addAll(videos);
            notifyDataSetChanged();
        }
    }

    public class HashTagViewHolder extends RecyclerView.ViewHolder implements Target {
        ItemHashtagBinding binding;
        final RenderScript rs;

        HashTagViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            rs = RenderScript.create(itemView.getContext());
        }

        void setModel(final int i, Video video) {
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoListener.onVideoSelected(i, list);
                }
            });
            String thumb_url = video.getThumbUrl();
            if (thumb_url.isEmpty()) {
                //Picasso.get().load(R.drawable.ic_video).into(myViewHolder.imageView);
                binding.ivthumb.setImageDrawable(itemView.getContext().getResources().getDrawable(R.drawable.ic_video));
                binding.ivthumb.setBackgroundColor(Color.parseColor("#39476C"));
            } else {
                //Log.e("Thumb", "onBindViewHolder: " + VIDEO_BASE_URL + thumb_url);
                Picasso.get().load(VIDEO_BASE_URL + thumb_url).placeholder(R.drawable.post_placeholder).into(this);
            }
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/4,bitmap.getHeight()/4,false);
            //bitmap.compress(Bitmap.CompressFormat.JPEG,50,null);
            int targetWidth = itemView.getWidth();

            double aspectRatio = (double) bitmap.getHeight() / (double) bitmap.getWidth();

            int targetHeight = (int) (targetWidth * aspectRatio);

            if (targetHeight <= 0 && targetWidth <= 0) {
                targetHeight = bitmap.getHeight() / 4;
                targetWidth = bitmap.getWidth() / 4;
            }

            Bitmap result = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);

            binding.ivthumb.setImageBitmap(result);

            Bitmap bitmapOriginal = Bitmap.createScaledBitmap(result, 50, 100, false);//bitmap.copy(bitmap.getConfig(),false);
            //.getDrawingCache().copy(bitmap.getConfig(), false);

            final Allocation input = Allocation.createFromBitmap(rs, bitmapOriginal); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(23f);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmapOriginal);

            //BitmapUtils bitmapUtils = new BitmapUtils();

            //bitmapOriginal = bitmapUtils.getRoundedBitmap(bitmapOriginal,10,0);

            Drawable drawable = new BitmapDrawable(itemView.getContext().getResources(), bitmapOriginal);
            binding.ivthumb.setBackground(drawable);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}

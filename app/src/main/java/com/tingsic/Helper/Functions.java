package com.tingsic.Helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.tingsic.R;

import java.io.File;

public class Functions {

    public static void make_directry(String path){
        File dir = new File(path);
        if(!dir.exists())
        {
            dir.mkdir();
        }
    }

    public static void Show_Alert(Context context, String title, String Message){
        new  AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(Message)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static Dialog determinant_dialog;
    public static ProgressBar determinant_progress;

    public static void Show_determinent_loader(Context context, boolean outside_touch, boolean cancleable) {

        determinant_dialog = new Dialog(context);
        determinant_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        determinant_dialog.setContentView(R.layout.item_determinant_progress_layout);
        determinant_dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.d_round_white_background));

        determinant_progress=determinant_dialog.findViewById(R.id.pbar);

        if(!outside_touch)
            determinant_dialog.setCanceledOnTouchOutside(false);

        if(!cancleable)
            determinant_dialog.setCancelable(false);

        determinant_dialog.show();

    }


    public static Dialog dialog;
    public static void Show_loader(Context context,boolean outside_touch, boolean cancleable) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_dialog_loading_view);
        dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.d_round_white_background));

        if(!outside_touch)
            dialog.setCanceledOnTouchOutside(false);

        if(!cancleable)
            dialog.setCancelable(false);

        dialog.show();
    }

    public static void cancel_loader(){
        if(dialog!=null){
            dialog.cancel();
        }
    }

    public static void Show_loading_progress(int progress){
        if(determinant_progress!=null ){
            determinant_progress.setProgress(progress);

        }
    }

    public static void cancel_determinent_loader(){
        if(determinant_dialog!=null){
            determinant_progress=null;
            determinant_dialog.cancel();
        }
    }
}

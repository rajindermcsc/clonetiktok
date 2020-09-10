package com.tingsic.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tingsic.API.ApiClient;
import com.tingsic.API.ApiInterface;
import com.tingsic.API.ImageProgressRequestBody;
import com.tingsic.API.ImageUploadListener;
import com.tingsic.API.ProgressRequestBody;
import com.tingsic.API.UploadListener;
import com.tingsic.Helper.Functions;
import com.tingsic.POJO.Auth;
import com.tingsic.POJO.Upload.Multipart.UploadVideoResponse;
import com.tingsic.POJO.Upload.Video.AddVideoRequest;
import com.tingsic.POJO.Upload.Video.AddVideoResponse;
import com.tingsic.POJO.Upload.Video.Data;
import com.tingsic.POJO.Upload.Video.Request;
import com.tingsic.R;
import com.tingsic.Utils.PrefManager;
import com.tingsic.Utils.Variables;
import com.tingsic.View.SocialView.SocialEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostVideoActivity extends AppCompatActivity implements UploadListener, ImageUploadListener {
    ImageView video_thumbnail;

    String video_path;

    EditText et_desc;
    SocialEditText et_hastag;
    private int PAYMENT_REQUEST=232;
    String imagePath;
    ImageButton close,goback;
    Intent intent;
    private static int CAMERA = 101;

    //ServiceCallback serviceCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);
        intent=getIntent();

        video_path = intent.getStringExtra("srcMp4Path");
//        video_path = Variables.output_filter_file;
        video_thumbnail = findViewById(R.id.video_thumbnail);
        et_hastag = findViewById(R.id.et_hastag);
        et_desc = findViewById(R.id.et_desc);
        goback = findViewById(R.id.goback);
        close = findViewById(R.id.close);

        // this will get the thumbnail of video and show them in imageview
        Bitmap bmThumbnail;
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(video_path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        if (bmThumbnail != null) {
            video_thumbnail.setImageBitmap(bmThumbnail);
        } else {
        }








        goback=findViewById(R.id.goback);
        close=findViewById(R.id.close);
        goback .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                onBackPressed();
            }
        });
        close .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent=new Intent(PostVideoActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        findViewById(R.id.post_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){

                //progressDialog.show();
                //Start_Service();
                ////Log.e("TAG", "onClick: "+video_path);
                //Log.e("TAG", "onClick: "+video_thumbnail);
                File thumbfile = new File(getExternalFilesDir(null), "temp.jpeg");
                try {
                    FileOutputStream stream = new FileOutputStream(thumbfile);

                    Bitmap bmThumbnail;
                    bmThumbnail = ThumbnailUtils.createVideoThumbnail(video_path,
                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                    if (bmThumbnail != null) {
                        bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                       imagePath = thumbfile.getPath();
                    } else {
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                uploadVideoAPI(video_path,imagePath);

            }
        });

    }

    private void uploadVideoAPI(String videoPath, String imagePath) {
        //Log.e("TAG", "uploadVideoAPI: "+videoPath);
        //Log.e("TAG", "uploadVideoAPI: "+imagePath);

        File videoFile = new File(videoPath);
        File imageFile = new File(imagePath);

        //todo remove Auth from here! :(
        Functions.Show_determinent_loader(this,false,false);


        ProgressRequestBody requestBody = new ProgressRequestBody(videoFile, this);
        ImageProgressRequestBody imageRequestBody = new ImageProgressRequestBody(imageFile, this);

        MultipartBody.Part videoBody = MultipartBody.Part.createFormData("video", videoFile.getName(), requestBody);
        MultipartBody.Part imageBody = MultipartBody.Part.createFormData("image", imageFile.getName(), imageRequestBody);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UploadVideoResponse> responseCall = apiInterface.uploadVideo(videoBody, imageBody);
        responseCall.enqueue(new Callback<UploadVideoResponse>() {
            @Override
            public void onResponse(Call<UploadVideoResponse> call, Response<UploadVideoResponse> response) {
                //Log.e("TAG", "onResponse() returned: " + response.code());
                Functions.cancel_determinent_loader();
                if (response.isSuccessful()) {
                    Functions.cancel_determinent_loader();
                    if (response.body().getSuccess() == 1) {
                        //Log.e("TAG", "onResponse: success");
                        if (PrefManager.isPaidContest(PostVideoActivity.this)) {

                            PrefManager.setTempVUrl(PostVideoActivity.this, response.body().getData());
                            PrefManager.setTempTUrl(PostVideoActivity.this, response.body().getThumbData());

                            Gson gson = new Gson();

                            String data = gson.toJson(response.body());

//                            btnProccedPayment.setTag(data);

//                            Intent intent = new Intent(PostVideoActivity.this, TestActivity.class);
//                            intent.putExtra("data", data);
//                            if (!et_desc.getText().toString().isEmpty()) {
//                                intent.putExtra("description", et_desc.getText().toString());
//                            }
//                            if (!et_hastag.getHashtags().isEmpty()) {
//                                intent.putExtra("hashTag", TextUtils.join(",", et_hastag.getHashtags()));
//                            }
//                            startActivityForResult(intent, PAYMENT_REQUEST);
                        } else {
                            //Log.e("TAG", "onResponseaddvideo: ");
                            String contestId = PreferenceManager.getDefaultSharedPreferences(PostVideoActivity.this).getString("contest_id", "1");
                            addVideoAPI(response.body().getData(), response.body().getThumbData(), contestId);
                        }
                    } else {
                        //Log.e("TAG", "onResponse: success 0 " + response.body().getMessage());
                    }
                } else {
                    //Log.e("TAG", "onResponse: not succes");
                }
            }

            @Override
            public void onFailure(Call<UploadVideoResponse> call, Throwable t) {
                Functions.cancel_determinent_loader();
                //Log.e("TAG", "onFailure: failed");
            }
        });

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode==270){
            //Log.e("PostVideo", "startActivityForResult: ");
        }
    }

    // this will start the service for uploading the video into database
    public void Start_Service(){

        /*serviceCallback=this;

        Upload_Service mService = new Upload_Service(serviceCallback);
        if (!Functions.isMyServiceRunning(this,mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("startservice");
            mServiceIntent.putExtra("uri",""+ Uri.fromFile(new File(video_path)));
            startService(mServiceIntent);


            Intent intent = new Intent(this, Upload_Service.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
        else {
            Toast.makeText(this, "Please wait video already in uploading progress", Toast.LENGTH_LONG).show();
        }*/


    }



    private void addVideoAPI(String video_url, String thumb_url, String contestId) {
        //Log.e("TAG", "addVideoAPI: "+video_url);
        //Log.e("TAG", "addVideoAPI: "+contestId);
        //Log.e("TAG", "addVideoAPI: "+thumb_url);

        AddVideoRequest videoRequest = new AddVideoRequest();

        Request request = new Request();

        int id = PreferenceManager.getDefaultSharedPreferences(this).getInt("id", -111);
        String token = PreferenceManager.getDefaultSharedPreferences(this).getString("token", "null");

        if (id == -111 || (token != null && token.equals("null"))) {
            //Log.e("TAG", "addVideoAPI: returned");
            return;
        }

        Auth auth = new Auth();
        auth.setId(id);
        auth.setToken(token);
        //Log.e("TAG", "addVideoAPI: "+id);
        //Log.e("TAG", "addVideoAPI: "+token);

        Data data = new Data();
        data.setVideoUrl(video_url);
        data.setThumbUrl(thumb_url);
        data.setContId(contestId);
        data.setUserId("" + id);
        if (!et_hastag.getHashtags().toString().isEmpty()) {

            data.setHashTag(TextUtils.join(",", Collections.singleton(et_hastag.getHashtags().toString())));
            //Log.e("TAG", "addVideoAPIhash: "+data.getHashTag());
        } else {
            data.setHashTag("");
        }
        if (!et_desc.getText().toString().isEmpty()) {
            data.setDescription(et_desc.getText().toString());
            //Log.e("TAG", "addVideoAPIdesc: "+data.getDescription());
        }
        else {
            data.setDescription("");
        }
        request.setData(data);

        videoRequest.setAuth(auth);
        videoRequest.setRequest(request);
        videoRequest.setService("addvideo");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<AddVideoResponse> responseCall = apiInterface.addVideo(videoRequest);
        responseCall.enqueue(new Callback<AddVideoResponse>() {
            @Override
            public void onResponse(Call<AddVideoResponse> call, Response<AddVideoResponse> response) {
//                //Log.e("TAG", "onResponse: AddVideo :" + response.code());
//                //Log.e("TAG", "onResponse: AddVideo :" + response.isSuccessful());
//                //Log.e("TAG", "onResponse: AddVideo :" + response.message().toString());
//                //Log.e("TAG", "onResponse: AddVideo :" + response.errorBody());
//                //Log.e("TAG", "onResponse: AddVideo :" + response.raw().request().url());
                String mes=response.message();
                //Log.e("TAG", "onResponse: "+mes);
                if (response.isSuccessful()) {
                    if (response.body().getSuccess() == 1) {
                        //Log.e("TAG", "onResponse() returned: " + response.body().getMessage());

                        showUploadVideoSuccess();
                    }
                } else {
//                    Toast.makeText(PostVideoActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
//                    //Log.e("TAG", "onResponse: " + response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<AddVideoResponse> call, Throwable t) {
                //Log.e("TAG", "onFailure: " + t.getMessage());
            }
        });
    }


    private void showUploadVideoSuccess() {
        et_hastag.setText("");
        et_desc.setText("");



        Toast toast = Toast.makeText(PostVideoActivity.this, "Uploaded Successfully.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

//                LinearLayout layout = findViewById(R.id.rlProgress);
//                layout.setVisibility(View.GONE);
//
                close.setVisibility(View.VISIBLE);
                goback.setVisibility(View.GONE);
                RelativeLayout linearLayout = findViewById(R.id.linlay_video_upload);
                linearLayout.setVisibility(View.VISIBLE);
//
                LinearLayout uploadDone = findViewById(R.id.linlay_upload_done);
                uploadDone.setVisibility(View.VISIBLE);

                LinearLayout ll_post = findViewById(R.id.ll_post);
                ll_post.setVisibility(View.GONE);
//

                Button btnUploadAnother = findViewById(R.id.btn_upload_another);
                btnUploadAnother.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RelativeLayout linearLayout = findViewById(R.id.linlay_video_upload);
                        linearLayout.setVisibility(View.VISIBLE);
                        close.setVisibility(View.GONE);
                        goback.setVisibility(View.VISIBLE);
                        LinearLayout uploadDone = findViewById(R.id.linlay_upload_done);
                        uploadDone.setVisibility(View.GONE);
                        LinearLayout ll_post = findViewById(R.id.ll_post);
                        ll_post.setVisibility(View.VISIBLE);
                        imagePath = "";
                        startActivityForResult(new Intent(PostVideoActivity.this, VideoRecorderActivity.class), CAMERA);
                        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                        finish();
                    }
                });

            }
        }, 2000);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }




    // when the video is uploading successfully it will restart the appliaction
    /*@Override
    public void ShowResponce(final String responce) {

        Toast.makeText(Post_Video_A.this, responce, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));
        finishAffinity();

    }*/


    // this is importance for binding the service to the activity
    /*Upload_Service mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            Upload_Service.LocalBinder binder = (Upload_Service.LocalBinder) service;
            mService = binder.getService();

            mService.setCallbacks(Post_Video_A.this);



        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };*/


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Stop_Service();
    }


    // this function will stop the the ruuning service
    public void Stop_Service(){

        /*serviceCallback=this;

        Upload_Service mService = new Upload_Service(serviceCallback);
        if (Functions.isMyServiceRunning(this,mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("stopservice");
            startService(mServiceIntent);

        }*/


    }


    @Override
    public void onProgressUpdate(int percentage) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onUploadStart() {

    }

    @Override
    public void onImageProgressUpdate(int percentage) {

    }

    @Override
    public void onIUploadError() {

    }

    @Override
    public void onIUploadFinish() {

    }

    @Override
    public void onIUploadStart() {

    }
}

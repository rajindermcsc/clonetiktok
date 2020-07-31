package com.tingsic.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.tingsic.Utils.PathFromUri;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;
import com.tingsic.Helper.Functions;
import com.tingsic.R;
import com.tingsic.Utils.Variables;
import com.tingsic.View.progressbar.ProgressBarListener;
import com.tingsic.View.progressbar.SegmentedProgressBar;
import com.tingsic.async.MergeVideoAudio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VideoRecorderActivity extends AppCompatActivity implements View.OnClickListener {

    CameraView cameraView;

    int number = 0;

    ArrayList<String> videopaths = new ArrayList<>();

    ImageButton record_image;
    ImageButton done_btn;
    boolean is_recording = false;
    boolean is_flash_on = false;

    ImageButton flash_btn;

    SegmentedProgressBar video_progress;

    LinearLayout camera_options;

    ImageButton rotate_camera,pick_gallery;

    public static int Sounds_list_Request_code = 1;
    TextView add_sound_txt;
    public static String Selected_sound_id = "null";


    int sec_passed = 0;
    private static final int SELECT_VIDEO = 300;
    private static final int VIDEO_CAPTURE = 400;
    String videoPath,imagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Hide_navigation();
        setContentView(R.layout.activity_video_recoder);
        Log.e("TAG", "onCreate: "+Variables.root);

        initView();
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void initView() {

        sec_passed = 0;
        Selected_sound_id = "null";

        cameraView = findViewById(R.id.camera);
        camera_options = findViewById(R.id.camera_options);


        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
            }

            @Override
            public void onError(CameraKitError cameraKitError) {
            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });


        record_image = findViewById(R.id.record_image);


        done_btn = findViewById(R.id.done);
        done_btn.setEnabled(false);
        done_btn.setOnClickListener(this);


        rotate_camera = findViewById(R.id.rotate_camera);
        pick_gallery = findViewById(R.id.pick_gallery);
        rotate_camera.setOnClickListener(this);
        pick_gallery.setOnClickListener(this);
        flash_btn = findViewById(R.id.flash_camera);
        flash_btn.setOnClickListener(this);

        findViewById(R.id.Goback).setOnClickListener(this);

        add_sound_txt = findViewById(R.id.add_sound_txt);
        add_sound_txt.setOnClickListener(this);


        // this is code hold to record the video
        final Timer[] timer = {new Timer()};
        pick_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a Video "), SELECT_VIDEO);
            }
        });


        record_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    timer[0] = new Timer();

                    timer[0].schedule(new TimerTask() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!is_recording)
                                        Start_or_Stop_Recording();
                                }
                            });

                        }
                    }, 200);


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    timer[0].cancel();
                    if (is_recording) {
                        Start_or_Stop_Recording();
                    }
                }
                return false;
            }

        });


        video_progress = findViewById(R.id.video_progress);
        video_progress.enableAutoProgressView(18000);
        video_progress.setDividerColor(Color.WHITE);
        video_progress.setDividerEnabled(true);
        video_progress.setDividerWidth(4);
        video_progress.setShader(new int[]{Color.CYAN, Color.CYAN, Color.CYAN});

        video_progress.SetListener(new ProgressBarListener() {
            @Override
            public void TimeinMill(long mills) {
                sec_passed = (int) (mills / 1000);

                if (sec_passed > 17) {
                    Start_or_Stop_Recording();
                }

            }
        });


    }




    // if the Recording is stop then it we start the recording
    // and if the mobile is recording the video then it will stop the recording
    public void Start_or_Stop_Recording() {

        if (!is_recording && sec_passed < 18) {
            number = number + 1;

            is_recording = true;

            File file = new File(Variables.root + "/" + "myvideo" + (number) + ".mp4");
            videopaths.add(Variables.root + "/" + "myvideo" + (number) + ".mp4");
            cameraView.captureVideo(file);
            


            if (audio != null) {
                audio.start();
            }


            video_progress.resume();


            done_btn.setBackgroundResource(R.drawable.ic_not_done);
            done_btn.setEnabled(false);

            record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_yes));

            camera_options.setVisibility(View.GONE);
            add_sound_txt.setClickable(false);
            rotate_camera.setVisibility(View.GONE);

        } else if (is_recording) {

            is_recording = false;

            video_progress.pause();
            video_progress.addDivider();

            if (audio != null) {
                audio.pause();
            }
            cameraView.stopVideo();


            if (sec_passed > 5) {
                done_btn.setBackgroundResource(R.drawable.ic_done);
                done_btn.setEnabled(true);
            }

            record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_no));
            camera_options.setVisibility(View.VISIBLE);

        } else if (sec_passed > 17) {
            Functions.Show_Alert(this, "Alert", "Video only can be a 18s.");
        }


    }


    // this will apped all the videos parts in one  fullvideo
    private boolean append() {
        Log.e("TAG", "append: "+videopaths.toString());
        final ProgressDialog progressDialog = new ProgressDialog(VideoRecorderActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    public void run() {

                        progressDialog.setMessage("Please wait..");
                        progressDialog.show();
                    }
                });

                ArrayList<String> video_list = new ArrayList<>();

                for (int i = 0; i < videopaths.size(); i++) {

                    File file = new File(videopaths.get(i));

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(VideoRecorderActivity.this, Uri.fromFile(file));
                    String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                    boolean isVideo = "yes".equals(hasVideo);

                    if (isVideo && file.length() > 3000) {
                        Log.d("resp", videopaths.get(i));
                        video_list.add(videopaths.get(i));
                    }

                }


                try {

                    Movie[] inMovies = new Movie[video_list.size()];

                    for (int i = 0; i < video_list.size(); i++) {
                        Log.e("resp", videopaths.get(0));
                        inMovies[i] = MovieCreator.build(video_list.get(i));
                    }


                    List<Track> videoTracks = new LinkedList<Track>();
                    List<Track> audioTracks = new LinkedList<Track>();
                    for (Movie m : inMovies) {
                        for (Track t : m.getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    }
                    Movie result = new Movie();
                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }


                    Container out = new DefaultMp4Builder().build(result);
                    String outputFilePath = null;
                    if (audio != null) {
                        outputFilePath = Variables.outputfile;
                    } else {
                        outputFilePath = Variables.outputfile2;
                    }
                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                    out.writeContainer(fos.getChannel());
                    fos.close();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();

                            if (audio != null)
                                Merge_withAudio();
                            else {
                                Go_To_preview_Activity("captured", videoPath);
                            }

                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "runex: "+e.getMessage());
                }
            }
        }).start();


        return true;
    }

    private boolean append_() {

        Log.e("TAG", "append: "+videopaths.toString());
        final ProgressDialog progressDialog = new ProgressDialog(VideoRecorderActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    public void run() {

                        progressDialog.setMessage("Please wait..");
                        progressDialog.show();
                    }
                });

                ArrayList<String> video_list = new ArrayList<>();

                for (int i = 0; i < videopaths.size(); i++) {

                    File file = new File(videopaths.get(i));

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(VideoRecorderActivity.this, Uri.fromFile(file));
                    String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                    boolean isVideo = "yes".equals(hasVideo);

                    if (isVideo && file.length() > 3000) {
                        Log.d("resp", videopaths.get(i));
                        video_list.add(videopaths.get(i));
                    }

                }


                try {

                    Movie[] inMovies = new Movie[video_list.size()];

                    for (int i = 0; i < video_list.size(); i++) {
                        Log.e("resp", videopaths.get(0));
                        inMovies[i] = MovieCreator.build(video_list.get(i));
                    }


                    List<Track> videoTracks = new LinkedList<Track>();
                    List<Track> audioTracks = new LinkedList<Track>();
                    for (Movie m : inMovies) {
                        for (Track t : m.getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    }
                    Movie result = new Movie();
                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }


                    Container out = new DefaultMp4Builder().build(result);
                    String outputFilePath = null;
                    if (audio != null) {
                        outputFilePath = Variables.outputfile;
                    } else {
                        outputFilePath = Variables.outputfile2;
                    }
                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                    out.writeContainer(fos.getChannel());
                    fos.close();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();

                            if (audio != null)
                                Merge_withAudio();
                            else {
                                Go_To_preview_Activity("captured", videoPath);
                            }

                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "runex: "+e.getMessage());
                }
            }
        }).start();


        return true;
//        Log.e("TAG", "append: "+videopaths.toString());
//        final ProgressDialog progressDialog = new ProgressDialog(VideoRecorderActivity.this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
//
//                        progressDialog.setMessage("Please wait..");
//                        progressDialog.show();
//                    }
//                });
//
//                ArrayList<String> video_list = new ArrayList<>();
//
//
//                File file = new File(videopaths.get(0));
//
//                    if (file.length() > 3000) {
//                        Log.e("resp", videopaths.get(0));
//                        video_list.add(videopaths.get(0));
//                    }
//
//
//
//                try {
//
//                    Movie[] inMovies = new Movie[video_list.size()];
//
//                    inMovies[0] = MovieCreator.build(video_list.get(0));
//
//
//
//                    Movie result = new Movie();
//
//
//
//                    Container out = new DefaultMp4Builder().build(result);
//                    String outputFilePath = null;
//                    if (audio != null) {
//                        outputFilePath = Variables.outputfile;
//                    } else {
//                        outputFilePath = Variables.outputfile2;
//                    }
//                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
//                    out.writeContainer(fos.getChannel());
//                    fos.close();
//
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            progressDialog.dismiss();
//
//                            if (audio != null)
//                                Merge_withAudio();
//                            else {
//                                Go_To_preview_Activity();
//                            }
//
//                        }
//                    });
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.e("TAG", "runexx: "+e.getMessage());
//                }
//            }
//        }).start();
//
//
//        return true;
    }


    // this will add the select audio with the video
    public void Merge_withAudio() {

        String root = Environment.getExternalStorageDirectory().toString();
        String audio_file;
        audio_file = root + "/" + Variables.SelectedAudio;

        String video = root + "/" + "output.mp4";
        String finaloutput = root + "/" + "output2.mp4";

        MergeVideoAudio merge_video_audio = new MergeVideoAudio(VideoRecorderActivity.this);
        merge_video_audio.doInBackground(audio_file, video, finaloutput);

    }

    public void RotateCamera() {
        cameraView.toggleFacing();
    }


    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rotate_camera:
                RotateCamera();
                break;

            case R.id.done:
                append();
                break;

            case R.id.record_image:
                Start_or_Stop_Recording();
                break;

            case R.id.flash_camera:

                if (is_flash_on) {
                    is_flash_on = false;
                    cameraView.setFlash(0);
                    flash_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));

                } else {
                    is_flash_on = true;
                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                    flash_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                }

                break;

            case R.id.Goback:
                onBackPressed();
                break;

            case R.id.add_sound_txt:
                Intent intent = new Intent(this, SoundListActivity.class);
                startActivityForResult(intent, Sounds_list_Request_code);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        initView();
        onResume();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAGVideo", "onActivityResult: "+requestCode);
        if (requestCode == Sounds_list_Request_code) {
            if (data != null) {

                if (data.getStringExtra("isSelected").equals("yes")) {
                    add_sound_txt.setText(data.getStringExtra("sound_name"));
                    Selected_sound_id = data.getStringExtra("sound_id");
                    PreparedAudio();
                }

            }

        }
        if (requestCode == 260) {
            if (resultCode == RESULT_OK) {
                Log.e("TAGVideo", "onActivityResult@: "+requestCode);
                Intent intent = new Intent();
                intent.putExtra("video_path", data.getStringExtra("video_path"));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        if (requestCode == SELECT_VIDEO) {
            Uri videoUri = data.getData();
            videoPath = PathFromUri.getRealPath(this, videoUri);
            Go_To_preview_Activity("selected",videoPath);
//            videopaths=new ArrayList<>();
//            videopaths.add((videoPath));
//            append_();
//            Log.i("TAG", "onActivityResult: Videouri: " + videoUri.toString());
//            Log.e("TAG", "onActivityResult: code req res " + requestCode + resultCode);
//            Log.i("TAG", "onActivityResult: " + videoPath);
//
//            Log.i("TAG", "onActivityResult: Size " + getFileSize(videoUri) + " Bytes");
//
//                /*if (Build.VERSION.SDK_INT <= 26) {
//                    this.videoPath = getPath(videoUri);
//                    Log.i(TAG, "onActivityResult: VideoPath"+videoPath);
//                }
//                else {
//                    File file = new File(videoUri.getPath());//create path from uri
//                    this.videoPath = file.getAbsolutePath();
//                    Log.i(TAG, "onActivityResult: VideoPath"+videoPath);
//                }*/
//
//
//            File thumbfile = new File(getExternalFilesDir(null), "temp.jpeg");
//            try {
//                FileOutputStream stream = new FileOutputStream(thumbfile);
//                Bitmap bitmap = PathFromUri.createVideoThumbnail(this, videoUri);
//                if (bitmap != null) {
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//
//                    imagePath = thumbfile.getPath();
//                }
//                Log.e("TAG", "onActivityResult: Image Path " + imagePath);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

//            if (this.imagePath == null || this.imagePath.isEmpty()) {
//                this.imagePath = getThumbnailPathForLocalFile(videoUri);
//            }
//
//            Log.e("TAG", "onActivityResult: thumbPsth: " + imagePath);
//
//            ivVideoThumnb.setVisibility(View.VISIBLE);
//            ivVideoThumnb.setClickable(false);
//            if (menuClear != null) {
//                menuClear.setVisible(true);
//            }
//
//            Picasso.get().load("file://" + imagePath).memoryPolicy(MemoryPolicy.NO_CACHE).into(ivVideoThumnb);
//
//                /*MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                //use one of overloaded setDataSource() functions to set your data source
//                retriever.setDataSource(this, videoUri);
//                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//                String vw = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//                String vh = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
//                String r = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
//                int width = (int) (Double.parseDouble(vw)/2);
//                int height = (int) (Double.parseDouble(vh)/2);
//                int rotation = (int) (Double.parseDouble(r));
//                if (rotation==0||rotation==180) {
//
//                }
//                Log.i(TAG, "onActivityResult: "+width+" "+height+ " rotation"+rotation);
//                long timeInMillisec = Long.parseLong(time );
//
//                retriever.release();*/
//
//            btnUpload.setVisibility(View.VISIBLE);
//            etHashtag.setVisibility(View.VISIBLE);
//
//            //fixme if you want to compress
//            long size = Long.parseLong(getFileSize(videoUri));
//
//            if (size > 6000000) {
//                File file = new File(getExternalFilesDir(null), "temp.mp4");
//                String currentOutputVideoPath = file.getPath();
//                //todo uncomment this!
//                btnUpload.setVisibility(View.VISIBLE);
//                etHashtag.setVisibility(View.VISIBLE);
//                //showLargeFileDialog(this.videoPath,currentOutputVideoPath);
//                if (size > 15000000) {
//                    showLargeFileDialog(this.videoPath, currentOutputVideoPath, false);
//                } else {
//                    showLargeFileDialog(this.videoPath, currentOutputVideoPath, true);
//                }
//            } else {
//                btnUpload.setVisibility(View.VISIBLE);
//                etHashtag.setVisibility(View.VISIBLE);
//            }

                /*File file = new File(getExternalFilesDir(null),"temp.mp4");
                String currentOutputVideoPath = file.getPath();
                btnUpload.setVisibility(View.VISIBLE);
                etHashtag.setVisibility(View.VISIBLE);
                compress(videoPath,currentOutputVideoPath);
                //showLargeFileDialog(this.videoPath,currentOutputVideoPath);*/

                /*Button btnCompress = findViewById(R.id.btn_video_compress);
                btnCompress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //String cmd = "-y -i " + currentInputVideoPath + " -preset ultrafast -b 600k " + currentOutputVideoPath; almost 4 min
                        //String cmd = "-y -i " + currentInputVideoPath + " -c:v libx264 -crf 40 -preset ultrafast " + currentOutputVideoPath;

                        //execCommand(currentOutputVideoPath,cmd);

                        //compress(videoPath,currentOutputVideoPath);
                    }
                });*/
        }
    }


    private String getFileSize(Uri videoUri) {
        Cursor cursor = null;

        try {
            String[] column = {MediaStore.Video.Media.SIZE};

            cursor = getContentResolver().query(videoUri, column, null, null, null);

            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    // this will play the sound with the video when we select the audio
    MediaPlayer audio;

    public void PreparedAudio() {
        File file = new File(Variables.root + "/" + Variables.SelectedAudio);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(Variables.root + "/" + Variables.SelectedAudio);
                audio.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {

            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
            cameraView.stop();

        } catch (Exception e) {

        }
    }


    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Are you Sure? if you Go back you can't undo this action")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        DeleteFile();
                        finish();
                        overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                    }
                }).show();

    }


    public void Go_To_preview_Activity(String captured, String videoPath) {
        Log.e("TAG", "Go_To_preview_Activity: "+videoPath);

        if (captured.equalsIgnoreCase("captured")) {
            Intent intent = new Intent(this, PreviewVideoActivity.class);
            intent.putExtra("type","captured");
            //intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            //startActivity(intent);
            startActivityForResult(intent, 260);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
        else {
            Intent intent = new Intent(this, PreviewVideoActivity.class);
            intent.putExtra("video_path", this.videoPath);
            intent.putExtra("type","selected");
            //intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            //startActivity(intent);
            startActivityForResult(intent, 260);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }


    // this will delete all the video parts that is create during priviously created video
    int delete_count = 0;

    public void DeleteFile() {
        delete_count++;
        File file = new File(Variables.root + "/" + "myvideo" + (delete_count) + ".mp4");
        if (file.exists()) {
            file.delete();
            DeleteFile();
        }
    }


    // this will hide the bottom mobile navigation controll
    public void Hide_navigation() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

    }


    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


}
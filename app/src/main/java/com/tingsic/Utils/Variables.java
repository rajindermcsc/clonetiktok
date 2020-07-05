package com.tingsic.Utils;

import android.os.Environment;

public class Variables {

    //todo try with mp3
    public static String SelectedAudio="SelectedAudio.aac";

    public static String root= Environment.getExternalStorageDirectory().toString();

    public static String outputfile=root + "/output.mp4";
    public static String outputfile2=root + "/output2.mp4";

    public static String output_filter_file=root + "/output-filtered.mp4";
    public static String output_filter_file2=root + "/output-filtered2.mp4";


    public static String file_provider_path="com.tingsic.provider";

}

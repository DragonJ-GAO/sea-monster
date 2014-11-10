package com.sea_monster.core.utils;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static String getExtendFileNameNoPoint(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }else{
            int pointStart = path.lastIndexOf(".");
            if(pointStart!=-1){
                return path.substring(pointStart+1);
            }
            return "";
        }
    }

    public static String getFileNameWithoutExtName(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }else{
            int pointStart = path.lastIndexOf(".");
            if(pointStart!=-1){
                return path.substring(0,pointStart);
            }
            return path;
        }
    }


    final static String TAG = "file_sys";
	public static final String NOMEDIA = ".nomedia";

	public static final void createDirectory(File storageDirectory, boolean nomedia) {
		if (!storageDirectory.exists()) {
			Log.d(TAG, "Trying to create storageDirectory: " + String.valueOf(storageDirectory.mkdirs()));

			Log.d(TAG, "Exists: " + storageDirectory + " " + String.valueOf(storageDirectory.exists()));
			Log.d(TAG, "State: " + Environment.getExternalStorageState());
			Log.d(TAG, "Isdir: " + storageDirectory + " " + String.valueOf(storageDirectory.isDirectory()));
			Log.d(TAG, "Readable: " + storageDirectory + " " + String.valueOf(storageDirectory.canRead()));
			Log.d(TAG, "Writable: " + storageDirectory + " " + String.valueOf(storageDirectory.canWrite()));
			File tmp = storageDirectory.getParentFile();
			Log.d(TAG, "Exists: " + tmp + " " + String.valueOf(tmp.exists()));
			Log.d(TAG, "Isdir: " + tmp + " " + String.valueOf(tmp.isDirectory()));
			Log.d(TAG, "Readable: " + tmp + " " + String.valueOf(tmp.canRead()));
			Log.d(TAG, "Writable: " + tmp + " " + String.valueOf(tmp.canWrite()));
			tmp = tmp.getParentFile();
			Log.d(TAG, "Exists: " + tmp + " " + String.valueOf(tmp.exists()));
			Log.d(TAG, "Isdir: " + tmp + " " + String.valueOf(tmp.isDirectory()));
			Log.d(TAG, "Readable: " + tmp + " " + String.valueOf(tmp.canRead()));
			Log.d(TAG, "Writable: " + tmp + " " + String.valueOf(tmp.canWrite()));
		}
		if (nomedia) {
			File nomediaFile = new File(storageDirectory, NOMEDIA);
			if (!nomediaFile.exists()) {
				try {
					Log.d(TAG, "Created file: " + nomediaFile + " " + String.valueOf(nomediaFile.createNewFile()));
				} catch (IOException e) {
					Log.d(TAG, "Unable to create .nomedia file for some reason.", e);
//					throw new IllegalStateException("Unable to create nomedia file.");
				}
			}

			if (!(storageDirectory.isDirectory() && nomediaFile.exists())) {
                Log.d(TAG, "Unable to create .nomedia file for some reason.");
//                throw new RuntimeException("Unable to create storage directory and nomedia file.");
			}
		}
	}

    public static void Channel(File f1,File f2) throws Exception{
        int length=512*1024;
        FileInputStream in=new FileInputStream(f1);
        FileOutputStream out=new FileOutputStream(f2);
        FileChannel inC=in.getChannel();
        FileChannel outC=out.getChannel();
        ByteBuffer b=null;
        while(true){
            if(inC.position()==inC.size()){
                inC.close();
                outC.close();
                return;
            }
            if((inC.size()-inC.position())<length){
                length=(int)(inC.size()-inC.position());
            }else
                length = 512*1024;
            b=ByteBuffer.allocateDirect(length);
            inC.read(b);
            b.flip();
            outC.write(b);
            outC.force(false);
        }
    }
}

package it.univaq.app.carapp.Utility;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class FileUtility {
    public static String MAIN_FILE_TRACKINGS = "fileWithAllTrackings";

    public static void writeTracking(Context context, String data){
        File dir = new File(getDirectory(context));
        File file = new File(dir, MAIN_FILE_TRACKINGS);
        try {
            if (file.createNewFile()){
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data.getBytes());
                fos.flush();
                fos.close();
            }
            else if (!file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file, true);
                String stringToWrite = "\n"+data;
                fos.write(stringToWrite.getBytes());
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] readTrackings(Context context){
        File dir = new File(getDirectory(context));
        File file = new File(dir, MAIN_FILE_TRACKINGS);
        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[(int) file.length()];
                fis.read(buffer);
                String data = new String(buffer);
                file.delete();
                fis.close();
                return data.split("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getDirectory(Context context) {
        return context.getExternalFilesDir("files").getAbsolutePath();
    }
}

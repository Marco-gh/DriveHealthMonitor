package it.univaq.app.carapp.Utility;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import it.univaq.app.carapp.Model.Session;

public class FileUtility {

    public static boolean writeSessionFile(Context context, String filename, byte[] data) {
        File dir = new File(getDirectory(context));
        File file = new File(dir, filename);
        if (file.exists()) file.delete();
        try {
            if (file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                fos.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Session readSessionFIle(String filename) {
        File file = new File(filename);
        if(file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);

                byte[] buffer = new byte[(int) file.length()];
                fis.read(buffer);

                String data = new String(buffer);
                Gson gson = new Gson();
                Session session = gson.fromJson(data, Session.class);
                return session;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void deleteSessionFile(Context context, String filename) {
        File dir = new File(getDirectory(context));
        File file = new File(dir, filename);
        if(file.exists()) file.delete();
    }

    private static String getDirectory(Context context) {
        return context.getExternalFilesDir("files").getAbsolutePath();
    }
}
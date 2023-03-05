package it.univaq.app.carapp.Utility;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import it.univaq.app.carapp.Model.Tracking;

public class FileUtility {
    public static String NAME_MAIN_FILE = "mainTrackingFile";

    /**
     * Memorizza il tracking su un file e aggiorna il MainFile con tutti i nomi
     */
    public static void writeTrackingFile(Context context, String filename, byte[] data) {
        File dir = new File(getDirectory(context));
        File mainTrackingFile = new File(dir, NAME_MAIN_FILE);
        try{
            mainTrackingFile.createNewFile();

            FileInputStream fis = new FileInputStream(mainTrackingFile);
            byte[] buffer = new byte[(int) mainTrackingFile.length()];
            fis.read(buffer);
            String s = new String(buffer);
            fis.close();
            String toWrite = filename+"\n"+s;

            FileOutputStream fos = new FileOutputStream(mainTrackingFile);
            fos.write(toWrite.getBytes());

            fos.flush();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        File file = new File(dir, filename);
        if (file.exists()) file.delete();
        try {
            if (file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void WriteMainTrackingFile(Context context, ArrayList<String> listOFTracking){
        File dir = new File(getDirectory(context));
        File maintrackingFile = new File(dir, NAME_MAIN_FILE);
        try{
            if(maintrackingFile.exists()) maintrackingFile.delete();

            FileOutputStream fos = new FileOutputStream(maintrackingFile);
            for (String s : listOFTracking) {
                String toWrite = s+"\n";
                fos.write(toWrite.getBytes());
                fos.flush();
                fos.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Tracking readTrackingFile(Context context, String filename) {
        File file = new File(filename);
        if(file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);

                byte[] buffer = new byte[(int) file.length()];
                fis.read(buffer);

                String data = new String(buffer);
                Gson gson = new Gson();
                Tracking tracking = gson.fromJson(data, Tracking.class);
                return tracking;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(!file.exists()){
            //se esite sul main file per un qualche errore rimuovilo da lì
            File dir = new File(getDirectory(context));
            File mainTrackingFile = new File(dir, NAME_MAIN_FILE);
            try {
                if(mainTrackingFile.exists()){
                    FileInputStream fis = new FileInputStream(mainTrackingFile);
                    FileOutputStream fos = new FileOutputStream(mainTrackingFile);

                    byte[] buffer = new byte[(int) mainTrackingFile.length()];
                    fis.read(buffer);
                    String data = new String(buffer);
                    String[] listOfFilesArray = data.split("\n");
                    ArrayList<String> listOfFilesList = new ArrayList<>();
                    for (String s : listOfFilesArray) {
                        if (Objects.equals(s, filename)){
                            listOfFilesList.remove(s);
                        }
                        else{
                            fos.write(s.getBytes());
                            fos.flush();
                            fos.close();
                        }
                    }
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *
     * @return A list with every tracking in memory
     */
    public static ArrayList<String> getNameFiles(Context context){
        File dir = new File(getDirectory(context));
        File mainTrackingFile = new File(dir, NAME_MAIN_FILE);
        try {
            if(mainTrackingFile.exists()){
                FileInputStream fis = new FileInputStream(mainTrackingFile);
                byte[] buffer = new byte[(int) mainTrackingFile.length()];
                fis.read(buffer);
                String data = new String(buffer);
                String[] listOfFilesArray = data.split("\n");
                ArrayList<String> listOfFilesList = new ArrayList<>();
                for (String s : listOfFilesArray) {
                    listOfFilesList.add(s);
                }
                return listOfFilesList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteTrackingFile(Context context, String filename) {
        File dir = new File(getDirectory(context));
        File file = new File(dir, filename);
        if(file.exists()) file.delete();
    }

    private static String getDirectory(Context context) {
        return context.getExternalFilesDir("files").getAbsolutePath();
    }
}
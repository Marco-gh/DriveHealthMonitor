package it.univaq.app.carapp.Utility;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import it.univaq.app.carapp.Model.Session;

public class FileUtility {
    public static String NAME_MAIN_FILE = "mainSessionFile";

    /**
     * Memorizza la sessione su un file e aggiorna il MainFile con tutti i nomi
     */
    public static void writeSessionFile(Context context, String filename, byte[] data) {
        //Aggiorna il main file con i nomi
        File dir = new File(getDirectory(context));
        File mainSessionFile = new File(dir, NAME_MAIN_FILE);
        try{
            mainSessionFile.createNewFile();

            FileInputStream fis = new FileInputStream(mainSessionFile);
            byte[] buffer = new byte[(int) mainSessionFile.length()];
            fis.read(buffer);
            String s = new String(buffer);
            fis.close();
            String toWrite = filename+"\n"+s;

            FileOutputStream fos = new FileOutputStream(mainSessionFile);
            fos.write(toWrite.getBytes());

            fos.flush();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //Scrive il nuovo file in memoria
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

    public static void WriteMainSessionFile(Context context, ArrayList<String> listOFSessions){
        File dir = new File(getDirectory(context));
        File mainSessionFile = new File(dir, NAME_MAIN_FILE);
        try{
            if(mainSessionFile.exists()) mainSessionFile.delete();

            FileOutputStream fos = new FileOutputStream(mainSessionFile);
            for (String s : listOFSessions) {
                String toWrite = s+"\n";
                fos.write(toWrite.getBytes());
                fos.flush();
                fos.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Session readSessionFile(Context context, String filename) {
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
        else if(!file.exists()){
            //se esite sul main file per un qualche errore rimuovilo da lì
            File dir = new File(getDirectory(context));
            File mainSessionFile = new File(dir, NAME_MAIN_FILE);
            try {
                if(mainSessionFile.exists()){
                    FileInputStream fis = new FileInputStream(mainSessionFile);
                    FileOutputStream fos = new FileOutputStream(mainSessionFile);

                    byte[] buffer = new byte[(int) mainSessionFile.length()];
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
     * @return A list with every session in memory
     */
    public static ArrayList<String> getNameFiles(Context context){
        File dir = new File(getDirectory(context));
        File mainSessionFile = new File(dir, NAME_MAIN_FILE);
        try {
            if(mainSessionFile.exists()){
                FileInputStream fis = new FileInputStream(mainSessionFile);
                byte[] buffer = new byte[(int) mainSessionFile.length()];
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

    public static void deleteSessionFile(Context context, String filename) {
        File dir = new File(getDirectory(context));
        File file = new File(dir, filename);
        if(file.exists()) file.delete();
    }

    private static String getDirectory(Context context) {
        return context.getExternalFilesDir("files").getAbsolutePath();
    }
}
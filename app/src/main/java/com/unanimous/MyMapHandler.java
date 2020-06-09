package com.unanimous;

import android.content.Context;
import android.util.Log;

import com.unanimous.MyDB.MyDBHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import static com.unanimous.MyDB.MyDBSchema.COMN_INX;
import static com.unanimous.MyDB.MyDBSchema.C_NM_INX;
import static com.unanimous.MyDB.MyDBSchema.C_TP_INX;
import static com.unanimous.MyDB.MyDBSchema.NAME_INX;
import static com.unanimous.MyDB.MyDBSchema.TABLES;
import static com.unanimous.MyDB.MyDBSchema.T_DATA_INX;
import static com.unanimous.MyDB.MyDBSchema.T_PROP_INX;

public class MyMapHandler {
    private boolean debug = true;
    public void debugging(String text) {
        if (debug) Log.e("MyMapHandler", text);
    }

    private Context mContext;
    MyDBHandler myDBHandler;
    private final String SAPARATOR = ",";

    // Map
    private static ArrayList<float[]> map_vec;
    private static ArrayList<float[]> map_ptr;
    public static ArrayList<float[]> getMap_vec() { return map_vec; }
    public static ArrayList<float[]> getMap_ptr() { return map_ptr; }
    public static int getMap_cnt() { return map_ptr.size() + map_vec.size(); }
    public void clear() { map_vec.clear(); map_ptr.clear(); }
    public void clear_ptr() { map_ptr.clear(); }

    public MyMapHandler(Context context, MyDBHandler myDBHandler) {
        this.mContext = context;
        this.myDBHandler = myDBHandler;
        this.map_vec = new ArrayList<>();
        this.map_ptr = new ArrayList<>();
        initPath();
    }

    private void lprint(String msg) {
        debugging(msg);
    }

    private void lprint(String msg, String errmsg) {
        debugging(msg+", "+errmsg);
    }

    public boolean isMapAvailable() {
        return map_vec.size() > 0;
    }

    public boolean isMapExists(String mapname) {
        ArrayList<String> result = myDBHandler.select(
                TABLES[T_PROP_INX][NAME_INX][0],
                TABLES[T_PROP_INX][COMN_INX][2],
                TABLES[T_DATA_INX][COMN_INX][C_NM_INX]+"="+mapname
        );
        return result.size() > 0;
    }

    public void removeMapFile(String filename) {
        File file = new File(mContext.getFilesDir().getAbsolutePath()+"/"+filename);
        try {
            if (file.exists()) file.delete();
            else debugging(filename + "not exists.");
        } catch (Exception e) {
            lprint("Remove Fail. "+ e.getMessage());
        }
    }

    public void removeMapDB(String mapname) {
        // Delete exist map or path by map id
        myDBHandler.delete(TABLES[T_PROP_INX][NAME_INX][NAME_INX],
                TABLES[T_PROP_INX][COMN_INX][C_NM_INX]+"="+mapname);
        myDBHandler.delete(TABLES[T_DATA_INX][NAME_INX][NAME_INX],
                TABLES[T_DATA_INX][COMN_INX][C_NM_INX]+"="+mapname);
    }

    public boolean loadMapRawToFile(int rawid, String filename) {
        InputStream is = mContext.getResources().openRawResource(rawid);
        File file = new File(mContext.getFilesDir().getAbsolutePath()+"/"+filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line="";
        FileWriter fw;
        try {
            removeMapFile(filename);
            file.createNewFile();
            fw = new FileWriter(file);
        } catch (FileNotFoundException fnfe) {
            lprint(filename+" is not found", fnfe.getMessage());
            return false;
        } catch (IOException ioe) {
            lprint(filename+" couldn't be created", ioe.getMessage());
            return false;
        }
        try {
            while((line = reader.readLine()) != null){ fw.write(line+"\n"); }
            reader.close();
            is.close();
            fw.close();
        } catch (IOException ioe) {
            lprint(filename+" couldn't be writen for file", ioe.getMessage());
            return false;
        }
        debugging("Create file success "+filename);
        return true;
    }

    public boolean loadMapArrayToDB(ArrayList<String> arr, String filename, String maptype) {
        // First, getting map propertise : map id, map name, map type
        debugging("map file propertise, "+filename+", "+maptype);
        String propertise[] = {"'"+filename+"'", "'"+maptype+"'"};

        // Remove existed map in file
        removeMapDB(propertise[C_NM_INX]);

        // Insert map properties into DB
        myDBHandler.insert(TABLES[T_PROP_INX][NAME_INX][NAME_INX], propertise);

        Iterator<String> iter = arr.iterator();
        // Seconds, Map Data : map id, map coords
        while (iter.hasNext()) {
            myDBHandler.insert(TABLES[T_DATA_INX][NAME_INX][NAME_INX],
                    new String[]{propertise[C_NM_INX], iter.next()});
        }
        return true;
    }

    public boolean loadMapFileToDB(String filename, String maptype) {
        File file = new File(mContext.getFilesDir(), filename);
        debugging("Get Map absolute path, "+file.getAbsolutePath());

        FileInputStream fis;
        BufferedReader br;
        String line = "";

        // Get file access
        try {
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
        } catch (FileNotFoundException fnfe) {
            lprint(filename+" is not found", fnfe.getMessage());
            return false;
        }

        // Read coordinates from file and save to db
        try {
            // First, getting map propertise : map id, map name, map type
            debugging("map file propertise, "+filename+", "+maptype);
            String propertise[] = {"'"+filename+"'", "'"+maptype+"'"};

            // Remove existed map in file
            removeMapDB(propertise[C_NM_INX]);

            // Insert map properties into DB
            myDBHandler.insert(TABLES[T_PROP_INX][NAME_INX][NAME_INX], propertise);

            // Seconds, Map Data : map id, map coords
            while ((line=br.readLine()) != null) {
                myDBHandler.insert(TABLES[T_DATA_INX][NAME_INX][NAME_INX],
                        new String[]{propertise[C_NM_INX], line});
            }
        } catch (IOException ioe) {
            lprint("Readline error", ioe.getMessage());
        }
        return true;
    }

    public void loadMapDBToArray(String mapname) {
        // Get maptype into local maptype
        try {
            String maptype = myDBHandler.select(
                    TABLES[T_PROP_INX][NAME_INX][NAME_INX],
                    TABLES[T_PROP_INX][COMN_INX][C_TP_INX],
                    TABLES[T_DATA_INX][COMN_INX][C_NM_INX]+"="+mapname
            ).get(0);
            debugging("Map Type : "+maptype);

            // Get map_vec into local map_vec
            if(maptype.equals("v")) {
                Iterator<float[]> iter = myDBHandler.selectmap(TABLES[T_DATA_INX][COMN_INX][C_NM_INX]+"="+mapname).iterator();
                while (iter.hasNext()) {
                    map_vec.add(iter.next());
                }
            }

            // Get map_vec into local map_vec
            if(maptype.equals("p")) {
                Iterator<float[]> iter = myDBHandler.selectmap(TABLES[T_DATA_INX][COMN_INX][C_NM_INX]+"="+mapname).iterator();
                float[] item;
                while (iter.hasNext()) {
                    item = iter.next();
                    map_ptr.add(new float[]{item[0], item[1]});
                }
            }
        }
        catch (IndexOutOfBoundsException ioobe) {
            debugging("There is no map name : "+mapname);
            return;
        }
    }

    public void initPath() {
        File file = mContext.getFilesDir();
        if (!file.exists()) {
            if (file.mkdirs()) { debugging("We create directories, "+file.getAbsolutePath()); }
            else { debugging("We couldn't create directories, "+file.getAbsolutePath()); }
        }
        else { debugging("There is already exist path, "+file.getAbsolutePath()); }
    }
}

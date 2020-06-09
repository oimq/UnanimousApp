package com.unanimous.MyDB;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import static com.unanimous.MyDB.MyDBSchema.COMN_INX;
import static com.unanimous.MyDB.MyDBSchema.CONT_INX;
import static com.unanimous.MyDB.MyDBSchema.DATABASE_VERSION;
import static com.unanimous.MyDB.MyDBSchema.DBNAME;
import static com.unanimous.MyDB.MyDBSchema.NAME_INX;
import static com.unanimous.MyDB.MyDBSchema.TABLES;
import static com.unanimous.MyDB.MyDBSchema.TABLE_MAP_DATA;
import static com.unanimous.MyDB.MyDBSchema.TYPE_INX;
import static com.unanimous.MyUtils.str;

public class MyDBHandler extends SQLiteOpenHelper {
    private boolean debug = true;

    public MyDBHandler(Context context, CursorFactory factory) {
        super(context, DBNAME, factory, DATABASE_VERSION);
    }

    public void debugging(String text) {
        if (debug) Log.e("MyDBHandler", text);
    }

    public void run(String query) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(query);
        }
        catch (SQLException se) {
            Log.e("MyDBHandler ", "Run query error, "+se.getMessage());
        }
        catch (Exception e) {
            Log.e("MyDBHandler ", "Run query error, "+e.getMessage());
        }
    }

    public void run(String query, SQLiteDatabase db) {
        try {
            db.execSQL(query);
        }
        catch (SQLException se) {
            Log.e("MyDBHandler ", "Run query error, "+se.getMessage());
        }
        catch (Exception e) {
            Log.e("MyDBHandler ", "Run query error, "+e.getMessage());
        }
    }

    public Cursor runRaw(String query) {
        SQLiteDatabase mydb = getWritableDatabase();
        Cursor cursor = null;
        if (mydb != null) {
            try { cursor = mydb.rawQuery(query, null); }
            catch (SQLException se) { debugging("Run query error, "+se.getMessage()); }
            catch (Exception e) { debugging("Other error, "+e.getMessage()); }
        }
        if (debug) {if (mydb == null) debugging("There are no DB!");}
        return cursor;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        create_tables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        delete_tables(db);
        create_tables(db);
    }

    public void delete_tables(SQLiteDatabase db) {
        for (int tinx = 0; tinx < TABLES.length; tinx++) {
            String query = "DROP TABLE IF EXISTS "+TABLES[tinx][NAME_INX][0];
            debugging("Delete tables, "+"<"+query+">");
            run(query, db);
        }
    }

    public void create_tables(SQLiteDatabase db) {
        // make table's creation text
        for (int tinx = 0; tinx < TABLES.length; tinx++) {
            String query = "CREATE TABLE "+TABLES[tinx][NAME_INX][0]+" (";
            for (int cinx = 0; cinx < TABLES[tinx][COMN_INX].length; cinx++) {
                query += TABLES[tinx][COMN_INX][cinx]+" "+
                         TABLES[tinx][TYPE_INX][cinx]+" "+
                         TABLES[tinx][CONT_INX][cinx]+",";
            }
            query = query.substring(0, query.length()-1)+");";
            debugging("Create Table, <"+query+">");
            run(query, db);
        }
    }

    public void insert(String tablename, String[] values) {
        String query = "INSERT INTO "+tablename+" VALUES (";
        for (String value : values) {query += value+",";}
        query = query.substring(0, query.length()-1)+");";
        debugging("Insert values, <"+query+">");
        run(query);
        return;
    }

    public void select(String tablename) {
        String query = "SELECT * FROM "+tablename+";";
        debugging("select, <"+query+">");
        Cursor cursor = runRaw(query);
        if (cursor == null) {
            debugging("Selection get null!!");
            return;
        }
        if (debug) {
            String result = "Cursor count : "+String.valueOf(cursor.getCount())+", ";
            while(cursor.moveToNext()){
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    result += cursor.getString(i);
                }
                result += " ||| ";
            }
            debugging("Get ["+result+"]");
        }
    }

    /*
     * Special function : select map data (coordinates)
     */
    public ArrayList<float[]> selectmap(String where) {
        String query = "SELECT * FROM "+TABLE_MAP_DATA[NAME_INX][NAME_INX]+" WHERE "+where;
        debugging("select, <"+query+">");
        Cursor cursor = runRaw(query);
        if (cursor == null) { debugging("Selection get null!!"); return null; }
        debugging("Select count : "+String.valueOf(cursor.getCount()));
        ArrayList<float[]> floarr = new ArrayList<>();
        while (cursor.moveToNext()) {
            floarr.add(new float[]{
                    cursor.getFloat(1),cursor.getFloat(2),cursor.getFloat(3),cursor.getFloat(4)
            });
            debugging("Select values : "+str(cursor.getFloat(1))+","+str(cursor.getFloat(2))+","
                    +str(cursor.getFloat(3))+","+str(cursor.getFloat(4)));
        }
        return floarr;
    }

    public ArrayList<String> select(String tablename, String col, String where) {
        String query = "SELECT "+col+" FROM "+tablename+" WHERE "+where+";";
        debugging("select, <"+query+">");
        Cursor cursor = runRaw(query);
        if (cursor == null) { debugging("Selection get null!!"); return null; }
        else {
            debugging("Select count : "+String.valueOf(cursor.getCount()));
            ArrayList<String> strarr = new ArrayList<String>();
            while(cursor.moveToNext()){
                strarr.add(cursor.getString(0));
            }
            return strarr;
        }
    }

    public void delete(String tablename, String where) {
        String query = "DELETE FROM "+tablename+" WHERE "+where+";";
        debugging("delete, <"+query+">");
        run(query);
        return;
    }
}

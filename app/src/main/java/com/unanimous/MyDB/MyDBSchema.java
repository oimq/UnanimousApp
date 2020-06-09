package com.unanimous.MyDB;

public class MyDBSchema {
    public static final String DBNAME = "unanimousdb";
    public static final int DATABASE_VERSION = 42;

    public static final short T_PROP_INX = 0;
    public static final short T_DATA_INX = 1;

    public static final short NAME_INX = 0;
    public static final short COMN_INX = 1;
    public static final short TYPE_INX = 2;
    public static final short CONT_INX = 3;

    public static final short C_NM_INX = 0;
    public static final short C_TP_INX = 1;


    /*
    Map DB Schema,
     - MAP_2D_PROPERTISE
     -- mapid : Map identified descriptive number.
     -- name : Map name.
     -- type : Vector map or point map?
     - MAP_2D_COORDINATES
     -- mapid : Map identified descriptive number.
     -- data : Coordinates data.
     */

    public static final String TABLE_MAP_PROPERTISE[][] = {
            {"MAP_2D_PROPERTISE"},                          // table name
            {"name", "type"},                      // column names
            {"TEXT", "TEXT"},                    // column data types
            {"NOT NULL", "NOT NULL"}         // column constraints
    };

    public static final String TABLE_MAP_DATA[][] = {
            {"MAP_2D_COORDINATES"},
            {"name", "sx", "sy", "ex", "ey"},
            {"TEXT", "FLOAT", "FLOAT", "FLOAT", "FLOAT"},
            {"NOT NULL", "", "", "", ""}
    };

    public static final String TABLES[][][] = {TABLE_MAP_PROPERTISE, TABLE_MAP_DATA};
}

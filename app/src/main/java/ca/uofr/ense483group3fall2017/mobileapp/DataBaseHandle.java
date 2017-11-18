package ca.uofr.ense483group3fall2017.mobileapp;

/**
 Database file for storing search results
 **/
import android.content.Context;
import android.database.sqlite.SQLiteDatabse;
import andriod.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{

    // Name of Data base
    public static final String DATABASE_NAME = "local_device_db";
    // Name of Table
    public static final String TABLE_NAME = "search_result";
    // Name of Columns
    public static final String COL1_NAME = "entry_id";
    public static final String COL2_NAME = "device_found";
    public static final String COL3_NAME = "distance";
    public static final String COL4_NAME = "DateTime";
    public static final String COL5_NAME = "location";

    public DatabaseHelper (Context context){
        super (context , DATABASE_NAME,null , 1);
    }

    @Overide
    public void OnCreate(SQLiteDatabase db)
    {
        string query =" ";
        query = "CREATE TABLE " +TABLE_NAME+ " (entry_id INTEGER PRIMARY KEY AUTOINCREMENT ,device_found VARCHAR(255) distance FLOAT, DateTime DATETIME ,location TEXT)"+
    db.execSQL();
    }

    public void onUpgrade (SQLiteDatabase db, int oldVer, int newVer)
    {

    }


}
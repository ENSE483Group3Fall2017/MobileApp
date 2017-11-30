package ca.uofr.ense483group3fall2017.mobileapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;

/**
 * Created by chenxiaojie on 2017-11-18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Name of Data base
    public static final String DATABASE_NAME = "local_device_db";
    // Name of Table
    public static final String TABLE_NAME = "search_result";
    // Name of Columns
    public static final String COL1_NAME = "entry_id";
    public static final String COL2_NAME = "device_found";
    public static final String COL3_NAME = "distance";
    //public static final String COL4_NAME = "DateTime";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db = this.getWritableDatabase(); // for checking
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (entry_id INTEGER PRIMARY KEY AUTOINCREMENT ,device_found VARCHAR(255), distance DOUBLE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String device_found, double distance){
        SQLiteDatabase db = this.getWritableDatabase(); //connect database
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2_NAME, device_found);
        contentValues.put(COL3_NAME, distance);
        //contentValues.put(COL4_NAME, DateTime);

        long result;
        result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null); //output data from database
        // this Cursor provide random read-write access to the result set returned
        return res;
    }

    public boolean updateData(Editable entry_id, Editable device_found, Editable distance, Editable DateTime, Editable location){
        SQLiteDatabase db = this.getWritableDatabase(); //connect database
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1_NAME, entry_id.toString());
        contentValues.put(COL2_NAME, String.valueOf(device_found));
        contentValues.put(COL3_NAME, String.valueOf(distance));
        //contentValues.put(COL4_NAME, String.valueOf(DateTime));

        db.update(TABLE_NAME, contentValues, "id = ?", new String[] {String.valueOf(entry_id)});

        return true;

    }

    public Integer deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase(); //connect database
        return db.delete(TABLE_NAME, "id = ?", new String[] {id});
    }

}

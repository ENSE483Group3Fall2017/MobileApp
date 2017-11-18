package ca.uofr.ense483group3fall2017.mobileapp;

/**
 Database file for storing search results
 **/
import android.content.Context;
import android.database.sqlite.SQLiteDatabse;
import andriod.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{
    public DatabaseHelper (Context context, String name, SQLiteDatabse.CursorFactory factory, int version){
        super (context , name,factory , version);
    }

    @Overide
    public void OnCreate(SQLiteDatabase db)
    {

    }

    public void onUpgrade (SQLiteDatabase db, int oldVer, int newVer)
    {
        
    }


}
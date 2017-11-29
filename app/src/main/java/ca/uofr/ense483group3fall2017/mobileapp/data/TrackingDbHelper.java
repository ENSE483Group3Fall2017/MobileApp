package ca.uofr.ense483group3fall2017.mobileapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bahram.aliyev on 2017-11-28.
 */

public class TrackingDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TrackindData.db";
    private static final int DATABASE_VERSION = 1;

    public TrackingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_TRACKING_INFO_TABLE = "CREATE TABLE " + TrackingInfoContract.TABLE_NAME + " (" +
                TrackingInfoContract._ID + " CHARACTER(32) PRIMARY KEY," +
                TrackingInfoContract.COLUMN_BATCH_ID + " CHARACTER(32), " +
                TrackingInfoContract.COLUMN_BEACON_ID + " CHARACTER(10) NOT NULL, " +
                TrackingInfoContract.COLUMN_PROXIMITY + " DOUBLE NOT NULL, " +
                TrackingInfoContract.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                "); ";
        final String SQL_TRACKING_BATCH_TABLE= "CREATE TABLE " + TrackingBatchContract.TABLE_NAME + " (" +
                TrackingBatchContract._ID + " CHARACTER(32) PRIMARY KEY," +
                TrackingBatchContract.COLUMN_FRANE_START + " CHARACTER(19) NOT NULL," +
                TrackingBatchContract.COLUMN_FRANE_END + " CHARACTER(19) NOT NULL," +
                TrackingBatchContract.COLUMN_GPS_COORDINATES + " TEXT NOT NULL, " +
                "); ";
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackingInfoContract.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackingBatchContract.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}



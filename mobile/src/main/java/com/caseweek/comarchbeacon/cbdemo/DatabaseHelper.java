package com.caseweek.comarchbeacon.cbdemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    final String TAG = "DatabaseHelper";

    private Context mContext;

    // IMPORTANT: when application is released, everytime we want to change out database schema, we must
    // increment this value. More over, in onUpgrade method we have to handle upgrading database from older version to new one.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database.db";
    public static final String SELECTED = "Selected";

    private static DatabaseHelper mInstance = null;

    public interface Tables {
        String BEACONS = "beacons";
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // @formatter:off
        db.execSQL("CREATE TABLE " + Tables.BEACONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CBeacon.UUID + " TEXT, "
                + CBeacon.MAJOR + " INTEGER, "
                + CBeacon.MINOR + " INTEGER, "
                + CBeacon.COLOR + " TEXT "
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean dbExists() {
        java.io.File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    // BEACONS
    // ----------------------------------------------------------------------------------------------------

    public void insertBeacon(CBeacon beacon) {
        getWritableDatabase().insert(Tables.BEACONS, null, beacon.getContentValues());
    }

    public List<CBeacon> getBeacons() {
        Cursor c = getReadableDatabase().query(true, Tables.BEACONS, null, null, null, null, null, null, null);
        c.moveToFirst();
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<CBeacon> beacons = new ArrayList<CBeacon>(c.getCount());
        while (!c.isAfterLast()) {
            beacons.add(new CBeacon(c));
            c.moveToNext();
        }
        c.close();
        return beacons;
    }
}

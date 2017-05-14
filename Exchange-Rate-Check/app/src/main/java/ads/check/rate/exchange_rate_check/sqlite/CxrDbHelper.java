package ads.check.rate.exchange_rate_check.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CxrDbHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "CxrDbHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "CxrConversions.sqlite";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ConversionResultContract.ConversionEntry.TABLE_NAME + " (" +
                    ConversionResultContract.ConversionEntry._ID + " INTEGER PRIMARY KEY," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE + " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_BASE_DESC+ " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET + " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_TARGET_DESC + " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_PRICE + " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_VOLUME + " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_CHANGE + " TEXT," +
                    ConversionResultContract.ConversionEntry.COLUMN_NAME_DATETIME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ConversionResultContract.ConversionEntry.TABLE_NAME;

    public CxrDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }



}

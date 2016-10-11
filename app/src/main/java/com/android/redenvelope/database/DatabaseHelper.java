package com.android.redenvelope.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

	private static final String TAG = "DatabaseHelper";
	private static final String DATEBASE_NAME = "rednevelope.db";
	private static final String TABLE_RECORD = "record";
	private static final int DATABASE_VERSION = 1;
	private Context mContext;
	public DatabaseHelper(Context context) {
		super(context, DATEBASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE record (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "money DECIMAL(5,2)," +
                "usedtime Long," +
                "time TEXT" +
                ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
                + currentVersion);
		Log.i(TAG, "Upgrading database; wiping app data");
		if (oldVersion < currentVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
			onCreate(db);
		}
	}

}

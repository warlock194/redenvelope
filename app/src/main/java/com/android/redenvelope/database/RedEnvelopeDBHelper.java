package com.android.redenvelope.database;

import java.io.File;
import java.util.ArrayList;

import com.android.redenvelope.Record;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RedEnvelopeDBHelper {
	private static final String TAG = "RedEnvelopeDBHelper";
	private Context mContext;
	private DatabaseHelper mDatabase;
	private static final String  TABLE_RECORD = "record";
	
	public RedEnvelopeDBHelper(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mDatabase = new DatabaseHelper(context);
	}
	
	public ArrayList<Record> queryRecord() {
		ArrayList<Record> result = new ArrayList<Record>();
		
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		Cursor cursor = db.query(TABLE_RECORD, null, null, null, null, null, null);
		if (null == cursor) {
			Log.i(TAG, "queryRecord fail: cursor is null");
			return result;
		}
		
		try {
			final int columnNameId = 1;
			final int columnMoneyId = 2;
			final int columnUsedTimeId = 3;
			final int columnTimeId = 4;
			while(cursor.moveToNext()) {
				final String userName = cursor.getString(columnNameId);
				final float money = cursor.getFloat(columnMoneyId);
				final long usedTime = cursor.getLong(columnUsedTimeId);
				final String receiveTime = cursor.getString(columnTimeId);
				result.add(new Record(userName, money, usedTime, receiveTime));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return result;
	}
	
	public boolean insertRecordTable(ContentValues values) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		long id = db.insert(TABLE_RECORD, null, values);
		if (-1 != id) { //insert fail
			return false;
		}
		return true;
	}
	
	public void deleteRecord(String whereClause, String[] whereArgs) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		db.delete(TABLE_RECORD, whereClause, whereArgs);
	}
	
	public void DeleteDataBase() {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final File dbFile = new File(db.getPath());
		closeDB();
		if (dbFile.exists()) {
			SQLiteDatabase.deleteDatabase(dbFile);
		}
		mDatabase = new DatabaseHelper(mContext);
	}
	
	public void closeDB() {
		mDatabase.close();
	}
}

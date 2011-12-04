package org.csie.mpp.buku.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	public static final int VERSION = 1;
	public static final String NAME = "BUKU_DB";
	
	public final Table books;

	public DBHelper(Context context) {
		super(context, NAME, null, VERSION);
		
		books = new BookTable(this);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("Yi", "onCreateDB");
		Table[] tables = new Table[] {
			books
		};
		for(Table table: tables) {
			String stmt = table.getCreateStmt();
			db.execSQL(stmt);
			Log.i("Yi", stmt);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion) {
			default:
				break;
		}
	}

}

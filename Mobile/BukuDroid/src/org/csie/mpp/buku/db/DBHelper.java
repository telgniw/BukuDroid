package org.csie.mpp.buku.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final int VERSION = 3;
	public static final String NAME = "BUKU_DB";
	
	private static final Entry.Schema[] tables = { BookEntry.SCHEMA };

	public DBHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(Entry.Schema table: tables) {
			table.create(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion) {
			case 1:
				switch(newVersion) {
					case 3:
						BookEntry.SCHEMA.upgrade(db, new String[]{ "coverLink" });
						break;
				}
				break;
		}
	}
}

package org.csie.mpp.buku.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final int VERSION = 4;
	public static final String NAME = "BUKU_DB";
	
	private static final Entry.Schema[] tables = { BookEntry.SCHEMA, FriendEntry.SCHEMA };

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
		if(oldVersion == 1 && newVersion == 3) {
			BookEntry.SCHEMA.upgrade(db, new String[]{ "coverLink" });
		}
		else if(oldVersion < 4 && newVersion == 4) {
			FriendEntry.SCHEMA.create(db);
		}
	}
}

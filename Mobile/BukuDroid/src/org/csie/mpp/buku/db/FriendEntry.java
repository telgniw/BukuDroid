package org.csie.mpp.buku.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

public class FriendEntry extends Entry {
	public static final Schema SCHEMA = new Schema(FriendEntry.class);

	@Column(name="id", type=Type.TEXT, primary=true, notNull=true)
	public String id;
	
	@Column(name="name", type=Type.TEXT)
	public String name;
	
	@Column(name="firstname", type=Type.TEXT)
	public String firstname;
	
	@Column(name="icon", type=Type.IMAGE)
	public Bitmap icon;
	
	@Override
	public boolean equals(Object obj) {
		return id.equals(((FriendEntry)obj).id);
	}
	
	@Override
	public boolean insert(SQLiteDatabase db) {
		return SCHEMA.insert(db, this);
	}

	@Override
	public boolean delete(SQLiteDatabase db) {
		return SCHEMA.delete(db, "id = \"" + id + "\"");
	}

	public static int count(SQLiteDatabase db) {
		return SCHEMA.count(db);
	}

	public static boolean exists(SQLiteDatabase db, String id) {
		return SCHEMA.exists(db, "id = \"" + id + "\"");
	}
	
	public static FriendEntry[] queryAll(SQLiteDatabase db) {
		return queryAll(db, null);
	}
	
	public static FriendEntry[] queryAll(SQLiteDatabase db, String orderBy) {
		Cursor cursor = SCHEMA.queryAll(db, orderBy);
		FriendEntry[] entries = new FriendEntry[cursor.getCount()];
		for(int i = 0; cursor.moveToNext(); i++) {
			entries[i] = new FriendEntry();
			SCHEMA.extract(cursor, entries[i]);
		}
		return entries;
	}
}

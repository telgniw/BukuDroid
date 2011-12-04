package org.csie.mpp.buku.db;

import android.database.sqlite.SQLiteDatabase;

public class BookEntry extends Entry {
	public static final Schema SCHEMA = new Schema(BookEntry.class);
	
	@Column(name="id", type=Type.INTEGER, primary=true)
	public int id;
	
	@Column(name="isbn", type=Type.TEXT, notNull=true, skip=false)
	public String isbn;
	
	@Column(name="time", type=Type.DATE, defaultVal="CURRENT_DATE")
	public long time;
	
	public boolean insert(SQLiteDatabase db) {
		return SCHEMA.insert(db, this);
	}
	
	public static int count(SQLiteDatabase db) {
		return db.query(SCHEMA.getName(), null, null, null, null, null, null).getCount();
	}
}

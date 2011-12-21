package org.csie.mpp.buku.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

public class BookEntry extends Entry {
	public static final Schema SCHEMA = new Schema(BookEntry.class);
	
	public static class Info {
		public float rating;
		public int ratingsCount;
		public String description;
	}
	
	@Column(name="isbn", type=Type.TEXT, primary=true, notNull=true, skip=false)
	public String isbn;
	
	@Column(name="vid", type=Type.TEXT, skip=false, notNull=true)
	public String vid;
	
	@Column(name="time", type=Type.DATE, defaultVal="CURRENT_DATE")
	public long time;
	
	@Column(name="title", type=Type.TEXT, skip=false)
	public String title;
	
	@Column(name="author", type=Type.TEXT, skip=false)
	public String author;
	
	@Column(name="cover", type=Type.IMAGE, skip=false)
	public Bitmap cover;
	
	public Info info = new Info();
	
	@Override
	public boolean equals(Object obj) {
		return isbn.equals(((BookEntry)obj).isbn);
	}

	public boolean insert(SQLiteDatabase db) {
		return SCHEMA.insert(db, this);
	}

	public boolean delete(SQLiteDatabase db) {
		return SCHEMA.delete(db, "isbn =\"" + isbn + "\"");
	}

	public static int count(SQLiteDatabase db) {
		return SCHEMA.count(db);
	}
	
	public static BookEntry get(SQLiteDatabase db, String isbn) {
		Cursor cursor = SCHEMA.get(db, "isbn = \"" + isbn + "\"");
		BookEntry entry = new BookEntry();
		if(cursor.moveToNext()) {
			SCHEMA.extract(cursor, entry);
			return entry;
		}
		return null;
	}
	
	public static boolean exists(SQLiteDatabase db, String isbn) {
		return SCHEMA.exists(db, "isbn = \"" + isbn + "\"");
	}
	
	public static BookEntry[] queryAll(SQLiteDatabase db) {
		return queryAll(db, null);
	}
	
	public static BookEntry[] queryAll(SQLiteDatabase db, String orderBy) {
		Cursor cursor = SCHEMA.queryAll(db, orderBy);
		BookEntry[] entries = new BookEntry[cursor.getCount()];
		for(int i = 0; cursor.moveToNext(); i++) {
			entries[i] = new BookEntry();
			SCHEMA.extract(cursor, entries[i]);
		}
		return entries;
	}
	
	public static BookEntry[] search(SQLiteDatabase db, String keyword) {
		String query = "title LIKE '%" + keyword + "%' ";
		query += "OR author LIKE '%" + keyword + "%' ";
		query += "OR isbn LIKE '%" + keyword + "%' ";
		Cursor cursor = SCHEMA.get(db, query);
		BookEntry[] entries = new BookEntry[cursor.getCount()];
		for(int i = 0; cursor.moveToNext(); i++) {
			entries[i] = new BookEntry();
			SCHEMA.extract(cursor, entries[i]);
		}
		return entries;
	}
}

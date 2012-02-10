package org.csie.mpp.buku.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.text.Spanned;

public class BookEntry extends Entry implements Serializable{
	public static final Schema SCHEMA = new Schema(BookEntry.class);
	
	public static class Info implements Serializable{
		public float rating;
		public int ratingsCount;
		public String description;
		public ArrayList<String> reviews;
		
		public String sourceName;
		public String source;
	}

	@Column(name="isbn", type=Type.TEXT, primary=true, notNull=true)
	public String isbn;
	
	// added in version 1: unrecoverable
	@Column(name="vid", type=Type.TEXT, notNull=true)
	public String vid;
	
	@Column(name="time", type=Type.DATE, defaultVal="CURRENT_DATE")
	public long time;
	
	@Column(name="title", type=Type.TEXT)
	public String title;
	
	@Column(name="author", type=Type.TEXT)
	public String author;
	
	@Column(name="cover", type=Type.IMAGE)
	public Bitmap cover;
	
	// added in version 3
	@Column(name="coverLink", type=Type.TEXT)
	public String coverLink;
	
	public Info info = new Info();

	//for bitmap serialization
	private static ByteBuffer dst;
	private static byte[] bytesar;	
	
	@Override
	public boolean equals(Object obj) {
		return isbn.equals(((BookEntry)obj).isbn);
	}

	@Override
	public boolean insert(SQLiteDatabase db) {
		return SCHEMA.insert(db, this);
	}

	@Override
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

	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeObject(vid);
		out.writeLong(time);
		out.writeObject(title);
		out.writeObject(author);
		out.writeObject(coverLink);
		out.writeObject(info);

	    out.writeInt(cover.getRowBytes());
	    out.writeInt(cover.getHeight());
	    out.writeInt(cover.getWidth());
	    out.writeInt(cover.getConfig().ordinal());

	    final int bmSize = cover.getRowBytes() * cover.getHeight();
	    if (dst == null || bmSize > dst.capacity()) {
	        dst = ByteBuffer.allocate(bmSize);
	    }
	    dst.rewind();
	    cover.copyPixelsToBuffer(dst);
	    dst.flip();
	    out.write(dst.array(), 0, bmSize);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		vid = (String)in.readObject();
		time = in.readLong();
		title = (String)in.readObject();
		author = (String)in.readObject();
		coverLink = (String)in.readObject();
		info = (Info)in.readObject();

		final int nbRowBytes = in.readInt();
		final int height = in.readInt();
		final int width = in.readInt();
		final Bitmap.Config config = Bitmap.Config.values()[in.readInt()];

		final int bmSize = nbRowBytes * height;
		if (dst == null || bmSize > dst.capacity()) {
			dst = ByteBuffer.allocate(bmSize);
		}
		dst.rewind();
		in.read(dst.array(), 0, bmSize);

		cover = Bitmap.createBitmap(width, height, config);
		cover.copyPixelsFromBuffer(dst);
	}
}

package org.csie.mpp.buku.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.Util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public abstract class Entry {
	public static enum Type {
		BOOLEAN,
		INTEGER,
		DATE,
		TEXT,
		IMAGE
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Column {
		String name();
		Type type();
		String defaultVal() default "";
		boolean primary() default false;
		boolean notNull() default false;
	}
	
	public abstract boolean insert(SQLiteDatabase db);
	public abstract boolean delete(SQLiteDatabase db);
	
	public static class Schema {
		private Class<? extends Entry> cls;
		private Field[] fields;
		private String[] columns;
		
		public Schema(Class<? extends Entry> cls) {
			this.cls = cls;
			
			List<Field> fl = new ArrayList<Field>();
			List<String> cl = new ArrayList<String>();
			for(Field field: cls.getDeclaredFields()) {
				Column c = field.getAnnotation(Column.class);
				if(c == null)
					continue;
				fl.add(field);
				cl.add(c.name());
			}
			
			fields = fl.toArray(new Field[fl.size()]);
			columns = cl.toArray(new String[cl.size()]);
		}
		
		public String getName() {
			return cls.getSimpleName();
		}
		
		public void create(SQLiteDatabase db) {
			StringBuilder stmt = new StringBuilder("CREATE TABLE " + getName() + " (");
			for(int i = 0; i < fields.length; i++) {
				Column c = fields[i].getAnnotation(Column.class);
				if(i > 0)
					stmt.append(",");
				stmt.append(c.name() + " " + c.type());
				if(c.defaultVal().length() > 0)
					stmt.append(" DEFAULT " + c.defaultVal());
				if(c.primary())
					stmt.append(" PRIMARY KEY");
				if(c.notNull())
					stmt.append(" NOT NULL");
			}
			stmt.append(")");
			
			Log.i(App.TAG, stmt.toString());
			db.execSQL(stmt.toString());
		}
		
		public void upgrade(SQLiteDatabase db, String[] added) {
			Log.i(App.TAG, "Begin Upgrade Transaction");
			db.beginTransaction();
			db.execSQL("ALTER TABLE " + getName() + " RENAME TO " + getName() + "_old");
			create(db);
			
			StringBuilder oldColumns = new StringBuilder();
			for(int i = 0; i < fields.length; i++) {
				Column c = fields[i].getAnnotation(Column.class);
				boolean flag = true;
				for(String a: added) {
					if(a.equals(c.name())) {
						flag = false;
						break;
					}	
				}
				if(flag) {
					if(oldColumns.length() > 0)
						oldColumns.append(",");
					oldColumns.append(c.name());
				}
			}
			
			Log.i(App.TAG, "Old Columns: " + oldColumns);
			db.execSQL("INSERT INTO " + getName() + "(" + oldColumns + ") SELECT " + oldColumns + " FROM " + getName() + "_old");
			db.execSQL("DROP TABLE " + getName() + "_old");
			db.setTransactionSuccessful();
			db.endTransaction();
			Log.i(App.TAG, "End Upgrade Transaction");
		}
		
		public boolean insert(SQLiteDatabase db, Entry entry) {
			ContentValues values = new ContentValues();
			try {
				for(Field field: fields) {
					Column c = field.getAnnotation(Column.class);
					if(c.defaultVal().length() > 0)
						continue;
					switch(c.type()) {
						case BOOLEAN:
							values.put(c.name(), field.getShort(entry));
							break;
						case INTEGER:
							values.put(c.name(), field.getInt(entry));
							break;
						case DATE:
							values.put(c.name(), field.getLong(entry));
							break;
						case TEXT:
							values.put(c.name(), (String)field.get(entry));
							break;
						case IMAGE:
							byte[] bytes = Util.toByteArray((Bitmap)field.get(entry));
							values.put(c.name(), bytes);
							break;
						default:
							values.put(c.name(), (byte[])field.get(entry));
							break;
					}
				}
			}
			catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			return db.insert(getName(), null, values) != -1;
		}
		
		public boolean delete(SQLiteDatabase db, String whereClause) {
			return db.delete(getName(), whereClause, null) > 0;
		}
		
		public int count(SQLiteDatabase db) {
			return db.query(getName(), null, null, null, null, null, null).getCount();
		}
		
		public Cursor get(SQLiteDatabase db, String whereClause) {
			return db.query(getName(), null, whereClause, null, null, null, null);
		}
		
		public boolean exists(SQLiteDatabase db, String whereClause) {
			return get(db, whereClause).getCount() > 0;
		}
		
		public Cursor queryAll(SQLiteDatabase db, String orderBy) {
			return db.query(getName(), columns, null, null, null, null, orderBy);
		}
		
		protected <T extends Entry> void extract(Cursor cursor, T entry) {
			try {
				for(int i = 0; i < fields.length; i++) {
					Column c = fields[i].getAnnotation(Column.class);
					switch(c.type()) {
						case BOOLEAN:
							fields[i].set(entry, cursor.getShort(i));
							break;
						case INTEGER:
							fields[i].set(entry, cursor.getInt(i));
							break;
						case DATE:
							fields[i].set(entry, cursor.getLong(i));
							break;
						case TEXT:
							fields[i].set(entry, cursor.isNull(i)? null : cursor.getString(i));
							break;
						case IMAGE:
							if(cursor.isNull(i))
								fields[i].set(entry, null);
							else {
								byte[] bytes = cursor.getBlob(i);
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								fields[i].set(entry, bitmap);
							}
							break;
						default:
							fields[i].set(entry, cursor.isNull(i)? null : cursor.getBlob(i));
							break;
					}
				}
			}
			catch(IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

package org.csie.mpp.buku.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class Entry {
	public static enum Type {
		BOOLEAN,
		INTEGER,
		DATE,
		TEXT,
		BLOB
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Column {
		String name();
		Type type();
		String defaultVal() default "";
		boolean primary() default false;
		boolean notNull() default false;
		boolean skip() default true;
	}
	
	public abstract boolean insert(SQLiteDatabase db);
	
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
			db.execSQL(stmt.toString());
		}
		
		public boolean insert(SQLiteDatabase db, Entry entry) {
			ContentValues values = new ContentValues();
			try {
				for(Field field: fields) {
					Column c = field.getAnnotation(Column.class);
					if(c.skip())
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
						case BLOB:
							values.put(c.name(), (byte[])field.get(entry));
							break;
						default:
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
		
		public boolean exists(SQLiteDatabase db, String whereClause) {
			return db.query(getName(), null, whereClause, null, null, null, null).getCount() > 0;
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
						case BLOB:
							fields[i].set(entry, cursor.isNull(i)? null : cursor.getBlob(i));
							break;
						default:
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

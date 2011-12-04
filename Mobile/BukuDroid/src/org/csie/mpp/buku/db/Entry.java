package org.csie.mpp.buku.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import android.content.ContentValues;
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
		
		public Schema(Class<? extends Entry> cls) {
			this.cls = cls;
			this.fields = cls.getDeclaredFields();
		}
		
		public String getName() {
			return cls.getSimpleName();
		}
		
		public void create(SQLiteDatabase db) {
			StringBuilder stmt = new StringBuilder("CREATE TABLE " + getName() + " (");
			boolean flag = false;
			for(Field field: fields) {
				Column c = field.getAnnotation(Column.class);
				if(c == null)
					continue;
				if(flag)
					stmt.append(",");
				else
					flag = true;
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
					if(c == null || c.skip())
						continue;
					switch(c.type()) {
						case BOOLEAN:
						case INTEGER:
						case DATE:
							values.put(c.name(), field.getInt(entry));
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
	}
}

package org.csie.mpp.buku.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public abstract class Table {
	public enum Type {
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
	}
	
	protected DBHelper helper;
	protected Table(DBHelper helper) {
		this.helper = helper;
	}
	
	public String getCreateStmt() {
		StringBuilder stmt = new StringBuilder("CREATE TABLE " + getName() + " (");
		Field[] fields = getClass().getDeclaredFields();
		for(int i = 0; i < fields.length; i++) {
			if(i > 0)
				stmt.append(",");
			Column c = fields[i].getAnnotation(Column.class);
			if(c == null)
				continue;
			stmt.append(c.name() + " " + c.type());
			if(c.defaultVal().length() > 0)
				stmt.append(" DEFAULT " + c.defaultVal());
			if(c.primary())
				stmt.append(" PRIMARY KEY");
		}
		stmt.append(")");
		return stmt.toString();
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}
	
	public int getCount() {
		return helper.getReadableDatabase().query(getName(), null, null, null, null, null, null).getCount();
	}
}

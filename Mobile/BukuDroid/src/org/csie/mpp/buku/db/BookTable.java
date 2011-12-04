package org.csie.mpp.buku.db;

public class BookTable extends Table {
	@Column(name="id", type=Type.INTEGER, primary=true)
	public int id;
	
	@Column(name="isbn", type=Type.TEXT)
	public String isbn;
	
	@Column(name="time", type=Type.DATE, defaultVal="CURRENT_DATE")
	public long time;

	protected BookTable(DBHelper helper) {
		super(helper);
	}
}

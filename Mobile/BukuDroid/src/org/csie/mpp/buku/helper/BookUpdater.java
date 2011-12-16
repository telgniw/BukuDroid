package org.csie.mpp.buku.helper;

import java.net.URL;
import java.net.URLConnection;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.Util;
import org.csie.mpp.buku.db.BookEntry;
import org.json.JSONObject;

import android.util.Log;

public class BookUpdater {
	public static interface OnUpdateFinishedListener {
		public void OnUpdateFinished(BookEntry entry);
	}
	
	private BookEntry entry;
	
	protected OnUpdateFinishedListener listener;
	
	public BookUpdater(BookEntry e) {
		entry = e;
	}
	
	public void setOnUpdateFinishedListener(OnUpdateFinishedListener l) {
		listener = l;
	}
	
	public void update() {
		String isbn = entry.isbn;
		
		try {
			URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + isbn);
			URLConnection conn = url.openConnection();
			JSONObject jsonObject = new JSONObject(Util.connectionToString(conn));
			String id = jsonObject.getJSONArray("items").getJSONObject(0).getString("id");
			
			url = new URL("http://www.google.com/books/feeds/volumes/" + id + "?alt=json");
			conn = url.openConnection();
			jsonObject = new JSONObject(Util.connectionToString(conn));
			entry.title = jsonObject.getJSONObject("entry").getJSONObject("title").getString("$t");
			//TODO(ianchou): handle multiple author case
			entry.author = jsonObject.getJSONObject("entry").getJSONArray("dc$creator").getJSONObject(0).getString("$t");
		}
		catch(Exception e) {
			Log.e(App.TAG, e.toString());
		}
		
		listener.OnUpdateFinished(entry);
	}
}

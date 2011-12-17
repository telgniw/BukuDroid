package org.csie.mpp.buku.helper;

import java.net.URL;
import java.net.URLConnection;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.Util;
import org.csie.mpp.buku.db.BookEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BookUpdater {
	public static interface OnUpdateFinishedListener {
		public void OnUpdateFinished(BookEntry entry);
		public void OnUpdateFailed(BookEntry entry);
	}
	
	private BookEntry entry;
	
	protected OnUpdateFinishedListener listener;
	
	public BookUpdater(BookEntry e) {
		entry = e;
	}
	
	public void setOnUpdateFinishedListener(OnUpdateFinishedListener l) {
		listener = l;
	}
	
	public boolean update() {
		try {
			URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + entry.isbn);
			URLConnection conn = url.openConnection();
			JSONObject json = new JSONObject(Util.connectionToString(conn));
			json = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo");
			
			entry.title = json.getString("title");
			JSONArray authors = json.getJSONArray("authors");
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < authors.length(); i++) {
				if(i > 0)
					builder.append(",");
				builder.append(authors.getString(i));
			}
			entry.author = builder.toString();

			updateInfo(json);
		}
		catch(Exception e) {
			Log.e(App.TAG, e.toString());
			
			listener.OnUpdateFailed(entry);
			return false;
		}
		
		listener.OnUpdateFinished(entry);
		return true;
	}
	
	public boolean updateInfo() {
		try {
			URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + entry.isbn);
			URLConnection conn = url.openConnection();
			JSONObject json = new JSONObject(Util.connectionToString(conn));
			json = json.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo");
			
			updateInfo(json);
		}
		catch(Exception e) {
			Log.e(App.TAG, e.toString());
			
			listener.OnUpdateFailed(entry);
			return false;
		}
		
		listener.OnUpdateFinished(entry);
		return true;
	}
	
	protected void updateInfo(JSONObject json) throws JSONException {
		entry.info.rating = (float)json.getDouble("averageRating");
		entry.info.ratingsCount = json.getInt("ratingsCount");
	}
}

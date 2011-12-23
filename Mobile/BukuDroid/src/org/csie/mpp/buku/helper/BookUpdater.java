package org.csie.mpp.buku.helper;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.csie.mpp.buku.App;
import org.csie.mpp.buku.Util;
import org.csie.mpp.buku.db.BookEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class BookUpdater {
	public static interface OnUpdateFinishedListener {
		public void OnUpdateFinished();
		public void OnUpdateFailed();
	}
	
	private BookEntry entry;
	
	protected OnUpdateFinishedListener listener;
	
	public BookUpdater(BookEntry e) {
		entry = e;
	}
	
	public void setOnUpdateFinishedListener(OnUpdateFinishedListener l) {
		listener = l;
	}
	
	protected abstract class AsyncUpdater extends AsyncTask<URL, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(URL... urls) {
			return update(urls[0]);
		}
		
		protected abstract boolean update(URL url);
	}
	
	public boolean updateEntry() {
		try {
			URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + entry.isbn);
			AsyncUpdater async = new AsyncUpdater() {
				@Override
				protected boolean update(URL url) {
					try {
						JSONObject json = new JSONObject(Util.urlToString(url));
						entry.vid = json.getJSONArray("items").getJSONObject(0).getString("id");
						
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
						
						if(json.has("imageLinks"))	{
							URL imageUrl = new URL(json.getJSONObject("imageLinks").getString("thumbnail"));
							entry.cover = Util.urlToImage(imageUrl);
						}
					}
					catch(Exception e) {
						Log.e(App.TAG, e.toString());
						listener.OnUpdateFailed();
						return false;
					}

					listener.OnUpdateFinished();
					return true;
				}
			};
			
			return async.update(url);
		}
		catch(MalformedURLException e) {
			Log.e(App.TAG, e.toString());
		}
		
		return false;
	}
	
	public boolean updateEntryByBooks() {

	    HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet("http://search.books.com.tw/exep/prod_search.php?key=" + entry.isbn);
	    try {
	    	HttpResponse response = httpclient.execute(httpget);
	    	int statusCode = response.getStatusLine().getStatusCode();
	    	if (statusCode != HttpStatus.SC_OK) {
	    		return false;
	    		//TODO(ianchou): error handling
	    	}

	    	HttpEntity entity = response.getEntity();
	    	String result = EntityUtils.toString(entity, "big5");
	    	result = result.substring(result.indexOf("item=")+"item=".length());
	    	result = result.substring(0, result.indexOf("\""));
	    	entry.vid = result;

	    	httpget = new HttpGet("http://www.books.com.tw/exep/prod/booksfile.php?item=" + entry.vid);
	    	response = httpclient.execute(httpget);
	    	statusCode = response.getStatusLine().getStatusCode();
	    	if (statusCode != HttpStatus.SC_OK) {
	    		return false;
	    		//TODO(ianchou): error handling
	    	}
	    	
	    	entity = response.getEntity();
	    	result = EntityUtils.toString(entity, "big5");
	    	result = result.substring(result.indexOf("<span>圖片預覽</span>") + "<span>圖片預覽</span>".length());
	    	result = result.substring(result.indexOf("<img src=") + "<img src=".length());
	    	result = result.substring(result.indexOf("?image=") + "?image=".length());
	    	URL imageUrl = new URL(result.substring(0, result.indexOf("&")));
			entry.cover = Util.urlToImage(imageUrl);
	    	result = result.substring(result.indexOf("<!--product data-->") + "<!--product data-->".length());
	    	result = result.substring(result.indexOf("<span>") + "<span>".length());
	    	entry.title = result.substring(0, result.indexOf("<"));
	    	result = result.substring(result.indexOf("=author\">") + "=author\">".length());
	    	entry.author = result.substring(0, result.indexOf("<"));
	    	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    listener.OnUpdateFinished();
		return true;
	}

	public boolean updateInfo() {
		try {
			URL url = new URL("https://www.googleapis.com/books/v1/volumes/" + entry.vid);
			AsyncUpdater async = new AsyncUpdater() {
				@Override
				protected boolean update(URL url) {
					try {
						JSONObject json = new JSONObject(Util.urlToString(url));
						json = json.getJSONObject("volumeInfo");
						
						updateInfo(json);
					}
					catch(Exception e) {
						Log.e(App.TAG, e.toString());
						return false;
					}
					
					listener.OnUpdateFinished();
					return true;
				}
			};
			
			return async.update(url);
		}
		catch(MalformedURLException e) {
			Log.e(App.TAG, e.toString());
		}
		
		return false;
	}
	
	protected void updateInfo(JSONObject json) throws JSONException {
		if(json.has("averageRating"))
			entry.info.rating = (float)json.getDouble("averageRating");
		if(json.has("ratingsCount"))
			entry.info.ratingsCount = json.getInt("ratingsCount");
		if(json.has("description"))
			entry.info.description = json.getString("description");
	}
}

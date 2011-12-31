package org.csie.mpp.buku.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
import org.json.JSONObject;

import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public abstract class BookUpdater {
	public static interface OnUpdatStatusChangedListener {
		public enum Status {
			OK_ENTRY,
			OK_INFO,
			BOOK_NOT_FOUND,
			UNKNOWN
		}

		public void onUpdateStart();
		public void onUpdateProgress();
		public void onUpdateFinish(Status status);
	}
	
	public static BookUpdater create(BookEntry entry) {
		switch(entry.isbn.length()) {
			case 10:
				entry.isbn = Util.toIsbn13(entry.isbn);
				break;
			case 17:
				entry.isbn = Util.upcToIsbn(entry.isbn);
				break;
			default:
				break;
		}
		String countryCode = entry.isbn.substring(3, 6);
        if(countryCode.equals("957") || countryCode.equals("986"))
        	return new ChineseUpdater(entry);
		return new NormalUpdater(entry);
	}
	
	protected final BookEntry entry;
	protected OnUpdatStatusChangedListener listener;
	
	protected BookUpdater(BookEntry e) {
		entry = e;
	}
	
	public void setOnUpdateFinishedListener(OnUpdatStatusChangedListener l) {
		listener = l;
	}
	
	public abstract void updateEntry();
	public abstract void updateInfo();
	
	protected abstract class AsyncUpdater extends AsyncTask<URL, Integer, OnUpdatStatusChangedListener.Status> {
		@Override
		protected OnUpdatStatusChangedListener.Status doInBackground(URL... urls) {
			return update(urls);
		}
		
		@Override
		protected void onPreExecute() {
			listener.onUpdateStart();
		}
		
		@Override
		protected void onProgressUpdate(Integer... progresses) {
			listener.onUpdateProgress();
		}
		
		@Override
		protected void onPostExecute(OnUpdatStatusChangedListener.Status result) {
			/* this will be run on UI thread */
			listener.onUpdateFinish(result);
		}
		
		protected abstract OnUpdatStatusChangedListener.Status update(URL... urls);
	}
	
	public static class NormalUpdater extends BookUpdater {
		protected NormalUpdater(BookEntry e) {
			super(e);
		}
		
		@Override
		public void updateEntry() {
			try {
				AsyncUpdater async = new AsyncUpdater() {
					@Override
					protected OnUpdatStatusChangedListener.Status update(URL... urls) {
						try {
							JSONObject json = new JSONObject(Util.urlToString(urls[0]));
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
							publishProgress();
							
							if(json.has("imageLinks"))	{
								entry.coverLink = json.getJSONObject("imageLinks").getString("thumbnail");
								URL imageUrl = new URL(entry.coverLink);
								entry.cover = Util.urlToImage(imageUrl);
							}
						}
						catch(Exception e) {
							Log.e(App.TAG, e.toString());
							return OnUpdatStatusChangedListener.Status.UNKNOWN;
						}

						return OnUpdatStatusChangedListener.Status.OK_ENTRY;
					}
				};
				
				URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn" + entry.isbn);
				async.execute(url);
			}
			catch(MalformedURLException e) {
				Log.e(App.TAG, e.toString());
			}
		}

		@Override
		public void updateInfo() {
			try {
				AsyncUpdater async = new AsyncUpdater() {
					@Override
					protected OnUpdatStatusChangedListener.Status update(URL... urls) {
						try {
							JSONObject json = new JSONObject(Util.urlToString(urls[0]));
							json = json.getJSONObject("volumeInfo");
							
							if(json.has("averageRating"))
								entry.info.rating = (float)json.getDouble("averageRating");
							if(json.has("ratingsCount"))
								entry.info.ratingsCount = json.getInt("ratingsCount");
							if(json.has("description"))
								entry.info.description = Html.fromHtml(json.getString("description"));
							publishProgress();

							HttpClient httpclient = new DefaultHttpClient();
						    HttpGet httpget = new HttpGet(urls[1].toURI());
					    	HttpResponse response = httpclient.execute(httpget);
					    	int statusCode = response.getStatusLine().getStatusCode();
					    	if (statusCode != HttpStatus.SC_OK) {
					    		return OnUpdatStatusChangedListener.Status.UNKNOWN;
					    	}

					    	HttpEntity entity = response.getEntity();
					    	String result = EntityUtils.toString(entity, "UTF-8");
					    	result = result.substring(result.indexOf(">User reviews<") + ">User reviews<".length());
					    	entry.info.reviews = new ArrayList<Spanned>();
					    	while(result.indexOf("<p dir=ltr>")!=-1){
					    		result = result.substring(result.indexOf("<p dir=ltr>") + "<p dir=ltr>".length());
					    		entry.info.reviews.add(Util.htmlToText(result.substring(0, result.indexOf("</p>"))));
					    	}

					    	entry.info.sources = Html.fromHtml("<a href=\"http://books.google.com/books?id=" + entry.vid + "\">google books</a>");
						}
						catch(Exception e) {
							Log.e(App.TAG, e.toString());
							return OnUpdatStatusChangedListener.Status.UNKNOWN;
						}
						
						return OnUpdatStatusChangedListener.Status.OK_INFO;
					}
				};
				
				URL url0 = new URL("https://www.googleapis.com/books/v1/volumes/" + entry.vid);
				URL url1 = new URL("http://books.google.com.tw/books?id=" + entry.vid + "&sitesec=reviews&hl=eng");
				async.execute(url0, url1);
			}
			catch(MalformedURLException e) {
				Log.e(App.TAG, e.toString());
			}
		}
	}
	
	public static class ChineseUpdater extends BookUpdater {
		protected ChineseUpdater(BookEntry e) {
			super(e);
		}


		@Override
		public void updateEntry() {
			try {
				AsyncUpdater async = new AsyncUpdater() {
					@Override
					protected OnUpdatStatusChangedListener.Status update(URL... urls) {
						try {
							HttpGet httpget = new HttpGet(urls[0].toURI());
							HttpResponse response = new DefaultHttpClient().execute(httpget);
					    	int statusCode = response.getStatusLine().getStatusCode();
					    	if (statusCode != HttpStatus.SC_OK) {
					    		return OnUpdatStatusChangedListener.Status.UNKNOWN;
					    	}

					    	HttpEntity entity = response.getEntity();
					    	String result = EntityUtils.toString(entity, "UTF-8");
					    	if(result.indexOf("item=")<0) {
					    		return OnUpdatStatusChangedListener.Status.BOOK_NOT_FOUND;
					    	}
					    	
					    	result = result.substring(result.indexOf("item=")+"item=".length());
					    	entry.vid = result.substring(0, result.indexOf("\"")).trim();
					    	result = result.substring(result.indexOf("title=\"") + "title=\"".length()); 	
					    	entry.title = result.substring(0, result.indexOf("\"")).trim();
					    	publishProgress();
					    	
					    	result = result.substring(result.indexOf("?image=") + "?image=".length());
					    	entry.coverLink = result.substring(0, result.indexOf("&"));
							entry.cover = Util.urlToImage(new URL(entry.coverLink));
							publishProgress();
							
							if(result.indexOf("\"go_author\"")!=-1){
								result = result.substring(result.indexOf("\"go_author\"") + "\"go_author\"".length());
								result = result.substring(result.indexOf("title=\"") + "title=\"".length());
						    	entry.author = result.substring(0, result.indexOf("\"")).trim();
							}
							
					    	return OnUpdatStatusChangedListener.Status.OK_ENTRY;
						}
						catch(Exception e) {
							Log.e(App.TAG, e.toString());
							return OnUpdatStatusChangedListener.Status.UNKNOWN;
						}
					}
				};
				
				URL url = new URL("http://search.books.com.tw/exep/prod_search.php?key=" + entry.isbn);
				async.execute(url);
			}
			catch(MalformedURLException e) {
				Log.e(App.TAG, e.toString());
			}
		}
		
		@Override
		public void updateInfo() {
			try {
				AsyncUpdater async = new AsyncUpdater() {
					@Override
					protected OnUpdatStatusChangedListener.Status update(URL... urls) {
						try {
							HttpClient httpclient = new DefaultHttpClient();
						    HttpGet httpget = new HttpGet(urls[0].toURI());
							HttpResponse response = httpclient.execute(httpget);
					    	int statusCode = response.getStatusLine().getStatusCode();
					    	if (statusCode != HttpStatus.SC_OK) {
					    		return OnUpdatStatusChangedListener.Status.UNKNOWN;
					    	}

					    	HttpEntity entity = response.getEntity();
					    	String result = EntityUtils.toString(entity, "big5");
					    	result = result.substring(result.indexOf("class=\"content_word\"")+"class=\"content_word\"".length());
					    	result = result.substring(result.indexOf("<BR><BR>")+"<BR><BR>".length());
					    	result = result.substring(0, result.indexOf("</td>"));
					    	entry.info.description = Util.htmlToText(result.trim());
					    	publishProgress();

					    	httpget = new HttpGet(urls[1].toURI());
					    	response = httpclient.execute(httpget);
					    	statusCode = response.getStatusLine().getStatusCode();
					    	if (statusCode != HttpStatus.SC_OK) {
					    		return OnUpdatStatusChangedListener.Status.UNKNOWN;
					    	}
					    	entity = response.getEntity();
					    	result = EntityUtils.toString(entity, "big5");
					    	
					    	if(result.indexOf(">讀者書評等級：")!=-1 && result.indexOf("</tt>")!=-1){
					    		String tmp = result.substring(result.indexOf(">讀者書評等級："), result.indexOf("</tt>"));
					    		entry.info.rating = 0;
					    		while(tmp.indexOf("images/m_bul11.gif")!=-1){
					    			++entry.info.rating;
					    			tmp = tmp.substring(tmp.indexOf("images/m_bul11.gif") + "images/m_bul11.gif".length());
					    		}
					    	}
					    	entry.info.reviews = new ArrayList<Spanned>();
					    	while(result.indexOf("<p class=\"des\">")!=-1){
					    		result = result.substring(result.indexOf("<p class=\"des\">") + "<p class=\"des\">".length());
					    		entry.info.reviews.add(Util.htmlToText(result.substring(0, result.indexOf("</p>"))));
					    	}

					    	entry.info.sources = Html.fromHtml("<a href=\"http://www.books.com.tw/exep/prod/booksfile.php?item=" + entry.vid + "\">博客來</a>");
						}
						catch(Exception e) {
							Log.e(App.TAG, e.toString());
							return OnUpdatStatusChangedListener.Status.UNKNOWN;
						}
						
						return OnUpdatStatusChangedListener.Status.OK_INFO;
					}
				};
				
				URL url0 = new URL("http://m.books.com.tw/product/showmore/" + entry.vid);
				URL url1 = new URL("http://www.books.com.tw/exep/prod/reader_opinion.php?item=" + entry.vid);
				async.execute(url0, url1);
			}
			catch(MalformedURLException e) {
				Log.e(App.TAG, e.toString());
			}
		}
	}
}

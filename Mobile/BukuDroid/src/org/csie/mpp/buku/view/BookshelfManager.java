package org.csie.mpp.buku.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.csie.mpp.buku.App;
import org.csie.mpp.buku.BookActivity;
import org.csie.mpp.buku.MainActivity;
import org.csie.mpp.buku.R;
import org.csie.mpp.buku.ScanActivity;
import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.listener.ContextMenuCallback;
import org.csie.mpp.buku.listener.ResultCallback;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BookshelfManager extends ViewManager implements ResultCallback {
	private interface ViewManager {
		public void initView(View view);
		public int length();
		public void setBooks(BookEntry[] entries);
		public void addBook(String isbn);
		public void removeBook(int position);
	};
	
	private class ListViewManager implements ViewManager, OnItemClickListener, ResultCallback, ContextMenuCallback {
		public static final int REQUEST_CODE = 435;
		private static final String FIELD_ICON = "ICON", FIELD_TITLE = "TITLE", FIELD_AUTHOR = "AUTHOR";
		
		private List<BookEntry> entries; 
		private ListView booklist;
		private SimpleAdapter booklistAdapter;
		private List<Map<String, Object>> list_items;
		
		public ListViewManager() {
			((MainActivity)activity).register(this);
		}
		
		@Override
		public void initView(View view) {
			booklist = (ListView)view.findViewById(R.id.inner_list);
			list_items = new ArrayList<Map<String, Object>>();
			booklistAdapter = new SimpleAdapter(
				activity, list_items, R.layout.list_item_book, new String[] {
					FIELD_ICON, FIELD_TITLE, FIELD_AUTHOR
				}, new int[] {
					R.id.list_image, R.id.list_title, R.id.list_author
				}
			);
			booklist.setAdapter(booklistAdapter);
			booklist.setOnItemClickListener(this);
			
			activity.registerForContextMenu(booklist);
			((MainActivity)activity).register(booklist, this);
		}
		
		@Override
		public int length() {
			return entries == null? -1 : entries.size();
		}
		
		@Override
		public void setBooks(BookEntry[] es) {
			entries = null;
			list_items.clear();
			for(BookEntry entry: es)
				_addBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}
		
		private void _addBook_(BookEntry entry) {
			if(entries == null)
				entries = new ArrayList<BookEntry>();
			entries.add(entry);
			
			BookInfo info = queryBookInfo(entry.isbn);
			Map<String, Object> item = new HashMap<String, Object>();
			item.put(FIELD_ICON, R.drawable.book);
			item.put(FIELD_TITLE, info.title);
			item.put(FIELD_AUTHOR, info.author);
			list_items.add(item);
		}

		@Override
		public void addBook(String isbn) {
			BookEntry entry = new BookEntry();
			entry.isbn = isbn;
			if(!entry.insert(wdb))
				Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
			_addBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}
		
		private void _removeBook_(int position) {
			entries.remove(position);
			list_items.remove(position);
		}

		@Override
		public void removeBook(int position) {
			entries.get(position).delete(rdb);
			_removeBook_(position);
			booklistAdapter.notifyDataSetChanged();
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent intent = new Intent(activity, BookActivity.class);
			intent.putExtra(BookActivity.ISBN, entries.get(position).isbn);
			activity.startActivityForResult(intent, REQUEST_CODE);
		}

		@Override
		public void onResult(int requestCode, int resultCode, Intent data) {
			if(requestCode == REQUEST_CODE) {
				// TODO
			}
		}

		private static final int MENU_DELETE = 0;
		private String[] menuItems;
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, ContextMenuInfo menuInfo) {
			int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if(menuItems == null)
				menuItems = activity.getResources().getStringArray(R.array.list_item_longclick);
			for(String menuItem: menuItems)
				menu.add(menuItem);
			menu.setHeaderTitle(entries.get(position).isbn);
		}

		@Override
		public void onSelectContextMenu(MenuItem item) {
			int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
			switch(item.getItemId()) {
				case MENU_DELETE:
					removeBook(position);
					break;
				default:
					break;
			}
		}
	};
	
	protected ListViewManager vm;
	
	public BookshelfManager(Activity activity, DBHelper helper) {
		super(activity, helper);
		
		vm = new ListViewManager();
		((MainActivity)activity).register(this);
	}
	
	public void onResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ScanActivity.REQUEST_CODE) {
			if(resultCode == ScanActivity.RESULT_FIRST_USER) {
				final String isbn = data.getStringExtra(ScanActivity.ISBN);
				if(!BookEntry.exists(rdb, isbn))
					updateBooklist(isbn);
				else {
					new AlertDialog.Builder(activity).setPositiveButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							updateBooklist(isbn);
						}
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setMessage(R.string.book_already_exists).create().show();
				}
			}
		}
	}
	
	private void updateBooklist() {
		BookEntry[] entries = BookEntry.queryAll(rdb);
		vm.setBooks(entries);
	}
	
	private void updateBooklist(String isbn) {
		if(vm.length() < 0)
			createBookView();
		vm.addBook(isbn);
	}

	@Override
	protected int getFrameId() {
		return R.id.bookshelf_frame;
	}
	
	private void createBookView() {
		FrameLayout frame = getFrame();
		if(frame.getChildCount() > 0)
			frame.removeAllViews();
		View view = activity.getLayoutInflater().inflate(R.layout.list, null);
		frame.addView(view);
		vm.initView(view);
	}
	
	private void createNoBookView() {
		FrameLayout frame = getFrame();
		if(frame.getChildCount() > 0)
			frame.removeAllViews();
		View view = activity.getLayoutInflater().inflate(R.layout.none, null);
		frame.addView(view);
		TextView text = (TextView)view.findViewById(R.id.inner_text);
		text.setText(R.string.add_book_to_start);
	}

	@Override
	protected void updateView() {
		if(BookEntry.count(rdb) == 0)
			createNoBookView();
		else {
			createBookView();
			updateBooklist();
		}
	}
	
	//TODO(ianchou): move this class to someplace more suitable
	/*
	 * a class to store the information of book from http://openlibrary.org
	 */
	private class BookInfo {
		public String isbn;
		public String title;
		public String author;
		BookInfo(){
			title = "Unknown";
			author = "Unknown";
		}
	}

	/*
	 * handle the query to http://openlibrary.org, get the information of book, may change to use google book.
	 */
	public BookInfo queryBookInfo(String isbn) {
		// need isbn_10 for http://openlibrary.org
		if(isbn.length()==13){
			isbn = convertISBN(isbn);
		}
		BookInfo bookInfo = new BookInfo();
		String key = "";
		String authorsKey = "";

		try{
			URL url = new URL("http://openlibrary.org/api/things?query={\"type\":\"\\/type\\/edition\",\"isbn_10\":\""+isbn+"\"}");
			URLConnection conn = url.openConnection ();
			JSONObject jsonObject = new JSONObject(connectionToString(conn));
			System.err.println(jsonObject);
			key = jsonObject.getJSONArray("result").getString(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
			if(key.isEmpty()){
				return bookInfo;
			}
			URL url = new URL("http://openlibrary.org/api/get?key="+key);
			URLConnection conn = url.openConnection ();
			JSONObject jsonObject = new JSONObject(connectionToString(conn));
			JSONObject result = jsonObject.getJSONObject("result");
			bookInfo.title = result.getString("title");
			authorsKey = result.getJSONArray("authors").getJSONObject(0).getString("key");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
			if(authorsKey.isEmpty()){
				return bookInfo;
			}
			URL url = new URL("http://openlibrary.org/api/get?key="+authorsKey);
			URLConnection conn = url.openConnection ();
			JSONObject jsonObject = new JSONObject(connectionToString(conn));
			JSONObject result = jsonObject.getJSONObject("result");
			bookInfo.author = result.getString("name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookInfo;
	}
	
	public String connectionToString(URLConnection conn) {
		StringBuilder builder = new StringBuilder();
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return builder.toString();
	}	
	
	public String convertISBN(String isbn) {
		StringBuilder builder = new StringBuilder();
		builder.append(isbn.substring(3,12));
		int sum=0;
		for(int i=0;i<9;++i){
			sum += (isbn.charAt(i+3)-'0')*(10-i);
		}
		int m = sum % 11;
		if(m==1)
			builder.append('X');
		else if(m==0)
			builder.append(0);
		else
			builder.append(11-m);
		return builder.toString();
	}
}

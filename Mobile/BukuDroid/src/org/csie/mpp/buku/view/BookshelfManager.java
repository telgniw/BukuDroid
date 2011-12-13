package org.csie.mpp.buku.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.R;
import org.csie.mpp.buku.ScanActivity;
import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BookshelfManager extends ViewManager {
	private interface ViewManager {
		public void initView(View view);
		public int length();
		public void setBooks(BookEntry[] entries);
		public void addBook(BookEntry entry);
		public void remove(BookEntry entry);
	};
	
	private class ListViewManager implements ViewManager {
		private static final String FIELD_ICON = "ICON", FIELD_TITLE = "TITLE", FIELD_AUTHOR = "AUTHOR";
		
		private BookEntry[] entries; 
		private ListView booklist;
		private SimpleAdapter booklistAdapter;
		private List<Map<String, Object>> list_items;
		
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
		}
		
		@Override
		public int length() {
			return entries == null? 0 : entries.length;
		}
		
		@Override
		public void setBooks(BookEntry[] es) {
			entries = es;
			for(BookEntry entry: entries)
				_addBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}
		
		private void _addBook_(BookEntry entry) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put(FIELD_ICON, R.drawable.book);
			item.put(FIELD_TITLE, entry.isbn);
			item.put(FIELD_AUTHOR, "author");
			list_items.add(item);
		}

		@Override
		public void addBook(BookEntry entry) {
			_addBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}

		@Override
		public void remove(BookEntry entry) {
			
		}
	};
	
	protected ListViewManager vm;
	
	public BookshelfManager(Activity activity, DBHelper helper) {
		super(activity, helper);
		
		vm = new ListViewManager();
	}
	
	public void onResultCallback(int requestCode, int resultCode, Intent data) {
		if(requestCode == ScanActivity.REQUEST_CODE) {
			if(resultCode == ScanActivity.RESULT_FIRST_USER) {
				BookEntry entry = new BookEntry();
				entry.isbn = data.getStringExtra(ScanActivity.ISBN);
				if(!entry.insert(wdb))
					Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
				updateBooklist(entry);
			}
		}
	}
	
	private void updateBooklist() {
		BookEntry[] entries = BookEntry.queryAll(rdb);
		vm.setBooks(entries);
	}
	
	private void updateBooklist(BookEntry entry) {
		vm.addBook(entry);
	}

	@Override
	protected int getFrameId() {
		return R.id.bookshelf_frame;
	}

	@Override
	protected void updateView() {
		FrameLayout frame = getFrame();
		if(frame.getChildCount() > 0)
			frame.removeAllViews();
		if(BookEntry.count(rdb) == 0) {
			View view = activity.getLayoutInflater().inflate(R.layout.none, null);
			frame.addView(view);
			TextView text = (TextView)view.findViewById(R.id.inner_text);
			text.setText(R.string.add_book_to_start);
		}
		else {
			View view = activity.getLayoutInflater().inflate(R.layout.list, null);
			frame.addView(view);
			vm.initView(view);
			updateBooklist();
		}
	}
}

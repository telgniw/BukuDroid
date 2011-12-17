package org.csie.mpp.buku.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BookshelfManager extends ViewManager {
	public static interface ViewListener {
		public void onListViewCreated(ListView view);
	}
	
	private interface ViewManager {
		public void initView(View view);
		public int length();
		public void set(BookEntry[] entries);
		public BookEntry get(int position);
		public void add(BookEntry entry);
		public void remove(BookEntry entry);
	}
	
	private class ListViewManager implements ViewManager {
		private static final String FIELD_ICON = "ICON", FIELD_TITLE = "TITLE", FIELD_AUTHOR = "AUTHOR";
		
		private List<BookEntry> entries; 
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
			
			if(callback != null)
				callback.onListViewCreated(booklist);
		}
		
		@Override
		public int length() {
			return entries == null? -1 : entries.size();
		}
		
		@Override
		public void set(BookEntry[] es) {
			entries = null;
			list_items.clear();
			for(BookEntry entry: es)
				_addBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}
		
		@Override
		public BookEntry get(int position) {
			return entries.get(position);
		}

		@Override
		public void add(BookEntry entry) {
			_addBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}

		@Override
		public void remove(BookEntry entry) {
			_removeBook_(entry);
			booklistAdapter.notifyDataSetChanged();
		}
		
		private void _addBook_(BookEntry entry) {
			if(entries == null)
				entries = new ArrayList<BookEntry>();
			entries.add(entry);
			
			Map<String, Object> item = new HashMap<String, Object>();
			item.put(FIELD_ICON, R.drawable.book);
			item.put(FIELD_TITLE, entry.title);
			item.put(FIELD_AUTHOR, entry.author);
			list_items.add(item);
		}
		
		private void _removeBook_(BookEntry entry) {
			int position = entries.indexOf(entry);
			entries.remove(position);
			list_items.remove(position);
		}
	}
	
	private ListViewManager vm;
	
	public BookshelfManager(Activity activity, DBHelper helper) {
		super(activity, helper);
		
		vm = new ListViewManager();
	}
	
	private ViewListener callback;
	
	public BookshelfManager(Activity activity, DBHelper helper, ViewListener callback) {
		this(activity, helper);
		
		this.callback = callback;
	}
	
	public void add(String isbn) {
		BookEntry entry = BookEntry.get(rdb, isbn);
		if(vm.length() < 0)
			createBookView();
		vm.add(entry);
	}
	
	public BookEntry get(int position) {
		return vm.get(position);
	}
	
	public void remove(BookEntry entry) {
		vm.remove(entry);
	}
	
	private void updateBooklist() {
		BookEntry[] entries = BookEntry.queryAll(rdb);
		vm.set(entries);
	}

	@Override
	protected int getFrameId() {
		return R.id.bookshelf_frame;
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
}

package org.csie.mpp.buku.view;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.R;
import org.csie.mpp.buku.ScanActivity;
import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.view.ViewPageFragment.ViewPageFragmentListener;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

public class BookshelfManager implements ViewPageFragmentListener {
	private Activity activity;
	private DBHelper helper;
	private SQLiteDatabase rdb, wdb;
	
	protected FrameLayout frame;
	protected ListView booklist;
	protected ArrayAdapter<String> booklistAdapter;
	
	public BookshelfManager(Activity activity) {
		this.activity = activity;
		
		helper = new DBHelper(activity);
		rdb = helper.getReadableDatabase();
		wdb = helper.getWritableDatabase();
	}
	
	public void onResultCallback(int requestCode, int resultCode, Intent data) {
		if(requestCode == ScanActivity.REQUEST_CODE) {
			if(resultCode == ScanActivity.RESULT_FIRST_USER) {
				BookEntry entry = new BookEntry();
				entry.isbn = data.getStringExtra(ScanActivity.ISBN);
				if(!entry.insert(wdb))
					Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
				updateBooklist();
			}
		}
	}
	
	private void updateBooklist() {
		if(booklist == null)
			updateView();
		else {
			BookEntry[] entries = BookEntry.queryAll(rdb);
			for(BookEntry entry: entries)
				booklistAdapter.add(entry.isbn);
					booklistAdapter.notifyDataSetChanged();
		}
	}
	
	private void updateView() {
		if(frame.getChildCount() > 0)
			frame.removeAllViews();
		if(BookEntry.count(rdb) == 0)
			frame.addView(activity.getLayoutInflater().inflate(R.layout.books_none, null));
		else {
			View view = activity.getLayoutInflater().inflate(R.layout.books_list, null);
			frame.addView(view);
			booklist = (ListView)view.findViewById(R.id.book_list);
			booklistAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1);
			booklist.setAdapter(booklistAdapter);
			updateBooklist();
		}
	}

	/* --- ViewPageFragmentListener	(start) --- */
	@Override
	public void onCreate(View view) {
		frame = (FrameLayout)view.findViewById(R.id.bookshelf_frame);
		updateView();
	}
	/* --- ViewPageFragmentListener	(end) --- */
}

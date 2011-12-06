package org.csie.mpp.buku.view;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.R;
import org.csie.mpp.buku.ScanActivity;
import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BookshelfManager extends ViewManager {
	protected ListView booklist;
	protected ArrayAdapter<String> booklistAdapter;
	
	public BookshelfManager(Activity activity, DBHelper helper) {
		super(activity, helper);
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
			booklist = (ListView)view.findViewById(R.id.inner_list);
			booklistAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1);
			booklist.setAdapter(booklistAdapter);
			updateBooklist();
		}
	}
}

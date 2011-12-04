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
import android.widget.FrameLayout;

public class BookshelfManager implements ViewPageFragmentListener {
	private Activity activity;
	private DBHelper helper;
	private SQLiteDatabase rdb, wdb;
	
	protected FrameLayout frame;
	
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
				updateView();
			}
		}
	}
	
	private void updateView() {
		if(frame.getChildCount() > 0)
			frame.removeAllViews();
		if(BookEntry.count(rdb) == 0)
			frame.addView(activity.getLayoutInflater().inflate(R.layout.books_none, null));
		else {
			frame.addView(activity.getLayoutInflater().inflate(R.layout.books_list, null));
			// TODO
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

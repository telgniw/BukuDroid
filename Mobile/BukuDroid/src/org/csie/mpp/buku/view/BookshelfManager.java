package org.csie.mpp.buku.view;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.view.ViewPageFragment.ViewPageFragmentListener;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

public class BookshelfManager implements ViewPageFragmentListener {
	private Activity activity;
	private FrameLayout frame;
	
	public BookshelfManager(Activity activity) {
		this.activity = activity;
	}

	/* --- ViewPageFragmentListener	(start) --- */
	@Override
	public void onCreate(View view) {
		frame = (FrameLayout)view.findViewById(R.id.bookshelf_frame);
		frame.addView(activity.getLayoutInflater().inflate(R.layout.books_none, null));
	}
	/* --- ViewPageFragmentListener	(end) --- */

}

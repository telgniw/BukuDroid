package org.csie.mpp.buku;

import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.helper.BookUpdater;
import org.csie.mpp.buku.helper.BookUpdater.OnUpdateFinishedListener;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class BookActivity extends Activity implements OnUpdateFinishedListener {
	public static final int REQUEST_CODE = 1437;
	public static final String ISBN = "isbn";
	public static final String CHECK_DUPLICATE = "duplicate";
	
	private DBHelper db;
	private BookEntry entry;
	private ActionBar actionBar;
	private boolean inBookshelf = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        db = new DBHelper(this);

        Intent intent = getIntent();
        String isbn = intent.getStringExtra(ISBN);
        entry = BookEntry.get(db.getReadableDatabase(), isbn);
        
        actionBar = ((ActionBar)findViewById(R.id.actionbar));
        
        boolean updateAll = false;
        
        if(entry != null) {
        	if(intent.getBooleanExtra(CHECK_DUPLICATE, false))
        		Toast.makeText(this, R.string.book_already_exists, 3000).show();
        	
			actionBar.addAction(new AbstractAction(R.drawable.ic_delete) {
				@Override
				public void performAction(View view) {
					Intent data = new Intent();
					data.putExtra(BookActivity.ISBN, entry.isbn);
					setResult(RESULT_FIRST_USER, data);
					finish();
				}
			});
			
			inBookshelf = true;
        	updateView();
        }
        else {
        	entry = new BookEntry();
        	entry.isbn = isbn;
        	updateAll = true;
        	
			actionBar.addAction(new AbstractAction(R.drawable.ic_bookshelf) {
				@Override
				public void performAction(View view) {
					if(entry.insert(db.getWritableDatabase()) == false)
						Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
					
					Intent data = new Intent();
					data.putExtra(BookActivity.ISBN, entry.isbn);
					setResult(RESULT_OK, data);
					finish();
				}
			});
        }	
        
        BookUpdater updater = new BookUpdater(entry);
        updater.setOnUpdateFinishedListener(this);
        
        if(updateAll) {
        	if(updater.updateEntry())
        		updater.updateInfo();
        } else {
        	updater.updateInfo();
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	db.close();
    }

    /* --- OnUpdateFinishedListener	(start) --- */
	@Override
	public void OnUpdateFinished() {
		updateView();
	}

	@Override
	public void OnUpdateFailed() {
		if(!inBookshelf)
			actionBar.removeViewAt(0);
		showError();
	}
	/* --- OnUpdateFinishedListener	(end) --- */
    
    private void updateView() {
    	if(entry.cover!=null)
    		((ImageView)findViewById(R.id.image)).setImageBitmap(entry.cover);
    	else
    		((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
    	
        ((TextView)findViewById(R.id.title)).setText(entry.title);
        ((TextView)findViewById(R.id.author)).setText(entry.author);
        
        ((RatingBar)findViewById(R.id.rating)).setRating(entry.info.rating);
        ((TextView)findViewById(R.id.description)).setText(entry.info.description);
        ((TextView)findViewById(R.id.description)).setMovementMethod(new ScrollingMovementMethod());
    }
    
    private void showError() {
    	((TextView)findViewById(R.id.title)).setText(R.string.book_not_found);
    	Toast.makeText(this, R.string.book_not_found_long, Toast.LENGTH_LONG).show();
    }
}

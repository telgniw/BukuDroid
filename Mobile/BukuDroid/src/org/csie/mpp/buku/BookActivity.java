package org.csie.mpp.buku;

import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.helper.BookUpdater;
import org.csie.mpp.buku.helper.BookUpdater.OnUpdatStatusChangedListener;

import com.flurry.android.FlurryAgent;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class BookActivity extends Activity implements OnUpdatStatusChangedListener, View.OnClickListener {
	public static final int REQUEST_CODE = 1437;
	public static final int RESULT_ISBN_INVALID = 633;
	public static final int RESULT_NOT_FOUND = 634;
	public static final int RESULT_DELETE = 643;
	public static final String CHECK_DUPLICATE = "duplicate";
	
	private DBHelper db;
	private BookEntry entry;
	private ActionBar actionBar;
	private Action actionAdd, actionDelete;
	private BookUpdater updater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        db = new DBHelper(this);

        Intent intent = getIntent();
        String isbn = intent.getStringExtra(App.ISBN);
        if(Util.checkIsbn(isbn) == false) {
        	setResult(RESULT_ISBN_INVALID);
        	finish();
        	return;
        }
        entry = BookEntry.get(db.getReadableDatabase(), isbn);
        
        actionBar = ((ActionBar)findViewById(R.id.actionbar));
        
        boolean updateAll = false;
        
        if(entry != null) {
        	if(intent.getBooleanExtra(CHECK_DUPLICATE, false))
        		Toast.makeText(this, R.string.book_already_exists, 3000).show();
        	
			actionDelete = new AbstractAction(R.drawable.ic_delete) {
				@Override
				public void performAction(View view) {
					Intent data = new Intent();
					data.putExtra(App.ISBN, entry.isbn);
					setResult(RESULT_DELETE, data);
					finish();
				}
			};        	

        	updateView(null);
        	actionBar.addAction(actionDelete);
        }
        else {
        	entry = new BookEntry();
        	entry.isbn = isbn;
        	updateAll = true;
        	
			actionAdd = new AbstractAction(R.drawable.ic_bookshelf) {
				@Override
				public void performAction(View view) {
					if(entry.insert(db.getWritableDatabase()) == false)
						Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
					
					Intent data = new Intent();
					data.putExtra(App.ISBN, entry.isbn);
					setResult(RESULT_OK, data);

					Toast.makeText(BookActivity.this, getString(R.string.added), App.TOAST_TIME).show();
					actionBar.removeAction(this);
				}
			};
        }	
        
        updater = BookUpdater.create(entry);
        updater.setOnUpdateFinishedListener(this);
  
       	if(updateAll)
       		updater.updateEntry();
       	else
       		updater.updateInfo();
    }
    
    @Override
    public void onStart() {
    	super.onStart();

		FlurryAgent.onStartSession(this, App.FLURRY_APP_KEY);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    	FlurryAgent.onEndSession(this);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	db.close();
    }

    /* --- OnUpdateFinishedListener	(start) --- */
    @Override
    public void onUpdateStart() {
    	updateView(null);
    }
    
    @Override
    public void onUpdateProgress() {
    	// TODO: enhance efficiency
    	updateView(null);
    }
	
	@Override
	public void onUpdateFinish(Status status) {
		switch(status) {
			case OK_ENTRY:
				actionBar.addAction(actionAdd);
				updater.updateInfo();
				updateView(status);
				break;
			case OK_INFO:
				updateView(status);
				break;
			case BOOK_NOT_FOUND:
	    		FlurryAgent.logEvent(App.FlurryEvent.BOOK_NOT_FOUND.toString());
	    		FlurryAgent.logEvent(entry.isbn);
	    		setResult(RESULT_NOT_FOUND);
	    		finish();
	    		break;
			default:
				Toast.makeText(this, R.string.unexpected_error, App.TOAST_TIME).show();
				break;
		}
	}
	/* --- OnUpdateFinishedListener	(end) --- */
    
    private void updateView(Status status) {
    	ImageView cover = ((ImageView)findViewById(R.id.image));
    	if(entry.cover != null)
    		cover.setImageBitmap(entry.cover);
    	else
    		cover.setImageResource(R.drawable.book);
    	
    	if(entry.title != null) {
    		((TextView)findViewById(R.id.title)).setText(entry.title);
    		((TextView)findViewById(R.id.author)).setText(entry.author);
            ((RatingBar)findViewById(R.id.rating)).setRating(entry.info.rating);
    	}
    	else {
        	if(status == null)
        		((TextView)findViewById(R.id.title)).setText(R.string.updating);
    	}
        
        TextView description = ((TextView)findViewById(R.id.description));
        if(entry.info.description != null) {
        	StringBuilder shortContent = new StringBuilder(); 
        	shortContent.append(entry.info.description.substring(0, Math.min(200, entry.info.description.length())));
        	if(entry.info.description.length()>200){
        		shortContent.append("...");
        		description.setOnClickListener(new OnClickListener() {
        	        @Override
        	        public void onClick(View v) {
        	        	new AlertDialog.Builder(BookActivity.this).setMessage(entry.info.description).show();               
        	        }
        	    });
        	}
        	description.setText(shortContent);
        	description.setMovementMethod(new ScrollingMovementMethod());
        }
        else {
        	if(status == null)
        		description.setText(R.string.updating);
        	else if(status == Status.OK_INFO)
        		description.setText(R.string.no_data);
        }
        
        LinearLayout reviews = (LinearLayout)findViewById(R.id.reviews);
        if(entry.info.reviews != null) {
        	if(reviews.getChildCount() == 1) {
        		reviews.removeAllViews();
        		
        		int size = entry.info.reviews.size();
	        	for(int i = 0; i < size; i++) {
	        		View view = getLayoutInflater().inflate(R.layout.list_item_review, null);
	        		StringBuilder shortContent = new StringBuilder();
	        		shortContent.append(entry.info.reviews.get(i).substring(0, Math.min(100, entry.info.reviews.get(i).length())));
	        		if(entry.info.reviews.get(i).length()>100)
	        			shortContent.append("...");
	        		TextView review = ((TextView)view.findViewById(R.id.list_review));
	            	review.setOnClickListener(this);
	            	review.setId(i);
	        		review.setText(shortContent);
	        		reviews.addView(view);        	  
	        	}
        	}
        }
        else {
        	if(status == null) {
        		if(reviews.getChildCount() == 0) {
        			View view = getLayoutInflater().inflate(R.layout.list_item_review, null);
        			((TextView)view.findViewById(R.id.list_review)).setText(R.string.updating);
        			reviews.addView(view);
        		}
        	}
        	else if(status == Status.OK_INFO)
        		description.setText(R.string.no_data);
        }
    }

    private AlertDialog dialog;
    
	@Override
	public void onClick(View v) {
		if(dialog == null)
			dialog = new AlertDialog.Builder(BookActivity.this).create();
		dialog.setMessage(entry.info.reviews.get(v.getId()));
		dialog.show();
	}
}

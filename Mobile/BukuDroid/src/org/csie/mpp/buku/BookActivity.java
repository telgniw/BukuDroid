package org.csie.mpp.buku;

import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;
import org.csie.mpp.buku.helper.BookUpdater;
import org.csie.mpp.buku.helper.BookUpdater.OnUpdateFinishedListener;
import org.csie.mpp.buku.view.DialogAction;

import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class BookActivity extends Activity {
	public static final String ISBN = "isbn";

	protected ActionBar actionbar;
	
	private DBHelper db;
	private BookEntry entry;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* initialize ActionBar */
        actionbar = (ActionBar)findViewById(R.id.actionbar);
        
        db = new DBHelper(this);

        entry = BookEntry.get(db.getReadableDatabase(), getIntent().getStringExtra(ISBN));
        
        if(entry != null)
        	updateView();
        else {
            actionbar.addAction(new DialogAction(this, 0, R.drawable.star));
            BookUpdater updater = new BookUpdater(entry);
            updater.setOnUpdateFinishedListener(new OnUpdateFinishedListener() {
				@Override
				public void OnUpdateFinished(BookEntry entry) {
					updateView();
				}
            });
        }
    }
    
    private void updateView() {
        ((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
        ((TextView)findViewById(R.id.title)).setText(entry.title);
        ((TextView)findViewById(R.id.author)).setText(entry.author);
        
        ((RatingBar)findViewById(R.id.rating)).setRating(3.5f);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	db.close();
    }
}

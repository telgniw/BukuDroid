package org.csie.mpp.buku;

import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* initialize ActionBar */
        actionbar = (ActionBar)findViewById(R.id.actionbar);
        
        db = new DBHelper(this);

        if(!BookEntry.exists(db.getReadableDatabase(), getIntent().getStringExtra(ISBN))) {
            actionbar.addAction(new DialogAction(this, 0, R.drawable.star));
        }
        
        ((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
        ((TextView)findViewById(R.id.title)).setText("title");
        ((TextView)findViewById(R.id.author)).setText("author");
        ((RatingBar)findViewById(R.id.rating)).setRating(3.5f);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	db.close();
    }
}

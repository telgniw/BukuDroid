package org.csie.mpp.buku;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class BookActivity extends Activity {
	public static final String ISBN = "isbn";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        ((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
        ((TextView)findViewById(R.id.title)).setText("title");
        ((TextView)findViewById(R.id.author)).setText("author");
        ((RatingBar)findViewById(R.id.rating)).setRating(3.5f);
    }
}

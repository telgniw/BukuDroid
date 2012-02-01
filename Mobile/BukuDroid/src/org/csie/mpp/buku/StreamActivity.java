package org.csie.mpp.buku;

import java.net.URL;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.android.SessionStore;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StreamActivity extends Activity {
	public static final int REQUEST_CODE = 1438;
	public static final String POST_ID = "POST_ID";
	public static final String NAME = "NAME";
	public static final String MESSAGE = "MESSAGE";
	public static final String BOOK = "BOOK";
	public static final String AUTHOR = "AUTHOR";
	public static final String LINK = "LINK";
	public static final String IMAGE = "IMAGE";

	private ActionBar actionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream);

        // initialize FB
        SessionStore.restore(App.fb, this);
        
        actionBar = ((ActionBar)findViewById(R.id.actionbar));
        
        Intent intent = getIntent();
        fetchPost(intent.getStringExtra(StreamActivity.POST_ID));
        actionBar.setTitle(getString(R.string.text_posted_by) + " " + intent.getStringExtra(NAME));
        
        String message = intent.getStringExtra(MESSAGE);
        if(message.length() > 0)
        	((TextView)findViewById(R.id.message)).setText(message);
        else
        	((TextView)findViewById(R.id.message)).setVisibility(TextView.GONE);
        
        ((TextView)findViewById(R.id.title)).setText(intent.getStringExtra(BOOK));
        ((TextView)findViewById(R.id.author)).setText(intent.getStringExtra(AUTHOR));
        
        try {
        	((ImageView)findViewById(R.id.image)).setImageBitmap((Bitmap)intent.getParcelableExtra(IMAGE));
        }
        catch(Exception e) {
        	Log.e(App.TAG, e.toString());
        }

        final String link = intent.getStringExtra(StreamActivity.LINK);
        ((LinearLayout)findViewById(R.id.book)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// the link will be our fan page link if the book link is not available when posted
		        if(link == null || link.equals(App.FB_FAN_PAGE))
		        	Toast.makeText(StreamActivity.this, R.string.msg_book_not_found, App.TOAST_TIME);
		        else {
		        	Intent intent = new Intent(StreamActivity.this, BookActivity.class);
		        	intent.putExtra(BookActivity.LINK, link);
		        	startActivityForResult(intent, BookActivity.REQUEST_CODE);
		        }
			}
        });
	}
	
	private void fetchPost(String post_id) {
		final LinearLayout comments = (LinearLayout)findViewById(R.id.comments);
		final LayoutInflater inflater = getLayoutInflater();
		
		AsyncTask<String, View, Boolean> async = new AsyncTask<String, View, Boolean>() {
			@Override
			protected Boolean doInBackground(String... args) {
				try {
					Bundle params = new Bundle();
					params.putString("fields", "from,message,created_time");
					
					String response = App.fb.request(args[0] + "/comments", params);
					JSONArray data = new JSONObject(response).getJSONArray("data");
					for(int i = 0; i < data.length(); i++) {
		        		View view = inflater.inflate(R.layout.list_item_comment, null);
		        		
						try {
							JSONObject json = data.getJSONObject(i);
							
							JSONObject from = json.getJSONObject("from");
							((TextView)view.findViewById(R.id.list_name)).setText(from.getString("name"));
							((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(
								Util.urlToImage(new URL("http://graph.facebook.com/" + from.getString("id") + "/picture"))
							);
							
							((TextView)view.findViewById(R.id.list_comment)).setText(json.getString("message"));
							((TextView)view.findViewById(R.id.list_time)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").parse(
								json.getString("created_time").replace('T', ' ')
							).toLocaleString());
						}
						catch(Exception e) {
							continue;
						}
						
						publishProgress(view);
					}
				}
				catch(Exception e) {
					Log.e(App.TAG, e.toString());
				}
				
				return true;
			}
			
			@Override
			protected void onPreExecute() {
				TextView updating = new TextView(StreamActivity.this);
				updating.setText(R.string.msg_updating);
				comments.addView(updating);
			}
			
			@Override
			protected void onProgressUpdate(View... progresses) {
				comments.addView(progresses[0]);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				comments.removeViewAt(0);
			}
		};
		
		async.execute(post_id);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode) {
    		case BookActivity.REQUEST_CODE:
    			if(resultCode == Activity.RESULT_CANCELED) {
    				// do nothing
    			}
    			else if(resultCode == BookActivity.RESULT_ISBN_INVALID) {
    				Toast.makeText(this, getString(R.string.msg_invalid_isbn), App.TOAST_TIME).show();
    			}
    			else if(resultCode == BookActivity.RESULT_NOT_FOUND) {
    				Toast.makeText(this, getString(R.string.msg_book_not_found), App.TOAST_TIME).show();
    			}
    			else {
    				setResult(resultCode);
    				finish();
    			}
    			break;
    		default:
    			break;
    	}
    }
}

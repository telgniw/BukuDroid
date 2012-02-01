package org.csie.mpp.buku.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.R;
import org.csie.mpp.buku.StreamActivity;
import org.csie.mpp.buku.Util;
import org.csie.mpp.buku.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class StreamManager extends ViewManager implements OnItemClickListener {
	private List<Stream> streams;
	private ArrayAdapter<Stream> adapter;
	
	public static class Stream {
		private String id;
		private String name;
		private String message;
		private String book;
		private String author;
		private String link;
		private Bitmap image;
		private Date time;
		
		@Override
		public boolean equals(Object o) {
			return id == ((Stream)o).id;
		}
	}
	
	public static class StreamAdapter extends ArrayAdapter<Stream> {
		private final LayoutInflater inflater;
		private final int resourceId;
		private final List<Stream> entries;
		
		private final String says;
		
		public StreamAdapter(Activity activity, int resource, List<Stream> list) {
			super(activity, resource, list);
			
			inflater = activity.getLayoutInflater();
			resourceId = resource;
			entries = list;
			
			says = " " + activity.getString(R.string.text_says);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Stream stream = entries.get(position);
			View view = inflater.inflate(resourceId, parent, false);
			if(stream.message != null) {
				String message = stream.name + says + stream.message;
				((TextView)view.findViewById(R.id.list_message)).setText(message);
			}
			if(stream.image != null)
				((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(stream.image);
			((TextView)view.findViewById(R.id.list_name)).setText(stream.book);
			((TextView)view.findViewById(R.id.list_author)).setText(stream.author);
			((TextView)view.findViewById(R.id.list_time)).setText(stream.time.toLocaleString());
			return view;
		}
	}
	
	protected class Updater extends AsyncTask<Integer, Integer, Boolean> {
		private TextView info;
		
		public Updater() {
			info = (TextView)getFrame().findViewById(R.id.text);
			info.setBackgroundColor(0xaaffffff);
			info.setText(R.string.msg_updating);
		}
		
		@Override
		protected Boolean doInBackground(Integer... args) {
			Bundle params = new Bundle();
			params.putString("q", "SELECT post_id,actor_id,message,attachment,created_time FROM stream WHERE filter_key IN ("
					+ "SELECT filter_key FROM stream_filter WHERE uid = me() AND type = 'newsfeed'"
					+ ") AND app_id = " + App.FB_APP_ID + " LIMIT 100");
			
			try {
				String response = App.fb.request("fql", params);
				Bundle fields = new Bundle();
				fields.putString("fields", "first_name");
				
				int counter = 0;
				while(response != null) {
					JSONArray data = new JSONObject(response).getJSONArray("data");
					
					for(int i = 0; i < data.length(); i++) {
						publishProgress(++counter);
						
						try {
							JSONObject json = data.getJSONObject(i);
							Stream stream = new Stream();
							stream.id = json.getString("post_id");
							
							if(streams.contains(stream))
								continue;
							
							try {
								String user_res = App.fb.request(json.getString("actor_id"), fields);
								stream.name = new JSONObject(user_res).getString("first_name");
							}
							catch(Exception e) {
								Log.e(App.TAG, e.toString());
							}
							
							stream.message = json.getString("message");
	
							stream.time = new Date(Long.parseLong(json.getString("created_time")) * 1000);
	
							json = json.getJSONObject("attachment");
							
							stream.book = json.getString("name");
							stream.author = json.getString("caption");
							stream.link = json.getString("href");
	
							try {
								stream.image = Util.urlToImage(new URL(json.getJSONArray("media").getJSONObject(0).getString("src")));
							}
							catch(Exception e) {
								// icon not found
							}
	
							streams.add(stream);
						}
						catch(Exception e) {
							Log.e(App.TAG, e.toString());
						}
					}
					
					response = null;
				}
			}
			catch(Exception e) {
				Log.e(App.TAG, e.toString());
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progresses) {
			adapter.notifyDataSetChanged();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			info.setBackgroundColor(0x00ffffff);
			info.setText("");
			
			if(result) {
				if(streams.size() == 0)
					createNoStreamView();
				else
					adapter.notifyDataSetChanged();
			}
		}
	}
	
	public StreamManager(Activity activity, DBHelper helper) {
		super(activity, helper);
		
		streams = new ArrayList<Stream>();
		adapter = new StreamAdapter(activity, R.layout.list_item_stream, streams);
	}
	
	public void remove(String post_id) {
		for(int i = 0; i < streams.size(); i++) {
			if(streams.get(i).id.equals(post_id)) {
				streams.remove(i);
				break;
			}
		}
		
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void updateView() {
		createStreamView();
		
		new Updater().execute(0);
	}
	
    private void createStreamView() {
		LinearLayout frame = getFrame();
		TextView text = (TextView)frame.findViewById(R.id.text);
		text.setText("");
		
		ListView list = (ListView)frame.findViewById(R.id.list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	private void createNoStreamView() {
		LinearLayout frame = getFrame();
		TextView text = (TextView)frame.findViewById(R.id.text);
		text.setText(R.string.msg_no_streams);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Stream stream = streams.get(position);
		Intent intent = new Intent(activity, StreamActivity.class);
		intent.putExtra(StreamActivity.POST_ID, stream.id);
		intent.putExtra(StreamActivity.NAME, stream.name);
		intent.putExtra(StreamActivity.MESSAGE, stream.message);
		intent.putExtra(StreamActivity.BOOK, stream.book);
		intent.putExtra(StreamActivity.AUTHOR, stream.author);
		intent.putExtra(StreamActivity.LINK, stream.link);
		intent.putExtra(StreamActivity.IMAGE, stream.image);
		activity.startActivityForResult(intent, StreamActivity.REQUEST_CODE);
	}
}

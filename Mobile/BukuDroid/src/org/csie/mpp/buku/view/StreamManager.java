package org.csie.mpp.buku.view;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.csie.mpp.buku.App;
import org.csie.mpp.buku.R;
import org.csie.mpp.buku.Util;
import org.csie.mpp.buku.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.android.BaseRequestListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class StreamManager extends ViewManager {
	private List<Stream> streams;
	private ArrayAdapter<Stream> adapter;
	
	public StreamManager(Activity activity, DBHelper helper) {
		super(activity, helper);
	}
	
	private static class Stream {
		private String id;
		private String message;
		private String book;
		private String link;
		private Bitmap pic;
		private Date time;
		
		public Stream(String id) {
			this.id = id;
		}
		public void setDate(String dateString){
			dateString = dateString.replace("T", " ");
			dateString = dateString.replace("+0000", "");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try{
				time = sdf.parse(dateString);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}

	private void createView(LinearLayout frame) {
		Log.d("APP", "QQ" + streams.size());
		if(streams.size() == 0) {
			TextView text = (TextView)frame.findViewById(R.id.text);
			text.setText("You have no streams. QQ");
		}
		else {
			TextView text = (TextView)frame.findViewById(R.id.text);
			text.setText("");
			
			adapter = new ArrayAdapter<Stream>(activity, R.layout.list_item_stream, streams) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					Stream stream = streams.get(position);
					LayoutInflater inflater = activity.getLayoutInflater();
					View view = inflater.inflate(R.layout.list_item_stream, null);
					((TextView)view.findViewById(R.id.list_stream_message)).setText(stream.message);
					if ( stream.pic != null)
						((ImageView)view.findViewById(R.id.list_stream_image)).setImageBitmap(stream.pic);
					if ( stream.book != null )
						((TextView)view.findViewById(R.id.list_stream_name)).setText(stream.book);
					((TextView)view.findViewById(R.id.list_stream_time)).setText(""+stream.time);
					return view;
				}
			};
			
			ListView list = (ListView)frame.findViewById(R.id.list);
			list.setAdapter(adapter);
		}
	}

	@Override
	protected void updateView() {
		final LinearLayout frame = getFrame();
		
		if(streams != null)
			createView(frame);
		else {
			Bundle params = new Bundle();
			params.putString("fields", "id,message,name,picture,link,application,created_time");
			App.fb_runner.request("me/feed", params, new BaseRequestListener() {
				@Override
				public void onComplete(String response, Object state) {
					streams = new ArrayList<Stream>();
					
					try {
						//while(response != null) {
							JSONObject json = new JSONObject(response);
							JSONArray data = json.getJSONArray("data");
							for(int i = 0; i < data.length(); i++) {
								JSONObject p = data.getJSONObject(i);
								Log.d("APP", "QQ"+p);
								if(p.has("application")) {
									if (p.get("application") == JSONObject.NULL)
										continue;
											
									if ( !p.getJSONObject("application").getString("name").equals("BukuDroid"))
										continue;
									Stream stream = new Stream(p.getString("id"));
									if (p.has("name"))
										stream.book = p.getString("name");
									stream.message = p.getString("message");
									stream.pic = Util.urlToImage(new URL(p.getString("picture")));
									stream.link = p.getString("link");
									stream.setDate(p.getString("created_time"));
									streams.add(stream);
								}
							}
							
							/*JSONObject paging = json.getJSONObject("paging");
							if(!paging.has("next"))
								response = null;
							else {
								URL url = new URL(paging.getString("next"));
								response = Util.urlToString(url);
								
							}*/
							
						//}
					}
					catch(Exception e) {
						Log.d("exception", e.toString());
						streams.clear();
					}
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							createView(frame);
						}
					});
				}
			});
		}
	}
}

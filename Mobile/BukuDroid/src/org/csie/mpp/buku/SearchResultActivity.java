package org.csie.mpp.buku;

import java.util.ArrayList;
import java.util.List;

import org.csie.mpp.buku.R;
import org.csie.mpp.buku.db.BookEntry;
import org.csie.mpp.buku.db.DBHelper;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchResultActivity extends Activity {
	
	private Bundle bundle;
	private Intent intent;
	private String query;
	
	private BookEntryAdapter booklistAdapter;
	private ArrayList<BookEntry> books;
	private ListView booklist;
		
	private DBHelper db;
	private BookEntry[] entries;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        db = new DBHelper(this);
        
        intent = this.getIntent();
        bundle = intent.getExtras();
        
        query = bundle.getString("query");
        Toast popup = Toast.makeText(this, query, Toast.LENGTH_SHORT);
    	popup.show();
    	
        
        entries = BookEntry.search(db.getReadableDatabase(), query);//BookEntry.queryAll(db.getReadableDatabase());
        //entries = BookEntry.queryAll(db.getReadableDatabase());
        
        books = new ArrayList<BookEntry>();
        for(BookEntry es: entries)
        {
        	books.add(es);
        }
        Log.d("BuKuDroid", ""+books.size());
        booklistAdapter = new BookEntryAdapter(this, R.layout.list_item_book, books);
        
        booklist = (ListView)findViewById(R.id.inner_list);
        booklist.setAdapter(booklistAdapter);
        booklist.setOnItemClickListener(new OnItemClickListener(){

        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		BookEntry book = (BookEntry)booklist.getItemAtPosition(position);
        		startBookActivity(book.isbn, false);
        	}
        });
        
        booklistAdapter.notifyDataSetChanged();
        
        
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	db.close();
    }

    public void startBookActivity(String isbn, boolean checkDuplicate) {
    	Intent intent = new Intent(this, BookActivity.class);
		intent.putExtra(BookActivity.ISBN, isbn);
		intent.putExtra(BookActivity.CHECK_DUPLICATE, checkDuplicate);
		//startActivityForResult(intent, BookActivity.REQUEST_CODE);
		startActivity(intent);
    }
    private static class BookEntryAdapter extends ArrayAdapter<BookEntry> {
		private LayoutInflater inflater;
		private int resourceId;
		private List<BookEntry> entries;
		
		public BookEntryAdapter(Activity activity, int resource, List<BookEntry> list) {
			super(activity, resource, list);
			
			inflater = activity.getLayoutInflater();
			resourceId = resource;
			entries = list;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BookEntry entry = entries.get(position);
			View view = inflater.inflate(resourceId, parent, false);
			// TODO: using ImageLoader instead
			((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(entry.cover);
			((TextView)view.findViewById(R.id.list_title)).setText(entry.title);
			((TextView)view.findViewById(R.id.list_author)).setText(entry.author);
			return view;
		}
	}
 }
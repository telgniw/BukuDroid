package org.csie.mpp.buku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ScanActivity extends Activity implements OnClickListener {
	public static final int REQUEST_CODE = 1436;
	public static final String ISBN = "isbn";
	
	protected EditText isbn;
	protected Button add;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);
        
        isbn = (EditText)findViewById(R.id.isbn);
        add = (Button)findViewById(R.id.add);
        
        add.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		String input = isbn.getText().toString();
		
		Intent data = new Intent();
		data.putExtra(ScanActivity.ISBN, input);
		setResult(RESULT_FIRST_USER, data);
		finish();
	}
}

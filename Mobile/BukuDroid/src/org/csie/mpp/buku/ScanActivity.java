package org.csie.mpp.buku;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ScanActivity extends Activity implements OnClickListener {
	public static final int REQUEST_CODE = 1436;
	public static final String ISBN = "isbn";
	
	protected EditText isbn;
	protected Button barcode;
	protected Button add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);

        isbn = (EditText)findViewById(R.id.isbn);
        barcode = (Button)findViewById(R.id.barcode);
        add = (Button)findViewById(R.id.add);

        barcode.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				//TODO(ianchou): integrate bar code scanning
		        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		        intent.putExtra("SCAN_MODE", "ONE_D_MODE");
		        String targetAppPackage = findTargetAppPackage(intent);
		        if (targetAppPackage == null) {
		          showDownloadDialog();
		          return;
		        }
		        startActivityForResult(intent, 0);
			}
        	
        });
        
        add.setOnClickListener(this);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                isbn.setText(contents);
            } else if (resultCode == RESULT_CANCELED) {
                //TODO(ianchou): show error message
            }
        }

    }

	@Override
	public void onClick(View v) {
		String input = isbn.getText().toString();
		
		Intent data = new Intent();
		data.putExtra(ScanActivity.ISBN, input);
		setResult(RESULT_FIRST_USER, data);
		finish();
	}

	private String findTargetAppPackage(Intent intent) {
	   PackageManager pm = ScanActivity.this.getPackageManager();
	   List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	   if (availableApps!=null && !availableApps.isEmpty()) {
		   return availableApps.get(0).activityInfo.packageName;
	   }
	   return null;
	}

	private AlertDialog showDownloadDialog() {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(ScanActivity.this);
		downloadDialog.setTitle("Install Barcode Scanner");
		downloadDialog.setMessage("This funcion requires Barcode Scanner. Would you like to install it?");
		downloadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://details?id=com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					ScanActivity.this.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {
					// Hmm, market is not installed
					Log.w(ScanActivity.class.getSimpleName(), "Android Market is not installed; cannot install Barcode Scanner");
				}
			}

		});
		downloadDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialogInterface, int i) {}

		});
		return downloadDialog.show();
	}

}

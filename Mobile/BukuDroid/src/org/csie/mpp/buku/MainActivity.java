package org.csie.mpp.buku;

import android.os.Bundle;

import com.markupartist.android.widget.ActionBar;

public class MainActivity extends BaseActivity {
	protected ActionBar actionbar;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        actionbar = (ActionBar)findViewById(R.id.actionbar);
    }
}

package org.csie.mpp.buku.listener;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;

public interface ContextMenuCallback {
	public void onCreateContextMenu(ContextMenu menu, ContextMenuInfo menuInfo);
	public void onSelectContextMenu(MenuItem item);
}

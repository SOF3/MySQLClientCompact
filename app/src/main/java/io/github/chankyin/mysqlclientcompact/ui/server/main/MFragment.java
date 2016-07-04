package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.support.v4.app.Fragment;

public abstract class MFragment extends Fragment{
	/**
	 * Triggered when back button is pressed and fragment is the active page
	 *
	 * @return true to prevent default
	 */
	public boolean onBackPressed(){
		return false;
	}

	public void onSelected(){
	}
}

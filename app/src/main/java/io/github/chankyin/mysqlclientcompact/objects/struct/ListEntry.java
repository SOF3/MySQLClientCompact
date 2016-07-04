package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;

public interface ListEntry{
	public CharSequence getName();

	@Deprecated
	public View getViewOnce(Context ctx);
	@Deprecated
	public void onFirstDisplay(ServerMainActivity activity);
}

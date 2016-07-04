package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;
import lombok.Getter;
import lombok.experimental.Builder;

@Builder
public class Column implements ListEntry{
	@Getter private String name;
	@Getter private String type;
	@Getter private boolean nullable;
	@Getter private String defaultValue;
	@Getter private String key;
	@Getter private String extra;

	@Override
	@Deprecated
	public View getViewOnce(Context ctx){
		LinearLayout layout = new LinearLayout(ctx);
		// TODO 
		return layout;
	}

	@Override
	@Deprecated
	public void onFirstDisplay(ServerMainActivity activity){

	}
}

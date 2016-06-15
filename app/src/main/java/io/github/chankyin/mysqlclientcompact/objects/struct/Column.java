package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.serverui.ServerMainActivity;
import lombok.Getter;
import lombok.Setter;

public class Column implements ListEntry{
	@Getter @Setter private String name;
	@Getter @Setter private String type;

	@Override
	public void doQuery(ServerMainActivity activity){
	}

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

package io.github.chankyin.mysqlclientcompact.objects.result;

import android.content.ClipData;
import android.content.Context;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class IntegerCell extends Cell<Integer>{
	private final boolean signed;

	public IntegerCell(int columnId, String name, int type, boolean signed, boolean isPrimaryKey){
		super(columnId, name, type, isPrimaryKey);
		this.signed = signed;
	}

	public void setValue(int i){
		value = i;
	}

	@Override
	public View toViewer(Context ctx){
		return Main.createTextView(ctx, String.format(Locale.ENGLISH, "%s", value));
	}

	@Override
	public ClipData getCopyData(Context ctx){
		return simpleClipData("%s", getValue());
	}

	@Override
	public void fetch(ResultSet set) throws SQLException{
		setValue(set.getInt(getColumnId()));
	}
}

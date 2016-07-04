package io.github.chankyin.mysqlclientcompact.objects.result;

import android.content.ClipData;
import android.content.Context;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StringCell extends Cell<String>{
	public StringCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
		super(columnId, columnName, columnType, isPrimaryKey);
	}

	@Override
	public View toViewer(Context ctx){
		return Main.createTextView(ctx, value);
	}

	@Override
	public ClipData getCopyData(Context ctx){
		return simpleClipData(getValue());
	}

	@Override
	public void fetch(ResultSet set) throws SQLException{
		setValue(set.getString(getColumnId()));
	}

	public void setValue(String string){
		value = string;
	}
}

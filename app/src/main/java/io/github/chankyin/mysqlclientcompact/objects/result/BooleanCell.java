package io.github.chankyin.mysqlclientcompact.objects.result;

import android.content.ClipData;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanCell extends Cell<Boolean>{
	public BooleanCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
		super(columnId, columnName, columnType, isPrimaryKey);
	}

	@Override
	public View toViewer(Context ctx){
		CheckBox box = new CheckBox(ctx);
		box.setEnabled(false);
		box.setChecked(value);
		return box;
	}

	@Override
	public ClipData getCopyData(Context ctx){
		return simpleClipData(getValue() ? "true" : "false");
	}

	@Override
	public void fetch(ResultSet set) throws SQLException{
		setValue(set.getBoolean(getColumnId()));
	}

	public void setValue(boolean bool){
		value = bool;
	}
}

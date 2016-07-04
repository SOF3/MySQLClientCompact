package io.github.chankyin.mysqlclientcompact.objects.result;

import android.content.ClipData;
import android.content.Context;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeCell extends Cell<Date>{
	public DateTimeCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
		super(columnId, columnName, columnType, isPrimaryKey);
	}

	@Override
	public View toViewer(Context ctx){
		return Main.createTextView(ctx, SimpleDateFormat.getInstance().format(value));
	}

	@Override
	public ClipData getCopyData(Context ctx){
		return simpleClipData(SimpleDateFormat.getInstance().format(getValue()));
	}

	@Override
	public void fetch(ResultSet set) throws SQLException{
		setValue(set.getTimestamp(getColumnId()).getTime());
	}

	/**
	 * @param timestamp number of milliseconds since Unix epoch
	 */
	public void setValue(long timestamp){
		setValue(timestamp, true);
	}

	public void setValue(long timestamp, boolean millis){
		value = new Date(timestamp * (millis ? 1 : 1000));
	}

	/**
	 * Reminder: {@link java.sql.Timestamp Timestamp} extends {@link Date}
	 *
	 * @param timestamp
	 */
	public void setValue(Date timestamp){
		value = timestamp;
	}
}

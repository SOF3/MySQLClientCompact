package io.github.chankyin.mysqlclientcompact.mysql.result;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.MyApplication;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.sql.Types.*;

@Getter
public abstract class Cell<CellType> implements Cloneable{
	private final int columnId;
	private final String name;
	private final int type;
	@Setter private boolean isPrimaryKey;

	protected CellType value;

	protected Cell(int columnId, String name, int type, boolean isPrimaryKey){
		this.columnId = columnId;
		this.name = name;
		this.type = type;
		this.isPrimaryKey = isPrimaryKey;
	}

	public abstract View toViewer(Context ctx);

	@Override
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}

	public static Cell<?> create(int columnId, String columnName, int columnType, boolean signed, boolean isPrimaryKey){
		Cell<?> ret;
		switch(columnType){
			case BIGINT:
			case BIT:
			case INTEGER:
			case ROWID:
			case SMALLINT:
			case TINYINT:
				ret = new IntegerCell(columnId, columnName, columnType, signed, isPrimaryKey);
				break;
			case DECIMAL:
			case DOUBLE:
			case FLOAT:
			case NUMERIC:
			case REAL:
				ret = new DecimalCell(columnId, columnName, columnType, signed, isPrimaryKey);
				break;
			case BOOLEAN:
				ret = new BooleanCell(columnId, columnName, columnType, isPrimaryKey);
				break;
			case BINARY:
			case BLOB:
			case LONGVARBINARY:
			case VARBINARY:
				ret = new BinaryCell(columnId, columnName, columnType, isPrimaryKey);
				break;
			case CHAR:
			case CLOB:
			case LONGNVARCHAR:
			case LONGVARCHAR:
			case NCHAR:
			case NVARCHAR:
			case NCLOB:
			case VARCHAR:
				ret = new StringCell(columnId, columnName, columnType, isPrimaryKey);
				break;
			case DATE:
			case TIME:
			case TIMESTAMP:
				ret = new DateTimeCell(columnId, columnName, columnType, isPrimaryKey);
				break;
			default:
				throw new UnsupportedOperationException("Unknown data type: " +
						MyApplication.findConstantNameInClass(Types.class, columnType, Integer.toString(columnType)));
		}
		return ret;
	}

	public abstract void fetch(ResultSet set) throws SQLException;

	public static class IntegerCell extends Cell<Integer>{
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
			return MyApplication.createTextView(ctx, String.format(Locale.ENGLISH, "%s", value));
		}

		@Override
		public void fetch(ResultSet set) throws SQLException{
			setValue(set.getInt(getColumnId()));
		}
	}

	public static class DecimalCell extends Cell<Double>{
		private final boolean signed;

		public DecimalCell(int columnId, String name, int type, boolean signed, boolean isPrimaryKey){
			super(columnId, name, type, isPrimaryKey);
			this.signed = signed;
		}

		public void setValue(double d){
			value = d;
		}

		@Override
		public View toViewer(Context ctx){
			return MyApplication.createTextView(ctx, String.format(Locale.ENGLISH, "%s", value));
		}

		@Override
		public void fetch(ResultSet set) throws SQLException{
			setValue(set.getDouble(getColumnId()));
		}
	}

	public static class BooleanCell extends Cell<Boolean>{
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
		public void fetch(ResultSet set) throws SQLException{
			setValue(set.getBoolean(getColumnId()));
		}

		public void setValue(boolean bool){
			value = bool;
		}
	}

	public static class BinaryCell extends Cell<byte[]>{
		public BinaryCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
			super(columnId, columnName, columnType, isPrimaryKey);
		}

		@Override
		public View toViewer(Context ctx){
			LinearLayout layout = new LinearLayout(ctx);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			TextView view = new TextView(ctx);
			view.setText("X'");
			layout.addView(view);
			view = new TextView(ctx);
			view.setTypeface(Typeface.MONOSPACE);
			view.setText(MyApplication.bin2hex(value, " ", value.length));
			layout.addView(view);
			view = MyApplication.createTextView(ctx, "'");
			view.setTypeface(Typeface.MONOSPACE);
			layout.addView(view);
			return layout;
		}

		@Override
		public void fetch(ResultSet set) throws SQLException{
			InputStream is = set.getBinaryStream(getColumnId());
			try{
				setValue(IOUtils.toByteArray(is));
			}catch(IOException e){
				e.printStackTrace();
				setValue(new byte[0]);
			}
		}

		public void setValue(byte[] array){
			value = array;
		}
	}

	public static class StringCell extends Cell<String>{
		public StringCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
			super(columnId, columnName, columnType, isPrimaryKey);
		}

		@Override
		public View toViewer(Context ctx){
			return MyApplication.createTextView(ctx, value);
		}

		@Override
		public void fetch(ResultSet set) throws SQLException{
			setValue(set.getString(getColumnId()));
		}

		public void setValue(String string){
			value = string;
		}
	}

	public static class DateTimeCell extends Cell<Date>{
		public DateTimeCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
			super(columnId, columnName, columnType, isPrimaryKey);
		}

		@Override
		public View toViewer(Context ctx){
			return MyApplication.createTextView(ctx, SimpleDateFormat.getDateTimeInstance().format(value));
		}

		@Override
		public void fetch(ResultSet set) throws SQLException{
			setValue(set.getDate(getColumnId()));
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
}

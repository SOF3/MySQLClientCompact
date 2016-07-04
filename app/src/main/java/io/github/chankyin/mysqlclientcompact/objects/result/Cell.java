package io.github.chankyin.mysqlclientcompact.objects.result;

import android.content.ClipData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.Main;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

import static java.sql.Types.*;

@Getter
public abstract class Cell<CellType> implements Cloneable, Serializable{
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

	public abstract ClipData getCopyData(Context ctx);

	protected ClipData simpleClipData(@NonNull String format){
		return ClipData.newPlainText("Cell", format);
	}

	protected ClipData simpleClipData(@NonNull String format, Object... args){
		return ClipData.newPlainText("Cell", String.format(Locale.getDefault(), format, args));
	}

	@Override
	public String toString(){
		StringWriter out = new StringWriter();
		JsonWriter writer = new JsonWriter(out);
		try{
			writer.beginObject();
			writer
					.name("columnId").value(columnId)
					.name("name").value(name)
					.name("type").value(type)
					.name("isPrimaryKey").value(isPrimaryKey)
					.name("value").value(String.valueOf(value));
			writer.endObject();
			writer.close();
		}catch(Exception e){
			return "\"error\"";
		}
		return out.toString();
	}

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
				UnsupportedOperationException ex = new UnsupportedOperationException("Unknown data type: " +
						Main.findConstantNameInClass(Types.class, columnType, Integer.toString(columnType)));
				Log.e("MySQL query", "Unexpected type", ex);
				throw ex;
		}
		return ret;
	}

	public abstract void fetch(ResultSet set) throws SQLException;
}

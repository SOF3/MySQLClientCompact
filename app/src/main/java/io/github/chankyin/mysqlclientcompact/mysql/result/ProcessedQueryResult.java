package io.github.chankyin.mysqlclientcompact.mysql.result;

import android.util.Log;
import com.mysql.jdbc.ResultSetMetaData;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ProcessedQueryResult implements ProcessedResult{
	private final String query;
	private final String command;
	private final Row header;
	private final Row[] values;

	public ProcessedQueryResult(ConnectionThread thread, String query, String command, ResultSet set) throws SQLException{
		this.query = query;
		this.command = command;

		ResultSetMetaData data = (ResultSetMetaData) set.getMetaData();
		header = new Row(Row.HEADER_ROW_ID, new Cell[data.getColumnCount()]);

		for(int columnId = 1; columnId <= header.contents.length; columnId++){
			Log.d("MySQL query", String.format(
					"Catalog: %s, Schema: %s, Table: %s",
					data.getCatalogName(columnId), data.getSchemaName(columnId), data.getTableName(columnId)
			));
			String[] keys = thread.getPrimaryKeys(data.getCatalogName(columnId), data.getTableName(columnId));
			String columnName = data.getColumnName(columnId);
			int columnType = data.getColumnType(columnId);
			Log.d("MySQL query", "Column: " + columnName + " (Type: " + columnType + ")");
			boolean isPrimaryKey = false;
			for(String key : keys){
				if(key.equals(columnName)){
					isPrimaryKey = true;
					break;
				}
			}
			header.contents[columnId - 1]
					= Cell.create(columnId, columnName, columnType, data.isSigned(columnId), isPrimaryKey);
		}

		List<Row> rows = new ArrayList<>();
		int rowId = 0;

		while(set.next()){
			Row row = header.clone(rowId++);
			for(Cell<?> cell : row.contents){
				cell.fetch(set);
			}
			rows.add(row);
		}
		values = new Row[rows.size()];
		rows.toArray(values);
	}

	@Override
	public Type getQueryType(){
		return Type.QUERY;
	}
}

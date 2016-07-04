package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.view.View;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedErrorResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedQueryResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.result.Row;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainPage;
import io.github.chankyin.mysqlclientcompact.ui.server.main.StructureFragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableStructure extends Structure<Column, SchemaStructure> implements ListEntry{
	private HorizontalScrollView horizScroll;
	private TableLayout tableLayout;

	public TableStructure(SchemaStructure parent, String name){
		super(parent, name);
	}

	@Override
	public void doQuery(final ServerMainActivity activity){
		activity.getConnectionThread().scheduleAsyncQuery("DESCRIBE `" + getParent().getName() + "`.`" + getName() + "`", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				if(result.getQueryType() == ProcessedResult.Type.ERROR){
					SQLException e = ((ProcessedErrorResult) result).getException();
					Toast.makeText(activity, activity.getString(R.string.ServerMain_Structure_QueryFailure, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
				}else if(result.getQueryType() == ProcessedResult.Type.QUERY){
					ProcessedQueryResult queryResult = (ProcessedQueryResult) result;
					List<Column> columns = new ArrayList<>(queryResult.getValues().length);
//					Log.d("MCC", ((ProcessedQueryResult) result).getValues()[0].toString());
					for(Row row : queryResult.getValues()){
						Column column = Column.builder()
								.name((String) row.getCellValue("COLUMN_NAME"))
								.type((String) row.getCellValue("COLUMN_TYPE"))
								.nullable(((String) row.getCellValue("IS_NULLABLE")).equalsIgnoreCase("YES"))
								.defaultValue((String) row.getCellValue("COLUMN_DEFAULT"))
								.key((String) row.getCellValue("COLUMN_KEY"))
								.extra((String) row.getCellValue("EXTRA"))
								.build();
						columns.add(column);
					}
					setContents(columns);

					StructureFragment fragment = (StructureFragment) activity.getPage(ServerMainPage.STRUCTURE);
					assert fragment != null;
					if(fragment.getDisplayedStructure() == TableStructure.this){
						fragment.setDisplayedStructure(TableStructure.this);
					}
				}else{
					throw new AssertionError("Unknown result type: " + result.getQueryType().name());
				}
			}
		});
	}

	@Override
	public void populateFragment(StructureFragment fragment){
		if(isContentLoaded()){
			fragment.getContentView().addView(getScroll(fragment.getActivity()));
		}else{
			super.populateFragment(fragment);
		}
	}

	public TableLayout getTableLayout(Context ctx){
		if(tableLayout == null){
			tableLayout = new TableLayout(ctx);
			TableRow row = new TableRow(ctx);
			Main.addViewWithBorderToRow(row, Main.createTextView(ctx, R.string.ServerMain_Structure_ColumnName));
			Main.addViewWithBorderToRow(row, Main.createTextView(ctx, R.string.ServerMain_Structure_ColumnType));
			Main.addViewWithBorderToRow(row, Main.createTextView(ctx, R.string.ServerMain_Structure_ColumnNullable));
			Main.addViewWithBorderToRow(row, Main.createTextView(ctx, R.string.ServerMain_Structure_ColumnDefault));
			Main.addViewWithBorderToRow(row, Main.createTextView(ctx, R.string.ServerMain_Structure_ColumnKey));
			Main.addViewWithBorderToRow(row, Main.createTextView(ctx, R.string.ServerMain_Structure_ColumnExtra));
			tableLayout.addView(row);
			for(Column column : getValues()){
				row = new TableRow(ctx);
				Main.addViewWithBorderToRow(row, Main.createTextView(ctx, column.getName()));
				Main.addViewWithBorderToRow(row, Main.createTextView(ctx, column.getType()));
				CheckBox box = new CheckBox(ctx);
				box.setChecked(column.isNullable());
				box.setEnabled(false);
				Main.addViewWithBorderToRow(row, box);
				Main.addViewWithBorderToRow(row, Main.createTextView(ctx, column.getDefaultValue()));
				Main.addViewWithBorderToRow(row, Main.createTextView(ctx, column.getKey()));
				Main.addViewWithBorderToRow(row, Main.createTextView(ctx, column.getExtra()));
				tableLayout.addView(row);
			}
		}
		return tableLayout;
	}

	public HorizontalScrollView getScroll(Context ctx){
		if(horizScroll == null){
			horizScroll = new HorizontalScrollView(ctx);
			ScrollView vertScroll = new ScrollView(ctx);
			vertScroll.addView(getTableLayout(ctx));
			horizScroll.addView(vertScroll);
		}
		return horizScroll;
	}

	@Override
	public int getStructureLevel(boolean hasParam){
		return hasParam ? R.string.ServerMain_Structure_Header_Table : R.string.ServerMain_Structure_Header_Table_NoParam;
	}

	@Override
	@Deprecated
	public View getViewOnce(Context ctx){
		return dep_getLayout(ctx);
	}

	@Override
	@Deprecated
	public void onFirstDisplay(final ServerMainActivity activity){
		activity.getConnectionThread().scheduleAsyncQuery("DESCRIBE `" + getParent().getName() + "`.`" + getName() + "`", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				// TODO
			}
		});
	}
}

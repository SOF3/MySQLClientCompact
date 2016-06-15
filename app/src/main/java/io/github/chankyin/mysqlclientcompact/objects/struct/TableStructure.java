package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.serverui.ServerMainActivity;

public class TableStructure extends Structure<Column, SchemaStructure> implements ListEntry{
	public TableStructure(SchemaStructure parent, String name){
		super(parent, name);
	}

	@Override
	public void doQuery(final ServerMainActivity activity){
		activity.getThread().scheduleAsyncQuery("DESCRIBE `" + getParent().getName() + "`.`" + getName() + "`", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				handleResult(activity, result, new ValueConstructor<Column>(){
					@Override
					public Column create(String name){
						return new Column();
					}
				}, "COLUMN_NAME");
			}
		});
	}

	@Override
	public int getStructureLevel(boolean hasParam){
		return hasParam?R.string.ServerMain_Structure_Header_Table:R.string.ServerMain_Structure_Header_Table_NoParam;
	}

	@Override
	@Deprecated
	public View getViewOnce(Context ctx){
		return dep_getLayout(ctx);
	}

	@Override
	@Deprecated
	public void onFirstDisplay(final ServerMainActivity activity){
		activity.getThread().scheduleAsyncQuery("DESCRIBE `" + getParent().getName() + "`.`" + getName() + "`", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				// TODO
			}
		});
	}
}

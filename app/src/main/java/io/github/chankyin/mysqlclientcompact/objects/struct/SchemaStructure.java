package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.view.View;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;

public class SchemaStructure extends Structure<TableStructure, DatabaseStructure> implements ListEntry{
	public SchemaStructure(DatabaseStructure parent, String name){
		super(parent, name);
	}

	@Override
	public void doQuery(final ServerMainActivity activity){
		activity.getConnectionThread().scheduleAsyncQuery("SHOW TABLES IN `" + getName() + "`", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				handleResult(activity, result, new ValueConstructor<TableStructure>(){
					@Override
					public TableStructure create(String name){
						return new TableStructure(SchemaStructure.this, name);
					}
				}, "TABLE_NAME");
			}
		});
	}

	@Override
	public int getStructureLevel(boolean hasParam){
		return hasParam ? R.string.ServerMain_Structure_Header_Schema : R.string.ServerMain_Structure_Header_Schema_NoParam;
	}

	@Override
	@Deprecated
	public View getViewOnce(Context ctx){
		return dep_getLayout(ctx);
	}

	@Override
	@Deprecated
	public void onFirstDisplay(final ServerMainActivity activity){
		activity.getConnectionThread().scheduleAsyncQuery("SHOW TABLES IN `" + getName() + "`", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				dep_present(activity, "TABLE_NAME", result, new ValueConstructor<TableStructure>(){
					@Override
					public TableStructure create(String name){
						return new TableStructure(SchemaStructure.this, name);
					}
				});
			}
		});
	}
}

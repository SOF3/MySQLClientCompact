package io.github.chankyin.mysqlclientcompact.objects.struct;

import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;
import lombok.Getter;

public class DatabaseStructure extends Structure<SchemaStructure, ServerObject>{
	@Getter private boolean queryStarted = false;

	public DatabaseStructure(ServerObject parent){
		super(parent, null);
	}

	@Override
	public void doQuery(final ServerMainActivity activity){
		queryStarted = true;
		activity.getConnectionThread().scheduleAsyncQuery("SHOW SCHEMAS", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
				handleResult(activity, result, new ValueConstructor<SchemaStructure>(){
					@Override
					public SchemaStructure create(String name){
						return new SchemaStructure(DatabaseStructure.this, name);
					}
				}, "SCHEMA_NAME");
			}
		});
	}

	@Override
	public int getStructureLevel(boolean hasParam){
		return hasParam ? R.string.ServerMain_Structure_Header_Database : R.string.ServerMain_Structure_Header_Database_NoParam;
	}

	@Deprecated
	public void onFirstDisplay(final ServerMainActivity activity){
		activity.getConnectionThread().scheduleAsyncQuery("SHOW SCHEMAS", new ConnectionThread.QueryResultHandler(){
			@Override
			public void handle(ProcessedResult result){
//				Log.d("DatabaseStructure", "Got result of type " + result.getQueryType().name());
//				if(result.getQueryType() == ProcessedResult.Type.ERROR){
//					SQLException e = ((ProcessedErrorResult) result).getException();
//					Toast.makeText(activity,
//							activity.getString(R.string.ServerMain_Structure_QueryFailure, e.getLocalizedMessage()),
//							Toast.LENGTH_LONG).show();
//				}else if(result.getQueryType() == ProcessedResult.Type.QUERY){
//					ProcessedQueryResult queryResult = (ProcessedQueryResult) result;
//					List<SchemaStructure> list = new ArrayList<>();
//					for(Row row : queryResult.getValues()){
//						Cell.StringCell cell = (Cell.StringCell) row.findCell("SCHEMA_NAME");
//						Log.d("DatabaseStructure", cell.getClass().getCanonicalName());
//						SchemaStructure schema = new SchemaStructure(DatabaseStructure.this, cell.getValue());
//						list.add(schema);
//						schema.onFirstDisplay(activity);
//					}
//					setContents(list);
//				}else{
//					throw new AssertionError("Unknown result type: " + result.getQueryType().name());
//				}
				dep_present(activity, "SCHEMA_NAME", result, new ValueConstructor<SchemaStructure>(){
					@Override
					public SchemaStructure create(String name){
						return new SchemaStructure(DatabaseStructure.this, name);
					}
				});
			}
		});
	}
}

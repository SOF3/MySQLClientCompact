package io.github.chankyin.mysqlclientcompact.mysql;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedErrorResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedQueryResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedUpdateResult;
import io.github.chankyin.mysqlclientcompact.objects.LocalTableRef;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.serverui.ServerMainActivity;
import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;

import static io.github.chankyin.mysqlclientcompact.mysql.CriticalError.CLOSE_ERROR;
import static io.github.chankyin.mysqlclientcompact.mysql.CriticalError.ESTABLISH_ERROR;
import static io.github.chankyin.mysqlclientcompact.mysql.CriticalError.UNKNOWN_ACCESS_ERROR;
import static io.github.chankyin.mysqlclientcompact.mysql.CriticalError.UNKNOWN_HOST;

public class ConnectionThread extends Thread{
	@Getter private final ServerObject serverObject;

	@Getter private final Application application;
	@Getter private final ServerMainActivity activity;
	@Getter private final Handler handler;

	@Getter private final LinkedList<Task> tasks = new LinkedList<>();

	@Getter private boolean connected = false, stopping = false;

	@Getter private Connection connection;

	private Map<LocalTableRef, String[]> primaryKeysCache = new HashMap<>();

	public ConnectionThread(ServerObject serverObject, ServerMainActivity activity){
		this.serverObject = serverObject;
		application = activity.getApplication();
		this.activity = activity;
		handler = new Handler(this.activity.getMainLooper());
	}

	@Override
	public void run(){
		InetAddress address;
		try{
			address = InetAddress.getByName(serverObject.getHostname());
		}catch(UnknownHostException e){
			postCriticalError(UNKNOWN_HOST, serverObject.getHostname());
			return;
		}
		try{
			connection = DriverManager.getConnection("jdbc:mysql://" +
					address.getHostAddress() + ":" + serverObject.getPort() +
					"/" + serverObject.getOptions().getDefaultSchema(""), serverObject.getUsername(), serverObject.getPassword());
		}catch(SQLException e){
			postCriticalError(ESTABLISH_ERROR, e.getLocalizedMessage());
			return;
		}

		connected = true;
		postShortToast(R.string.Connection_SuccessfulToast,
				serverObject.getUsername(), serverObject.getHostname(), serverObject.getPort());

		while(!stopping){
			while(!tasks.isEmpty()){
				Task task;
				synchronized(tasks){
					task = tasks.remove(0);
				}
				task.run(this);
			}
		}

		try{
			connection.close();
		}catch(SQLException e){
			postCriticalError(CLOSE_ERROR);
			return;
		}

		connected = false;
		postShortToast(R.string.Connection_Disconencted, serverObject.getServerName());
		handler.post(new Runnable(){
			@Override
			public void run(){
				activity.finish();
			}
		});
	}


	public void disconnect(){
		stopping = true;
	}

	public String[] getPrimaryKeys(String schema, String table) throws SQLException{
		return getPrimaryKeys(schema, table, false);
	}

	public String[] getPrimaryKeys(String schema, String table, boolean fetchAgain) throws SQLException{
		return getPrimaryKeys(LocalTableRef.builder().schema(schema).table(table).build(), fetchAgain);
	}

	public String[] getPrimaryKeys(LocalTableRef ref, boolean noCache) throws SQLException{
		if("information_schema".equals(ref.getSchema())){
			return new String[0];
		}
		if(noCache || !primaryKeysCache.containsKey(ref)){
			return updatePrimaryKeysCache(ref);
		}
		return primaryKeysCache.get(ref);
	}

	private String[] updatePrimaryKeysCache(LocalTableRef ref) throws SQLException{
		assertThisThread();
		Log.d("MySQL query", "Updating primary keys for " + ref);
		ResultSet keys = connection.getMetaData().getPrimaryKeys(ref.getSchema(), ref.getSchema(), ref.getTable());
		List<String> list = new ArrayList<>();
		while(keys.next()){
			list.add(keys.getString("COLUMN_NAME"));
		}

		String[] array = new String[list.size()];
		list.toArray(array);
		primaryKeysCache.put(ref, array);
		return array;
	}

	public ProcessedResult exec(String query) throws SQLException{
		assertThisThread();
		Log.d("MySQL query", "Executing query: " + query);
		Statement stmt;
		int spacePos = query.indexOf(' ');
		if(spacePos == -1){
			spacePos = query.length();
		}
		int crPos = query.indexOf('\r');
		if(crPos == -1){
			crPos = query.length();
		}
		int lfPos = query.indexOf('\n');
		if(lfPos == -1){
			lfPos = query.length();
		}
		int end = Math.min(spacePos, Math.min(crPos, lfPos));
		String command = query.substring(0, end).toLowerCase(Locale.ENGLISH);

//		boolean isQuery = false;
//		switch(command){
//			case "select":
//			case "show":
//			case "describe":
//			case "desc":
//			case "explain":
//				isQuery = true;
//				break;
//		}

		stmt = getConnection().createStatement();
		ProcessedResult result;
		try{
			if(stmt.execute(query)){
				result = new ProcessedQueryResult(this, query, command, stmt.getResultSet());
			}else{
				result = new ProcessedUpdateResult(query, command, stmt.getUpdateCount());
			}
		}catch(SQLException e){
			Log.d("MySQL query", "SQLException for query: " + query, e);
			result = new ProcessedErrorResult(query, command, e);
		}

		return result;
	}


	public void postShortToast(@StringRes int stringId, Object... args){
		postToast(Toast.LENGTH_SHORT, stringId, args);
	}

	public void postToast(@StringRes int stringId, Object... args){
		postToast(Toast.LENGTH_LONG, stringId, args);
	}

	public void postToast(final int length, @StringRes final int stringId, final Object... args){
		assertThisThread();
		handler.post(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(activity, activity.getString(stringId, args), length).show();
			}
		});
	}

	public void postShortToast(final String string){
		postToast(Toast.LENGTH_SHORT, string);
	}

	public void postToast(final String string){
		postToast(Toast.LENGTH_LONG, string);
	}

	public void postToast(final int length, final String string){
		assertThisThread();
		handler.post(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(activity, string, length).show();
			}
		});
	}

	public void postCriticalError(final CriticalError error, final Object... args){
		assertThisThread();
		handler.post(new Runnable(){
			@Override
			public void run(){
				Toast.makeText(activity,
						activity.getString(R.string.Connection_CriticalError,
								activity.getString(error.getMessageId(), args)),
						Toast.LENGTH_LONG).show();
			}
		});
		disconnect();
	}

	public void runOnConnectionThread(Task task){
		synchronized(tasks){
			tasks.add(task);
		}
	}

	public void scheduleAsyncQuery(final String query, final QueryResultHandler resultHandler){
		assertUiThread();
		runOnConnectionThread(new Task(){
			@Override
			public void run(ConnectionThread thread){
				final ProcessedResult result;
				try{
					result = exec(query);
				}catch(SQLException e){
					postCriticalError(UNKNOWN_ACCESS_ERROR, e.getLocalizedMessage());
					return;
				}
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						resultHandler.handle(result); // no lambdas >_<
					}
				});
			}
		});
	}


	public boolean isThisThread(){
		return Thread.currentThread() == this;
	}

	public void assertThisThread() throws IllegalThreadStateException{
		if(!isThisThread()){
			throw new IllegalThreadStateException("This method can only be called from this ConnectionThread");
		}
	}

	public void assertUiThread() throws IllegalThreadStateException{
		if(Looper.myLooper() != Looper.getMainLooper()){
			throw new IllegalThreadStateException("This method can only be called from the UI thread");
		}
	}

	public static interface Task{
		public void run(ConnectionThread thread);
	}

	public static interface QueryResultHandler{
		public void handle(ProcessedResult result);
	}
}

package io.github.chankyin.mysqlclientcompact.serverui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.CriticalError;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;

import java.sql.SQLException;

public class QueryFragment extends Fragment{
	private ServerMainActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		activity = (ServerMainActivity) getActivity();
		ServerObject server = activity.getServer();
		View layout = inflater.inflate(R.layout.server_main_fragment_query, container, false);
		layout.findViewById(R.id.ServerMain_Query_QueryButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				String query = ((EditText) getActivity().findViewById(R.id.ServerMain_Query_QueryBox)).getText().toString();

				activity.getThread().runOnConnectionThread(new ExecuteQueryTask(query));
			}
		});
		return layout;
	}

	private class ExecuteQueryTask implements ConnectionThread.Task{
		private final String query;

		public ExecuteQueryTask(String query){
			this.query = query.trim();
		}

		@Override
		public void run(ConnectionThread thread){
			try{
				final ProcessedResult result = thread.exec(query);
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						activity.handleQueryResult(result);
					}
				});
			}catch(SQLException e){
				thread.postCriticalError(CriticalError.UNKNOWN_ACCESS_ERROR, e.getLocalizedMessage());
			}
		}
	}
}

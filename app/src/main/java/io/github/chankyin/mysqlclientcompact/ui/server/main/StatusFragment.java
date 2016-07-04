package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedQueryResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.objects.result.Row;

import java.util.Locale;

public class StatusFragment extends MFragment implements ConnectionThread.QueryResultHandler{
	private LinearLayout layout;
	private ServerMainActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		activity = (ServerMainActivity) getActivity();
		View wrapper = inflater.inflate(R.layout.server_main_fragment_status, container, false);
		layout = (LinearLayout) wrapper.findViewById(R.id.ServerMain_Status_Table);

		refresh();

		return layout;
	}

	public void refresh(){
		ServerObject server = activity.getServer();
		activity.getConnectionThread().scheduleAsyncQuery("SHOW GLOBAL STATUS", this);
		activity.getConnectionThread().scheduleAsyncQuery(
				"SELECT DATABASE() as db, CONNECTION_ID() as connId, VERSION() AS version",
				new ConnectionThread.QueryResultHandler(){
					@Override
					public void handle(ProcessedResult result){
						if(result.getQueryType() == ProcessedResult.Type.QUERY){
							Row row = ((ProcessedQueryResult) result).getValues()[0];
							setText(R.id.ServerMain_Status_CurrentDatabase, (CharSequence) row.getCellValue("db"));
							setText(R.id.ServerMain_Status_ConnectionId, (Integer) row.getCellValue("connId"));
							setText(R.id.ServerMain_Status_ServerVersion, (String) row.getCellValue("version"));
						}
					}
				});

		setText(R.id.ServerMain_Status_ServerAddress, getResources().getString(R.string.ServerMain_Status_ServerAddressValue,
				server.getHostname(), server.getPort(), server.getUsername()));
	}

	@Override
	public void handle(ProcessedResult result){
		if(result.getQueryType() == ProcessedResult.Type.QUERY){
			int questions = -1, uptimeSecs = -1;
			for(Row row : ((ProcessedQueryResult) result).getValues()){
				String value = (String) row.getCellValue("VARIABLE_VALUE");
				switch((String) row.getCellValue("VARIABLE_NAME")){
					case "Queries":
						setText(R.id.ServerMain_Status_Queries, value);
						break;
					case "Questions":
						setText(R.id.ServerMain_Status_Questions, value);
						questions = Integer.parseInt(value);
						break;
					case "Slow_queries":
						setText(R.id.ServerMain_Status_SlowQueries, value);
						break;
					case "Uptime":
						uptimeSecs = Integer.parseInt(value);
						setText(R.id.ServerMain_Status_Uptime, Main.formatTimeInterval(uptimeSecs));
						break;
				}
			}
			if(questions != -1 && uptimeSecs != -1){
				setText(R.id.ServerMain_Status_QPS, Math.round(questions * 1000d / uptimeSecs) / 1000d);
			}
		}
	}

	private void setText(@IdRes int resId, CharSequence text){
		((TextView) layout.findViewById(resId)).setText(text);
	}

	private void setText(@IdRes int resId, Integer num){
		((TextView) layout.findViewById(resId)).setText(String.format(Locale.ENGLISH, "%d", num));
	}

	private void setText(@IdRes int resId, Double num){
		((TextView) layout.findViewById(resId)).setText(String.format(Locale.ENGLISH, "%s", num));
	}
}

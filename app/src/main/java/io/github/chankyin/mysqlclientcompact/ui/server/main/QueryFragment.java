package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class QueryFragment extends MFragment{
	private ServerMainActivity activity;

	private List<ProcessedResult> history = new LinkedList<>();
	private MyArrayAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		activity = (ServerMainActivity) getActivity();
		ServerObject server = activity.getServer();
		View layout = inflater.inflate(R.layout.server_main_fragment_query, container, false);
		final EditText editText = (EditText) layout.findViewById(R.id.ServerMain_Query_QueryBox);
		assert editText != null;
		layout.findViewById(R.id.ServerMain_Query_QueryButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				String query = editText.getText().toString();

				if(query.isEmpty()){
					Toast.makeText(activity, R.string.ServerMain_Query_Execute_Empty, Toast.LENGTH_LONG).show();
					return;
				}

				activity.getConnectionThread().scheduleAsyncQuery(query, new MyQueryResultHandler());
			}
		});
		Button clearButton = (Button) layout.findViewById(R.id.ServerMain_Query_ClearQuery);
		clearButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				editText.getText().clear();
			}
		});
		ListView list = (ListView) layout.findViewById(R.id.ServerMain_Query_HistoryList);
		adapter = new MyArrayAdapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(adapter);
		return layout;
	}

	private class MyQueryResultHandler implements ConnectionThread.QueryResultHandler{
		@Override
		public void handle(ProcessedResult result){
			history.add(0, result);
			adapter.notifyDataSetChanged();

			PresentResultActivity.start(activity, result);
		}
	}

	private class MyArrayAdapter extends ArrayAdapter<ProcessedResult> implements AdapterView.OnItemClickListener{
		public MyArrayAdapter(){
			super(activity, android.R.layout.simple_list_item_1, history);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			ProcessedResult result = getItem(position);
			LinearLayout layout = new LinearLayout(activity);

			TextView query = Main.createTextView(activity, result.getQuery());
			query.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			query.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
			layout.addView(query);

			TextView date = Main.createTextView(activity, Main.WC_WC,
					DateFormat.getTimeInstance().format(new Date(result.getQueryTimestamp())));
			date.setTextSize(getResources().getDimension(R.dimen.query_history_time_font));
			if(result.getQueryType() == ProcessedResult.Type.ERROR){
				date.setTextColor(Main.getColorInt(R.color.error));
			}
			layout.addView(date);

			return layout;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			PresentResultActivity.start(activity, getItem(position));
		}
	}
}

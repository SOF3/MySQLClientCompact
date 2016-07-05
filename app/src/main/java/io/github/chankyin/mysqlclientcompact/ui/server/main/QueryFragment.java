package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import io.github.chankyin.mysqlclientcompact.ui.query.QueryEditorActivity;
import io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static io.github.chankyin.mysqlclientcompact.ui.query.QueryEditorActivity.RESULT_KEY_QUERY;

public class QueryFragment extends MFragment{
	private static final int REQUEST_CODE_EDIT_QUERY = QueryEditorActivity.class.getCanonicalName().hashCode() & 0xFFFF;
	private ServerMainActivity activity;

	private EditText queryBox;
	private List<ProcessedResult> history = new LinkedList<>();
	private MyArrayAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		activity = (ServerMainActivity) getActivity();
		ServerObject server = activity.getServer();
		View layout = inflater.inflate(R.layout.server_main_fragment_query, container, false);
		queryBox = (EditText) layout.findViewById(R.id.ServerMain_Query_QueryBox);
		assert queryBox != null;

		layout.findViewById(R.id.ServerMain_Query_QueryButton).setOnClickListener(new ExecuteQueryButtonListener(queryBox));

		Button copyButton = (Button) layout.findViewById(R.id.ServerMain_Query_CopyQuery);
		copyButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Query", queryBox.getText().toString());
				clipboard.setPrimaryClip(clip);
			}
		});

		Button clearButton = (Button) layout.findViewById(R.id.ServerMain_Query_ClearQuery);
		clearButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				queryBox.getText().clear();
			}
		});

		Button editorButton = (Button) layout.findViewById(R.id.ServerMain_Query_AdvancedEditor);
		editorButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				startActivityForResult(new Intent(activity, QueryEditorActivity.class), REQUEST_CODE_EDIT_QUERY);
			}
		});

		ListView list = (ListView) layout.findViewById(R.id.ServerMain_Query_HistoryList);
		adapter = new MyArrayAdapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(adapter);

		return layout;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_CODE_EDIT_QUERY && resultCode == Activity.RESULT_OK){
			queryBox.setText(data.getStringExtra(RESULT_KEY_QUERY));
		}
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

	private class ExecuteQueryButtonListener implements View.OnClickListener{
		private final EditText editText;

		public ExecuteQueryButtonListener(EditText editText){
			this.editText = editText;
		}

		@Override
		public void onClick(View v){
			String query = editText.getText().toString();

			if(query.isEmpty()){
				Toast.makeText(activity, R.string.ServerMain_Query_Execute_Empty, Toast.LENGTH_LONG).show();
				return;
			}

			activity.getConnectionThread().scheduleAsyncQuery(query, new MyQueryResultHandler());
		}
	}
}

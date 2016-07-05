package io.github.chankyin.mysqlclientcompact.ui.query;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import io.github.chankyin.mysqlclientcompact.R;

public class QueryEditorActivity extends AppCompatActivity{
	public static final String RESULT_KEY_QUERY = "io.github.chankyin.mysqlclientcompact.ui.query.QueryEditorActivity.RESULT_KEY_QUERY";



	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_query_editor);

		Spinner spinner = (Spinner) findViewById(R.id.QueryEditor_StatementType);
		assert spinner != null;
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.QueryEditor_StatementTypes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

	}
}

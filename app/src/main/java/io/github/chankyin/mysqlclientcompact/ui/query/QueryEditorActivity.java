package io.github.chankyin.mysqlclientcompact.ui.query;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.objects.query.StructuredQuery;
import lombok.Getter;

public class QueryEditorActivity extends AppCompatActivity{
	public static final String RESULT_KEY_QUERY = "io.github.chankyin.mysqlclientcompact.ui.query.QueryEditorActivity.RESULT_KEY_QUERY";

	@Getter private StructuredQuery query;

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
		spinner.setOnItemClickListener(new StatementTypeChooseListener());
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		if(query.isValid()){
			setResult(RESULT_OK, new Intent().putExtra(RESULT_KEY_QUERY, query.toString()));
		}
	}

	private class StatementTypeChooseListener implements AdapterView.OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
			switch(i){
				case 0: // SELECT
					changeToSelect();
					break;
				case 1: // INSERT
					changeToInsert();
					break;
				case 2: // UPDATE
					changeToUpdate();
					break;
				case 3: // DELETE
					changeToDelete();
					break;
				case 4: // REPLACE
					changeToReplace();
					break;
			}
		}
	}

	private void changeToSelect(){
		// TODO
	}

	private void changeToInsert(){
		// TODO
	}

	private void changeToUpdate(){
		// TODO
	}

	private void changeToDelete(){
		// TODO
	}

	private void changeToReplace(){
		// TODO
	}
}

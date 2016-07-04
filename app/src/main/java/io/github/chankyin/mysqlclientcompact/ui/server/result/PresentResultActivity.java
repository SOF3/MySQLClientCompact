package io.github.chankyin.mysqlclientcompact.ui.server.result;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;

import java.io.Serializable;

public class PresentResultActivity extends AppCompatActivity{
	public final static String INTENT_EXTRA_RESULT = "io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity.RESULT_OBJECT";

	private ProcessedResult result;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Serializable extra = getIntent().getSerializableExtra(INTENT_EXTRA_RESULT);
		if(!(extra instanceof ProcessedResult)){
			throw new IllegalArgumentException("Intent extra RESULT_OBJECT must be serialized ProcessedResult");
		}
		result = (ProcessedResult) extra;

		setContentView(R.layout.activity_present_result);

		TextView queryView = (TextView) findViewById(R.id.PresentResult_Query);
		assert queryView != null;
		queryView.setText(result.getQuery());

		Button button = (Button) findViewById(R.id.PresentResult_Query_Copy);
		assert button != null;
		button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Query", result.getQuery());
				clipboard.setPrimaryClip(clip);
			}
		});

		long start = System.nanoTime();
		result.present(this);
		long end = System.nanoTime();
	}

	public static void start(Context ctx, ProcessedResult result){
		ctx.startActivity(new Intent(ctx, PresentResultActivity.class).putExtra(INTENT_EXTRA_RESULT, result));
	}
}

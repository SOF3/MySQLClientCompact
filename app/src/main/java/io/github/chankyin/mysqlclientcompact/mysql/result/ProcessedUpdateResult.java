package io.github.chankyin.mysqlclientcompact.mysql.result;

import android.widget.LinearLayout;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity;
import lombok.Getter;

@Getter
public class ProcessedUpdateResult implements ProcessedResult{
	private final String query;
	private final String command;
	private final int rowsCount;
	@Getter private final long queryTimestamp;

	public ProcessedUpdateResult(String query, String command, int rowsCount){
		this.query = query;
		this.command = command;
		this.rowsCount = rowsCount;
		queryTimestamp = System.currentTimeMillis();
	}

	@Override
	public Type getQueryType(){
		return Type.UPDATE;
	}

	@Override
	public void present(PresentResultActivity atv){
		LinearLayout body = (LinearLayout) atv.findViewById(R.id.PresentResult_Body);
		assert body != null;

		TextView status = Main.createTextView(atv, R.string.PresentResult_Success);
		status.setTextColor(Main.getColorInt(R.color.success));
		status.setTextSize(atv.getResources().getDimension(R.dimen.query_result_status_font));
		body.addView(status);

		if(rowsCount != -1){
			body.addView(Main.createTextView(atv, R.string.PresentResult_UpdateCount, rowsCount));
		}
	}
}

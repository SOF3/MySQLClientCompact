package io.github.chankyin.mysqlclientcompact.mysql.result;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity;
import lombok.Getter;

import java.sql.SQLException;

@Getter
public class ProcessedErrorResult implements ProcessedResult{
	private final String query;
	private final String command;
	private final SQLException exception;
	@Getter private final long queryTimestamp;

	public ProcessedErrorResult(String query, String command, SQLException exception){
		this.query = query;
		this.command = command;
		this.exception = exception;
		queryTimestamp = System.currentTimeMillis();
	}

	@Override
	public Type getQueryType(){
		return Type.ERROR;
	}

	@Override
	public void present(final PresentResultActivity atv){
		TextView queryView = (TextView) atv.findViewById(R.id.PresentResult_Query);
		assert queryView != null;
		queryView.setText(processedQuery(atv));

		LinearLayout body = (LinearLayout) atv.findViewById(R.id.PresentResult_Body);
		assert body != null;

		TextView status = Main.createTextView(atv, R.string.PresentResult_Error);
		status.setTextColor(Main.getColorInt(R.color.error));
		status.setTextSize(atv.getResources().getDimension(R.dimen.query_result_status_font));
		body.addView(status);

		body.addView(Main.createTextView(atv, exception.getLocalizedMessage()));
	}

	private CharSequence processedQuery(Context ctx){
		Log.d("PresentError", "OK:a");
		if(exception instanceof MySQLSyntaxErrorException){
			Log.d("PresentError", "OK:b");
			String start = "You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near '";
			String message = exception.getMessage();
			if(message.startsWith(start)){
				Log.d("PresentError", "OK:c");
				message = message.substring(start.length());
				String atLine = "' at line ";
				int lastIndex = message.lastIndexOf(atLine);
				if(lastIndex != -1){
					Log.d("PresentError", "OK:d");
					String highlight = message.substring(0, lastIndex);
					String lineNumberString = message.substring(lastIndex + atLine.length());
					try{
						Log.d("PresentError", "OK:e");
						int lineNumber = Integer.parseInt(lineNumberString) - 1;
						String[] lines = query.split("\n");
						if(0 <= lineNumber && lineNumber < lines.length){
							Log.d("PresentError", "OK:f");
							String line = lines[lineNumber];
							int index = line.indexOf(highlight);
							if(index != -1){
								Log.d("PresentError", "OK:g");
								String front = "";
								for(int i = 0; i < lineNumber; i++){
									front += lines[i] + "\n";
								}
								front += line.substring(0, index);
								String back = line.substring(index + highlight.length());
								for(int i = lineNumber + 1; i < lines.length; i++){
									back += "\n" + lines[i];
								}

								front = Html.escapeHtml(front);
								highlight = Html.escapeHtml(highlight);
								back = Html.escapeHtml(back);

								highlight = "<font color=\"#FF0008\">" + highlight + "</font>";

								Log.d("PresentResult", front + highlight + back);
								return Html.fromHtml(front + highlight + back);
							}
						}
					}catch(NumberFormatException e){
					}
				}
			}
		}

		Log.d("PresentResult", query);
		return query;
	}
}

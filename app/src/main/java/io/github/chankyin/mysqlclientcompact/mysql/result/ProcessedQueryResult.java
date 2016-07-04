package io.github.chankyin.mysqlclientcompact.mysql.result;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.mysql.jdbc.ResultSetMetaData;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.objects.result.Cell;
import io.github.chankyin.mysqlclientcompact.objects.result.Row;
import io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class ProcessedQueryResult implements ProcessedResult{
	private final String query;
	private final String command;
	private final Row header;
	private final Row[] values;
	@Getter private final long queryTimestamp;

	public ProcessedQueryResult(ConnectionThread thread, String query, String command, ResultSet set) throws SQLException{
		this.query = query;
		this.command = command;
		queryTimestamp = System.currentTimeMillis();

		ResultSetMetaData data = (ResultSetMetaData) set.getMetaData();
		header = new Row(Row.HEADER_ROW_ID, new Cell[data.getColumnCount()]);

		for(int columnId = 1; columnId <= header.getContents().length; columnId++){
			Log.d("MySQL query", String.format(
					"Catalog: %s, Schema: %s, Table: %s",
					data.getCatalogName(columnId), data.getSchemaName(columnId), data.getTableName(columnId)
			));
			String[] keys = thread.getPrimaryKeys(data.getCatalogName(columnId), data.getTableName(columnId));
			String columnName = data.getColumnName(columnId);
			int columnType = data.getColumnType(columnId);
			Log.d("MySQL query", "Column: " + columnName + " (Type: " + columnType + ")");
			boolean isPrimaryKey = false;
			for(String key : keys){
				if(key.equals(columnName)){
					isPrimaryKey = true;
					break;
				}
			}
			header.getContents()[columnId - 1]
					= Cell.create(columnId, columnName, columnType, data.isSigned(columnId), isPrimaryKey);
		}

		List<Row> rows = new ArrayList<>();
		int rowId = 0;

		while(set.next()){
			Row row = header.clone(rowId++);
			for(Cell<?> cell : row.getContents()){
				cell.fetch(set);
			}
			rows.add(row);
		}
		values = new Row[rows.size()];
		rows.toArray(values);
	}

	@Override
	public Type getQueryType(){
		return Type.QUERY;
	}

	@Override
	public void present(final PresentResultActivity atv){
		final int initStart = 1;
		final int initEnd = Math.min(values.length, 50);

		LinearLayout body = (LinearLayout) atv.findViewById(R.id.PresentResult_Body);
		assert body != null;

		TextView status = Main.createTextView(atv, R.string.PresentResult_Success);
		status.setTextColor(Main.getColorInt(R.color.success));
		status.setTextSize(atv.getResources().getDimension(R.dimen.query_result_status_font));
		body.addView(status);

		if(values.length == 0){
			body.addView(Main.createTextView(atv, R.string.PresentResult_Query_EmptyResultSet));
			return;
		}

		LinearLayout inVertScroll = new LinearLayout(atv);
		inVertScroll.setOrientation(LinearLayout.VERTICAL);
		LinearLayout boundsBar = new LinearLayout(atv);

		boundsBar.addView(Main.createTextView(atv, R.string.PresentResult_Query_From));
		final EditText from = new EditText(atv);
		from.setInputType(InputType.TYPE_CLASS_NUMBER);
		from.setText(String.format(Locale.ENGLISH, "%d", initStart));
		boundsBar.addView(from);
		from.setLayoutParams(Main.WEIGHT_MP);

		boundsBar.addView(Main.createTextView(atv, R.string.PresentResult_Query_To));
		final EditText to = new EditText(atv);
		to.setInputType(InputType.TYPE_CLASS_NUMBER);
		to.setText(String.format(Locale.ENGLISH, "%d", initEnd));
		boundsBar.addView(to);
		to.setLayoutParams(Main.WEIGHT_MP);

		boundsBar.addView(Main.createTextView(atv, R.string.PresentResult_Query_Count));
		final EditText count = new EditText(atv);
		count.setInputType(InputType.TYPE_CLASS_NUMBER);
		count.setText(String.format(Locale.ENGLISH, "%d", initEnd - initStart + 1));
		boundsBar.addView(count);
		count.setLayoutParams(Main.WEIGHT_MP);

		final Button submit = new Button(atv);
		submit.setText(R.string.PresentResult_Query_SubmitBounds);
		boundsBar.addView(submit);

		final BoundsBalancer balancer = new BoundsBalancer(from, to, count, submit);
		from.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean stillHasFocus){
				if(!stillHasFocus){
					balancer.onFromChanged();
				}
			}
		});
		to.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean stillHasFocus){
				if(!stillHasFocus){
					balancer.onToChanged();
				}
			}
		});
		count.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean stillHasFocus){
				if(!stillHasFocus){
					balancer.onCountChanged();
				}
			}
		});

		inVertScroll.addView(boundsBar);

		inVertScroll.addView(Main.createTextView(atv, R.string.PresentResult_Query_BoundsWarning));

		final TextView sizeView = new TextView(atv);
		sizeView.setText(atv.getString(R.string.PresentResult_Query_Bounds,
				initStart, initEnd, initEnd - initStart + 1, values.length));
		inVertScroll.addView(sizeView);

		inVertScroll.addView(Main.createTextView(atv, R.string.PresentResult_Query_TableHint));

		final HorizontalScrollView horizScroll = new HorizontalScrollView(atv);
		horizScroll.addView(getTableLayout(atv, initStart, initEnd));
		inVertScroll.addView(horizScroll);

		ScrollView vertScroll = new ScrollView(atv);
		vertScroll.addView(inVertScroll);

		submit.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				int start = Integer.parseInt(from.getText().toString());
				int end = Integer.parseInt(to.getText().toString());

				View currentFocus = atv.getCurrentFocus();
				if(currentFocus == from || currentFocus == to || currentFocus == count){
					currentFocus.clearFocus();
					if(!submit.isEnabled()){
						return;
					}
				}

				int count = end - start + 1;

				horizScroll.removeAllViews();
				horizScroll.addView(getTableLayout(atv, start, end));

				sizeView.setText(atv.getString(R.string.PresentResult_Query_Bounds,
						start, end, end - start + 1, values.length));
			}
		});

		body.addView(vertScroll);
	}

	private class BoundsBalancer{
		private final EditText from;
		private final EditText to;
		private final EditText count;
		private final Button submit;
		private boolean lastChangedTo = false;

		public BoundsBalancer(EditText from, EditText to, EditText count, Button submit){
			this.from = from;
			this.to = to;
			this.count = count;
			this.submit = submit;
		}

		public void onFromChanged(){
			lastChangedTo = false;
			updateCount();
		}

		public void onToChanged(){
			lastChangedTo = true;
			updateCount();
		}

		public void onCountChanged(){
			Editable otherText = lastChangedTo ? to.getText() : from.getText(), countText = count.getText();
			if(otherText.length() == 0 || countText.length() == 0){
				submit.setEnabled(false);
				return;
			}
			int other, count;
			try{
				other = Integer.parseInt(otherText.toString());
				count = Integer.parseInt(countText.toString());
			}catch(NumberFormatException e){
				submit.setEnabled(false);
				return;
			}
			if(count <= 0){
				submit.setEnabled(false);
				return;
			}
			int dep = lastChangedTo ? other - count + 1 : other + count - 1;
			if(lastChangedTo && dep < 0 || !lastChangedTo && dep > values.length){
				submit.setEnabled(false);
				return;
			}

			EditText edit = lastChangedTo ? from : to;
			edit.setText(String.format(Locale.ENGLISH, "%d", dep));
			submit.setEnabled(true);
		}

		private void updateCount(){
			Editable fromText = from.getText(), toText = to.getText();
			if(fromText.length() == 0 || toText.length() == 0){
				submit.setEnabled(false);
				return;
			}
			int start, end;
			try{
				start = Integer.parseInt(fromText.toString());
				end = Integer.parseInt(toText.toString());
			}catch(NumberFormatException e){
				submit.setEnabled(false);
				return;
			}
			if(start < 1 || end > values.length || end < start){
				submit.setEnabled(false);
				return;
			}

			count.setText(String.format(Locale.ENGLISH, "%d", end - start + 1));
			submit.setEnabled(true);
		}
	}

	public TableLayout getTableLayout(PresentResultActivity atv, int start, int end){
		if(start < 1 || end > values.length || end < start){
			throw new IndexOutOfBoundsException("start=" + start + ",end=" + end);
		}

		TableLayout table = new TableLayout(atv);
		table.addView(createHeaderRow(atv));

		for(int i = start - 1; i <= end - 1 && i < values.length; i++){
			Row row = values[i];
			TableRow rowView = new TableRow(atv);
			for(Cell<?> cell : row.getContents()){
				View viewer = cell.toViewer(atv);
				viewer.setOnClickListener(new CopyOnClickListener(cell.getCopyData(atv)));
				Main.addViewWithBorderToRow(rowView, viewer);
			}
			table.addView(rowView);
		}

		return table;
	}

	private TableRow createHeaderRow(Context ctx){
		TableRow row = new TableRow(ctx);

		for(Cell<?> cell : header.getContents()){
			TextView view = Main.createTextView(ctx, cell.getName());
			view.setOnClickListener(new CopyOnClickListener("Column", cell.getName()));
			Main.addViewWithBorderToRow(row, view);
		}

		return row;
	}

	private class CopyOnClickListener implements View.OnClickListener{
		private ClipData clip;

		public CopyOnClickListener(String label, String string){
			this(ClipData.newPlainText(label, string));
		}

		public CopyOnClickListener(ClipData clip){
			this.clip = clip;
		}

		@Override
		public void onClick(View v){
			ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setPrimaryClip(clip);
		}
	}
}

package io.github.chankyin.mysqlclientcompact.objects.query;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.objects.LocalTableRef;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Getter
public class SelectQuery extends StructuredQuery{
	private boolean distinct = false;
	private List<SubQuery> columns = new ArrayList<>();
	private LocalTableRef table = null;
	private Order order = Order.NIL;
	private String orderKey = null;
	private int limit = NIL_LIMIT;
	private List<String> columnOptions = new ArrayList<>(Arrays.asList("Other…", "Loading…"));

	@Override
	protected void populateLayoutImpl(LinearLayout layout){
		Context ctx = layout.getContext();

		CheckBox distinctBox = new CheckBox(ctx);
		distinctBox.setText(R.string.QueryEditor_Select_Distinct);
		distinctBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b){
				distinct = b;
			}
		});
		layout.addView(distinctBox);

		layout.addView(createColumnsLayout(ctx));
	}

	private ViewGroup createColumnsLayout(final Context ctx){
		final LinearLayout output = new LinearLayout(ctx);
		output.setOrientation(LinearLayout.HORIZONTAL);

		Button button = new Button(ctx);
		button.setText(R.string.QueryEditor_Select_AddColumn);
		button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				output.addView(getColumnEntry(ctx));
			}
		});
		output.addView(button);

		return output;
	}

	private View getColumnEntry(Context ctx){
		LinearLayout output = new LinearLayout(ctx);

		Spinner spinner = new Spinner(ctx);
		spinner.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, columnOptions));
		output.addView(spinner);

		return output;
	}

	@Override
	protected String getQueryString(){
		StringBuilder builder = new StringBuilder("SELECT ");

		if(distinct){
			builder.append("DISTINCT ");
		}

		for(SubQuery column : columns){
			builder.append('(')
					.append(column.getQueryString())
					.append("),");
		}
		builder.setCharAt(builder.length() - 1, ' ');

		if(table != null){
			builder.append("FROM ")
					.append(table)
					.append(' ');
		}

		if(order != Order.NIL && orderKey != null){
			builder.append(String.format(Locale.ENGLISH, order.getSyntax(), orderKey))
					.append(' ');
		}

		if(limit != NIL_LIMIT){
			builder.append("LIMIT ")
					.append(limit);
		}

		return builder.toString().trim();
	}

	@Override
	public boolean isValid(){
		return table != null && columns.size() != 0;
	}

	public void setColumnOptions(List<String> options){
		while(columnOptions.size() > 1){
			columnOptions.remove(1);
		}
		columnOptions.addAll(options);
	}
}

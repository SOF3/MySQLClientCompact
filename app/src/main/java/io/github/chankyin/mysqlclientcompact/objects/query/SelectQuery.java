package io.github.chankyin.mysqlclientcompact.objects.query;

import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.objects.LocalTableRef;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class SelectQuery extends StructuredQuery{
	private boolean distinct;
	private List<SubQuery> columns = new ArrayList<>();
	private LocalTableRef table = null;
	private Order order = Order.NIL;
	private String orderKey = null;
	private int limit = NIL_LIMIT;

	@Override
	protected void populateLayoutImpl(LinearLayout layout){
		// TODO
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
}

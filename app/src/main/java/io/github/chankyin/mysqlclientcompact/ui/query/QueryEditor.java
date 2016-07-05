package io.github.chankyin.mysqlclientcompact.ui.query;

import io.github.chankyin.mysqlclientcompact.objects.query.StructuredQuery;

public class QueryEditor{
	private StructuredQuery query;

	@Override
	public String toString(){
		return query.toString();
	}
}

package io.github.chankyin.mysqlclientcompact.objects.query;

public abstract class SubQuery{
	@Override
	public final String toString(){
		return getQueryString();
	}

	protected abstract String getQueryString();
}

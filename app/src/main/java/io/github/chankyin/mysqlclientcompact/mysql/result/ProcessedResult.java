package io.github.chankyin.mysqlclientcompact.mysql.result;

public interface ProcessedResult{
	public String getQuery();

	public String getCommand();

	public Type getQueryType();

	public enum Type{
		UPDATE,
		QUERY,
		ERROR
	}
}

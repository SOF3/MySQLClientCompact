package io.github.chankyin.mysqlclientcompact.mysql.result;

import io.github.chankyin.mysqlclientcompact.ui.server.result.PresentResultActivity;

import java.io.Serializable;

public interface ProcessedResult extends Serializable{
	public Type getQueryType();

	public String getQuery();
	public String getCommand();

	public long getQueryTimestamp();

	public void present(PresentResultActivity atv);

	public enum Type{
		UPDATE,
		QUERY,
		ERROR
	}
}

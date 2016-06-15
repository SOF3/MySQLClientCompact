package io.github.chankyin.mysqlclientcompact.mysql.result;

import lombok.Getter;

@Getter
public class ProcessedUpdateResult implements ProcessedResult{
	private final String query;
	private final String command;
	private final int rowsCount;

	public ProcessedUpdateResult(String query, String command, int rowsCount){
		this.query = query;
		this.command = command;
		this.rowsCount = rowsCount;
	}

	@Override
	public Type getQueryType(){
		return Type.UPDATE;
	}
}

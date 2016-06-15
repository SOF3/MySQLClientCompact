package io.github.chankyin.mysqlclientcompact.mysql.result;

import lombok.Getter;

import java.sql.SQLException;

@Getter
public class ProcessedErrorResult implements ProcessedResult{
	private final String query;
	private final String command;
	private final SQLException exception;

	public ProcessedErrorResult(String query, String command, SQLException exception){
		this.query = query;
		this.command = command;
		this.exception = exception;
	}

	@Override
	public Type getQueryType(){
		return Type.ERROR;
	}
}

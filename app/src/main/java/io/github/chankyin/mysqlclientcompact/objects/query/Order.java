package io.github.chankyin.mysqlclientcompact.objects.query;

import lombok.Getter;

public enum Order{
	NIL(""),
	ASCENDING("ORDER BY %s ASC"),
	DESCENDING("ORDER BY %s DESC");

	@Getter private String syntax;

	Order(String syntax){
		this.syntax = syntax;
	}
}

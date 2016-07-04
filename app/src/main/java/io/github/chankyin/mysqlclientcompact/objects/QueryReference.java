package io.github.chankyin.mysqlclientcompact.objects;

import android.content.Context;
import android.support.annotation.StringRes;
import io.github.chankyin.mysqlclientcompact.R;
import lombok.Getter;
import lombok.Setter;

public class QueryReference implements QueryLogEntry{
	@Getter private String query;
	@Getter @Setter private QueryState state = QueryState.PENDING;

	public QueryReference(String query){
		this.query = query;
	}

	@Override
	public String toString(Context ctx){
		return ctx.getString(R.string.ServerMain_QueryLog_Format, query, ctx.getString(state.getName()));
	}

	public enum QueryState{
		PENDING(R.string.QueryState_Pending),
		EXECUTING(R.string.QueryState_Executing),
		COMPLETED(R.string.QueryState_Completed),
		ERROR(R.string.QueryState_Error);

		@Getter @StringRes private int name;

		QueryState(@StringRes int name){
			this.name = name;
		}
	}
}

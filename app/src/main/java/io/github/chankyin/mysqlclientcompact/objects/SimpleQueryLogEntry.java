package io.github.chankyin.mysqlclientcompact.objects;

import android.content.Context;
import android.support.annotation.StringRes;
import lombok.Getter;

public class SimpleQueryLogEntry implements QueryLogEntry{
	@Getter @StringRes private int resId;
	@Getter private Object[] params;

	public SimpleQueryLogEntry(@StringRes int resId, Object... params){
		this.resId = resId;
		this.params = params;
	}

	@Override
	public String toString(Context context){
		return context.getString(resId, params);
	}
}

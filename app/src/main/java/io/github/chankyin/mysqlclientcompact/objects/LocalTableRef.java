package io.github.chankyin.mysqlclientcompact.objects;

import android.support.annotation.NonNull;
import lombok.Getter;
import lombok.experimental.Builder;

@Builder
@Getter
public class LocalTableRef{
	@NonNull private final String schema;
	@NonNull private final String table;

	@Override
	public boolean equals(Object o){
		return o != null && o instanceof LocalTableRef && deepEquals((LocalTableRef) o);
	}

	@Override
	public int hashCode(){
		int result = 0x10ca1;
		result = result * 0x74b1e + schema.hashCode();
		result = result * 0x74b1e + table.hashCode();
		result = result * 0x74b1e + getClass().getCanonicalName().hashCode();
		return result;
	}

	private boolean deepEquals(LocalTableRef other){
		return other.schema.equals(schema) && other.table.equals(table);
	}

	@Override
	public String toString(){
		return '`' + schema + "`.`" + table + '`';
	}
}

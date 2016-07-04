package io.github.chankyin.mysqlclientcompact.objects.result;

import android.support.annotation.NonNull;
import io.github.chankyin.mysqlclientcompact.Main;
import lombok.Getter;

import java.io.Serializable;
import java.util.Locale;

@Getter
public class Row implements Serializable{
	public final static int HEADER_ROW_ID = -1;

	final int rowId;
	final Cell<?>[] contents;

	public Row(int rowId, Cell[] contents){
		this.rowId = rowId;
		this.contents = contents;
	}

	public Row clone(int rowId){
		if(this.rowId != HEADER_ROW_ID){
			throw new IllegalStateException("Only header Row objects can be cloned");
		}

		return new Row(rowId, deepCloneContents());
	}

	private Cell[] deepCloneContents(){
		Cell[] array = new Cell[contents.length];
		for(int i = 0; i < contents.length; i++){
			try{
				array[i] = (Cell) contents[i].clone();
			}catch(CloneNotSupportedException e){
				e.printStackTrace();
			}
		}
		return array;
	}

	public Cell<?> findCell(@NonNull String column){
		for(Cell<?> cell : contents){
			if(column.equals(cell.getName())){
				return cell;
			}
		}
		return null;
	}

	public Object getCellValue(@NonNull String column){
		for(Cell<?> cell : contents){
			if(column.equals(cell.getName())){
				return cell.getValue();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T castCellValue(@NonNull String column) throws ClassCastException{
		for(Cell<?> cell : contents){
			if(column.equals(cell.getName())){
				return (T) cell.getValue();
			}
		}
		return null;
	}

	@Override
	public String toString(){
		return String.format(Locale.ENGLISH, "{\"rowId\": %d, \"contents\":[%s]}",
				rowId, Main.implode(", ", (Object[]) contents));
	}
}

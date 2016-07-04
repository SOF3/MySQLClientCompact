package io.github.chankyin.mysqlclientcompact.objects.result;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.Main;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BinaryCell extends Cell<byte[]>{
	public BinaryCell(int columnId, String columnName, int columnType, boolean isPrimaryKey){
		super(columnId, columnName, columnType, isPrimaryKey);
	}

	@Override
	public View toViewer(Context ctx){
		LinearLayout layout = new LinearLayout(ctx);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		TextView view = new TextView(ctx);
		view.setText("X'");
		layout.addView(view);
		view = new TextView(ctx);
		view.setTypeface(Typeface.MONOSPACE);
		view.setText(Main.bin2hex(value, " ", value.length));
		layout.addView(view);
		view = Main.createTextView(ctx, "'");
		view.setTypeface(Typeface.MONOSPACE);
		layout.addView(view);
		return layout;
	}

	@Override
	public ClipData getCopyData(Context ctx){
		return simpleClipData(new String(getValue()));
	}

	@Override
	public void fetch(ResultSet set) throws SQLException{
		InputStream is = set.getBinaryStream(getColumnId());
		try{
			if(is == null){
				setValue(new byte[0]);
			}else{
				setValue(IOUtils.toByteArray(is));
			}
		}catch(IOException e){
			e.printStackTrace();
			setValue(new byte[0]);
		}
	}

	public void setValue(byte[] array){
		value = array;
	}
}

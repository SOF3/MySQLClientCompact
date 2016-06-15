package io.github.chankyin.mysqlclientcompact;

import android.app.Application;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@Getter
@Setter
public class MyApplication extends Application{
	public final static ViewGroup.LayoutParams WC_WC = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
	public final static ViewGroup.LayoutParams MP_WC = new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT);

	@Override
	public void onCreate(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		super.onCreate();
	}

	public static String findConstantNameInClass(Class<?> clazz, Object constantValue){
		return findConstantNameInClass(clazz, constantValue, true, null);
	}

	public static String findConstantNameInClass(Class<?> clazz, Object constantValue, String defaultResult){
		return findConstantNameInClass(clazz, constantValue, true, defaultResult);
	}

	public static String findConstantNameInClass(Class<?> clazz, Object constantValue, boolean declared){
		return findConstantNameInClass(clazz, constantValue, declared, null);
	}

	public static String findConstantNameInClass(Class<?> clazz, Object constantValue, boolean declared, String defaultResult){
		Field[] fields = declared ? clazz.getDeclaredFields() : clazz.getFields();
		for(Field field : fields){
			if((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != (Modifier.FINAL | Modifier.STATIC)){
				try{
					if(field.get(null).equals(constantValue)){
						return field.getName();
					}
				}catch(IllegalAccessException e){
				}
			}
		}
		return defaultResult;
	}

	public static String bin2hex(byte[] bin){
		return bin2hex(bin, "", bin.length);
	}

	public static String bin2hex(byte[] bin, String glue){
		return bin2hex(bin, glue, bin.length);
	}

	public static String bin2hex(byte[] bin, int bytesPerLine){
		return bin2hex(bin, "", bytesPerLine);
	}

	public static String bin2hex(byte[] bin, String glue, int bytesPerLine){
		int unit = 2 + glue.length();
		StringBuilder builder = new StringBuilder(bin.length * unit);
		for(int i = 0; i < bin.length; i++){
			builder.append(Integer.toHexString(bin[i] >> 8 & 0x0F).charAt(0))
					.append(Integer.toHexString(bin[i] & 0x0F).charAt(0))
					.append((i + 1) % bytesPerLine == 0 ? "\n" : glue);
		}
		return bin.length % bytesPerLine == 0 ?
				builder.substring(0, builder.length() - 1) :
				builder.substring(0, builder.length() - glue.length());
	}

	public static byte[] hex2bin(char[] chars) throws NumberFormatException{
		if((chars.length & 1) != 0){
			throw new NumberFormatException("Not in duplets");
		}
		byte[] bin = new byte[chars.length];
		for(int i = 0; i < chars.length * 2; i += 2){
			bin[i / 2] = hex2bin(chars[i], chars[i + 1]);
		}
		return bin;
	}

	public static byte hex2bin(char c0, char c1) throws NumberFormatException{
		return Byte.parseByte(new String(new char[]{c0, c1}), 16);
	}

	public static TextView createTextView(Context ctx, @StringRes int res, Object... args){
		TextView tv = new TextView(ctx);
		tv.setText(ctx.getString(res, args));
		return tv;
	}

	public static TextView createTextView(Context ctx, CharSequence text){
		TextView tv = new TextView(ctx);
		tv.setText(text);
		return tv;
	}

	public static boolean isSubclassOf(Class<?> clazz, Object object){
		if(object.getClass().equals(clazz))return true;
		try{
			object.getClass().asSubclass(clazz);
			return true;
		}catch(ClassCastException e){
			return false;
		}
	}
}

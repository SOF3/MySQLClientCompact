package io.github.chankyin.mysqlclientcompact;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.objects.TranslatedPresentable;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@Getter
@Setter
public class Main extends Application{
	public final static ViewGroup.LayoutParams WC_WC = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
	public final static ViewGroup.LayoutParams WC_MP = new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
	public final static ViewGroup.LayoutParams MP_WC = new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
	public final static ViewGroup.LayoutParams MP_MP = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
	public final static ViewGroup.LayoutParams MP_WEIGHT = new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1);
	public final static ViewGroup.LayoutParams WEIGHT_MP = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1);

	@Getter private static Main instance;

	@Override
	public void onCreate(){
		try{
			instance = this;
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
		if(bin.length == 0){
			return "";
		}
		if(bytesPerLine == 0){
			bytesPerLine = bin.length;
		}
		int unit = 2 + glue.length();
		StringBuilder builder = new StringBuilder(bin.length * unit);
		for(int i = 0; i < bin.length; i++){
			builder.append(Integer.toHexString(bin[i] >> 8 & 0x0F).charAt(0))
					.append(Integer.toHexString(bin[i] & 0x0F).charAt(0));
			if((i + 1) % bytesPerLine == 0){
				builder.append("\n");
			}else{
				builder.append(glue);
			}
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
		return createTextView(ctx, null, res, args);
	}

	public static TextView createTextView(Context ctx, @Nullable ViewGroup.LayoutParams params, @StringRes int res, Object... args){
		TextView tv = new TextView(ctx);
		tv.setText(ctx.getString(res, args));
		if(params != null){
			tv.setLayoutParams(params);
		}
		return tv;
	}

	public static TextView createTextView(Context ctx, CharSequence text){
		return createTextView(ctx, null, text);
	}

	public static TextView createTextView(Context ctx, @Nullable ViewGroup.LayoutParams params, CharSequence text){
		TextView tv = new TextView(ctx);
		tv.setText(text);
		if(params != null){
			tv.setLayoutParams(params);
		}
		return tv;
	}

	public static String implode(String glue, Iterable<?> pieces){
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(Object piece : pieces){
			if(first){
				first = false;
			}else{
				builder.append(glue);
			}
			builder.append(piece instanceof TranslatedPresentable ?
					((TranslatedPresentable) piece).toString(instance) : piece.toString());
		}
		return builder.toString();
	}

	public static String implode(String glue, Object... pieces){
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(Object piece : pieces){
			if(first){
				first = false;
			}else{
				builder.append(glue);
			}
			builder.append(piece instanceof TranslatedPresentable ?
					((TranslatedPresentable) piece).toString(instance) : piece.toString());
		}
		return builder.toString();
	}

	public static void addViewWithBorderToRow(TableRow row, View view){
		int padding = view.getContext().getResources().getDimensionPixelOffset(R.dimen.table_value_padding);
		view.setPadding(view.getPaddingLeft() + padding, view.getPaddingTop() + padding,
				view.getPaddingRight() + padding, view.getPaddingBottom() + padding);
//		view.setLayoutParams(WC_MP);
		view.setBackgroundResource(R.drawable.table_cell_shape);
		row.addView(view);
	}

	public static boolean isSubclassOf(Class<?> clazz, Object object){
		if(object.getClass().equals(clazz)){
			return true;
		}
		try{
			object.getClass().asSubclass(clazz);
			return true;
		}catch(ClassCastException e){
			return false;
		}
	}

	@ColorInt
	@SuppressWarnings("deprecation")
	public static int getColorInt(@ColorRes int resId){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			return getColorIntM(resId);
		}else{
			return instance.getResources().getColor(resId);
		}
	}

	@ColorInt
	@TargetApi(Build.VERSION_CODES.M)
	private static int getColorIntM(@ColorRes int resId){
		return instance.getResources().getColor(resId, instance.getTheme());
	}

	public static CharSequence formatTimeInterval(int seconds){
		if(seconds == 0){
			return "0";
		}
		boolean negative = seconds < 0;
		if(negative){
			seconds *= -1;
		}

		int days = seconds / 86400;
		int hours = (seconds %= 86400) / 3600;
		int minutes = (seconds %= 3600) / 60;
		seconds %= 60;

		Resources res = instance.getResources();
		String[] units = res.getStringArray(R.array.Time_Units);
		int[] values = {days, hours, minutes, seconds};
		if(units.length!= values.length){
			throw new AssertionError("units.length != values.length");
		}
		StringBuilder builder = new StringBuilder();
		if(negative){
			builder.append('-');
		}
		final String valueUnitSeparator = res.getString(R.string.Time_ValueUnitSeparator);
		final String unitSeparator = res.getString(R.string.Time_InterUnitSeparator);
		for(int i = 0; i < units.length; i++){
			if(values[i] > 0){
				builder.append(values[i])
						.append(valueUnitSeparator)
						.append(units[i])
						.append(unitSeparator);
			}
		}
		return builder.substring(0, builder.length() - unitSeparator.length());
	}
}

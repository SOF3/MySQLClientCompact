package io.github.chankyin.mysqlclientcompact.view;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.Main;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public class LogTextView<T> extends TextView{
	@Getter @Setter private int maxSize;
	@Getter private final List<T> list;

	public LogTextView(Context context){
		this(context, 50);
	}

	public LogTextView(Context context, int maxSize){
		super(context);
		if(maxSize < 1){
			throw new IndexOutOfBoundsException("Max size must be at least 1");
		}
		this.maxSize = maxSize;
		list = new LinkedList<>();
	}

	public void addLine(T value){
		addLine(value, true);
	}

	public void addLine(T value, boolean updateText){
		synchronized(list){
			if(list.size() == maxSize){
				list.remove(0);
			}
			list.add(value);
		}

		if(updateText){
			updateText();
		}
	}

	public void updateText(){
		updateText(true);
	}

	public void updateText(boolean scrollToBottom){
		setText(Main.implode("\n", list));
		if(scrollToBottom){
			final ViewParent parent = getParent();
			if(parent instanceof ScrollView){
				((ScrollView) parent).post(new Runnable(){
					@Override
					public void run(){
						((ScrollView) parent).fullScroll(View.FOCUS_DOWN);
					}
				});
			}
		}
	}
}

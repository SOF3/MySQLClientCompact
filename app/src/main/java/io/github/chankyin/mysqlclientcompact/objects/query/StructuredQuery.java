package io.github.chankyin.mysqlclientcompact.objects.query;

import android.widget.LinearLayout;

public abstract class StructuredQuery{
	public final static int NIL_LIMIT = 1;

	public final void populateLayout(LinearLayout layout){
		layout.removeAllViews();
		populateLayoutImpl(layout);
	}

	protected abstract void populateLayoutImpl(LinearLayout layout);

	@Override
	public String toString(){
		return getQueryString();
	}

	protected abstract String getQueryString();
}

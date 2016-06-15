package io.github.chankyin.mysqlclientcompact.serverui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.MyApplication;
import io.github.chankyin.mysqlclientcompact.objects.struct.DatabaseStructure;
import io.github.chankyin.mysqlclientcompact.objects.struct.Structure;
import lombok.Getter;

public class StructureFragment extends Fragment{
	@Getter private DatabaseStructure database;
	@Getter private boolean notInitialized = true;

	@Getter private Structure<?, ?> displayedStructure = null;
	@Getter private LinearLayout contentView;
	@Getter private ViewGroup header;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		if(database == null){
			database = new DatabaseStructure(((ServerMainActivity) getActivity()).getServer());
		}

		contentView = new LinearLayout(getActivity());
		contentView.setOrientation(LinearLayout.VERTICAL);

		HorizontalScrollView scroll = new HorizontalScrollView(getActivity());
		header = new LinearLayout(getActivity());
		scroll.setLayoutParams(MyApplication.MP_WC);
		header.setLayoutParams(MyApplication.MP_WC);
		scroll.addView(header);
		contentView.addView(scroll);

		setDisplayedStructure(database);

		return contentView;
	}

	@Override
	public void onResume(){
		super.onResume();
		database.doQuery((ServerMainActivity) getActivity());
	}

	public void setDisplayedStructure(Structure<?, ?> displayedStructure){
		this.displayedStructure = displayedStructure;
		header.removeAllViews();
		for(Button button : displayedStructure.getIndexButtons(this)){
			header.addView(button);
		}

		while (contentView.getChildCount()>1){
			contentView.removeViewAt(1);
		}
		displayedStructure.populateFragment(this);
	}
}

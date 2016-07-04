package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.objects.struct.DatabaseStructure;
import io.github.chankyin.mysqlclientcompact.objects.struct.Structure;
import lombok.Getter;

public class StructureFragment extends MFragment{
	@Getter private DatabaseStructure database;

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
		scroll.setLayoutParams(Main.MP_WC);
		header.setLayoutParams(Main.MP_WC);
		scroll.addView(header);
		contentView.addView(scroll);

		setDisplayedStructure(database);

		return contentView;
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		header.removeAllViews();
	}

	@Override
	public void onSelected(){
		if(database == null){
			return;
		}

		if(!database.isQueryStarted()){
			database.doQuery((ServerMainActivity) getActivity());
		}
	}

	@Override
	public boolean onBackPressed(){
		if(displayedStructure != null && displayedStructure.getParent() instanceof Structure){
			setDisplayedStructure((Structure) displayedStructure.getParent());
			return true;
		}
		return false;
	}

	public void setDatabase(DatabaseStructure database){
		this.database = database;
		setDisplayedStructure(database);
	}

	public void setDisplayedStructure(Structure<?, ?> displayedStructure){
		this.displayedStructure = displayedStructure;
		header.removeAllViews();
		for(Button button : displayedStructure.getIndexButtons(this)){
			header.addView(button);
		}

		while(contentView.getChildCount() > 1){
			contentView.removeViewAt(1);
		}
		displayedStructure.populateFragment(this);
	}
}

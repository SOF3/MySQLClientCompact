package io.github.chankyin.mysqlclientcompact.objects.struct;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedErrorResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedQueryResult;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.result.Row;
import io.github.chankyin.mysqlclientcompact.objects.result.StringCell;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainPage;
import io.github.chankyin.mysqlclientcompact.ui.server.main.StructureFragment;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Structure<ValueType extends ListEntry, ParentType>{
	@Getter private boolean contentLoaded = false;

	@Getter private ParentType parent;

	@Getter private String name;

	@Getter private List<ValueType> values = new ArrayList<>();

	@Getter List<OnChangeListener<ValueType>> listeners = new ArrayList<>();

	@Deprecated
	private LinearLayout dep_layout = null;
	@Deprecated
	private ListView dep_listView = null;

	private ListView listView = null;

	private Button indexButton;

	public Structure(ParentType parent, String name){
		this.parent = parent;
		this.name = name;
	}

	public Button getIndexButton(final StructureFragment fragment){
		if(indexButton == null){
			indexButton = new Button(fragment.getActivity());
			if(name == null){
				indexButton.setText(fragment.getResources().getString(getStructureLevel(false)));
			}else{
				indexButton.setText(fragment.getResources().getString(getStructureLevel(true), getName()));
			}
			indexButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v){
					fragment.setDisplayedStructure(Structure.this);
				}
			});
		}
		return indexButton;
	}

	public List<Button> getIndexButtons(StructureFragment fragment){
		List<Button> buttons;
		if(getParent() instanceof Structure){
			Structure<?, ?> parent = (Structure) getParent();
			buttons = parent.getIndexButtons(fragment);
		}else{
			buttons = new ArrayList<>();
		}
		buttons.add(getIndexButton(fragment));
		return buttons;
	}

	public void populateFragment(final StructureFragment fragment){
		if(isContentLoaded()){
			if(listView == null){
				listView = new ListView(fragment.getActivity());
				ListViewAdapter adapter = new ListViewAdapter(fragment);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(adapter);
			}
			if(listView.getParent() instanceof ViewGroup){
				((ViewGroup) listView.getParent()).removeView(listView);
			}
			fragment.getContentView().addView(listView);
		}else{
			TextView textView = Main.createTextView(fragment.getActivity(),
					R.string.ServerMain_Structure_EntryLoading, getName() == null ? "" : getName());
			textView.setTextSize(fragment.getResources().getDimension(R.dimen.activity_center_word_font));
			fragment.getContentView().addView(textView);
		}
	}

	public int getNestLevels(){
		Object parent = getParent();
		return parent instanceof Structure ? ((Structure) parent).getNestLevels() + 1 : 0;
	}

	public void setName(String name){
		for(OnChangeListener<ValueType> listener : listeners){
			listener.onChangeName(name);
		}
		this.name = name;
	}

	public void setContents(List<ValueType> data){
		contentLoaded = true;
		while(!values.isEmpty()){
			ValueType value = values.remove(0);
			for(OnChangeListener<ValueType> listener : listeners){
				listener.onRemoveContent(value);
			}
		}

		values.addAll(data);
		for(OnChangeListener<ValueType> listener : listeners){
			listener.onSetContents();
			for(ValueType value : data){
				listener.onOneValueAdded(value);
			}
		}
	}

	public void addContent(ValueType data){
		throwInit();
		for(OnChangeListener<ValueType> listener : listeners){
			listener.onAddContent(data);
			listener.onOneValueAdded(data);
		}
		values.add(data);
	}

	public void removeContent(ValueType data){
		throwInit();
		for(OnChangeListener<ValueType> listener : listeners){
			listener.onRemoveContent(data);
		}
		values.remove(data);
	}

	private void throwInit(){
		if(!contentLoaded){
			throw new IllegalStateException("Not initialized");
		}
	}

	@StringRes
	public abstract int getStructureLevel(boolean hasParam);

	protected void handleResult(ServerMainActivity activity, ProcessedResult result,
	                            ValueConstructor<ValueType> constr, String columnName){
		if(result.getQueryType() == ProcessedResult.Type.ERROR){
			SQLException e = ((ProcessedErrorResult) result).getException();
			Toast.makeText(activity, activity.getString(R.string.ServerMain_Structure_QueryFailure, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
		}else if(result.getQueryType() == ProcessedResult.Type.QUERY){
			ProcessedQueryResult queryResult = (ProcessedQueryResult) result;
			List<ValueType> list = new ArrayList<>();
			for(Row row : queryResult.getValues()){
				StringCell cell = (StringCell) row.findCell(columnName);
				Log.d("DatabaseStructure", cell.getClass().getCanonicalName());
				ValueType value = constr.create(cell.getValue());
				list.add(value);
				if(value instanceof Structure){
					((Structure) value).doQuery(activity);
				}
			}
			setContents(list);

			StructureFragment fragment = (StructureFragment) activity.getPage(ServerMainPage.STRUCTURE);
			assert fragment != null;
			if(fragment.getDisplayedStructure() == this){
				fragment.setDisplayedStructure(this);
			}
		}else{
			throw new AssertionError("Unknown result type: " + result.getQueryType().name());
		}
	}

	public abstract void doQuery(ServerMainActivity activity);

	public static interface OnChangeListener<ValueType>{

		public void onChangeName(String newName);

		public void onSetContents();
		/**
		 * Called only when an individual object is added through {@link #addContent(ListEntry)} )}
		 *
		 * @param type
		 */
		public void onAddContent(ValueType type);

		public void onRemoveContent(ValueType type);

		/**
		 * Called whenever an object is added
		 *
		 * @param type
		 */
		public void onOneValueAdded(ValueType type);

	}

	public static abstract class OnChangeAdaptor<ValueType> implements OnChangeListener<ValueType>{
		@Override
		public void onChangeName(String newName){
		}

		@Override
		public void onSetContents(){
		}

		@Override
		public void onAddContent(ValueType valueType){
		}

		@Override
		public void onRemoveContent(ValueType valueType){
		}

		@Override
		public void onOneValueAdded(ValueType valueType){
		}

	}

	private class ListViewAdapter extends ArrayAdapter<ValueType> implements AdapterView.OnItemClickListener{
		private final StructureFragment fragment;

		public ListViewAdapter(StructureFragment fragment){
			super(fragment.getContext(), android.R.layout.simple_list_item_1, values);
			this.fragment = fragment;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			TextView textView = Main.createTextView(fragment.getContext(), getItem(position).getName());
			int padding = fragment.getResources().getDimensionPixelOffset(R.dimen.database_structure_entry_padding);
			textView.setPadding(padding, padding, padding, padding);
			textView.setTextSize(fragment.getResources().getDimension(R.dimen.database_structure_text_font));
			return textView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			ValueType value = getItem(position);
			if(value instanceof Structure){
				fragment.setDisplayedStructure((Structure) value);
			}else{
				Log.wtf("MCC", Structure.this.getClass().getName() + " didn't override populateFragment(StructureFragment)!");
			}
		}
	}

	protected static interface ValueConstructor<ValueType>{
		public ValueType create(String name);
	}

	@Deprecated
	protected void dep_present(ServerMainActivity activity, String columnName, ProcessedResult result, ValueConstructor<ValueType> constr){
		Log.d(getClass().getSimpleName(), "Got result of type " + result.getQueryType().name());
		if(result.getQueryType() == ProcessedResult.Type.ERROR){
			SQLException e = ((ProcessedErrorResult) result).getException();
			Toast.makeText(activity,
					activity.getString(R.string.ServerMain_Structure_QueryFailure, e.getLocalizedMessage()),
					Toast.LENGTH_LONG).show();
		}else if(result.getQueryType() == ProcessedResult.Type.QUERY){
			ProcessedQueryResult queryResult = (ProcessedQueryResult) result;
			List<ValueType> list = new ArrayList<>();
			for(Row row : queryResult.getValues()){
				StringCell cell = (StringCell) row.findCell(columnName);
				Log.d("DatabaseStructure", cell.getClass().getCanonicalName());
				ValueType value = constr.create(cell.getValue());
				list.add(value);
				value.onFirstDisplay(activity);
			}
			setContents(list);
		}else{
			throw new AssertionError("Unknown result type: " + result.getQueryType().name());
		}
	}

	@Deprecated
	public LinearLayout dep_getLayout(final Context ctx){
		if(dep_layout != null){
			return dep_layout;
		}
		dep_layout = new LinearLayout(ctx);
		dep_layout.setLayoutParams(Main.MP_WC);
		dep_layout.setOrientation(LinearLayout.VERTICAL);
		int padding = ctx.getResources().getDimensionPixelOffset(R.dimen.database_structure_entry_padding);
		dep_layout.setPadding(padding, padding, padding, padding);
		if(isContentLoaded()){
			dep_layout.addView(dep_getListView(ctx));
		}else{
			final TextView textView = Main.createTextView(ctx, R.string.ServerMain_Structure_EntryLoading, getName());
			textView.setLayoutParams(Main.WC_WC);
			textView.setTextSize(ctx.getResources().getDimension(R.dimen.database_structure_text_font));
			dep_layout.addView(textView);
			listeners.add(new ListEntryOnChangeAdaptor(ctx, textView));
		}
		return dep_layout;
	}

	@Deprecated
	protected ListView dep_getListView(final Context ctx){
		if(dep_listView != null){
			return dep_listView;
		}
		dep_listView = new ListView(ctx);
		final ArrayAdapter<ValueType> adapter = new ArrayAdapter<ValueType>(ctx, android.R.layout.simple_list_item_1, values){
			@Override
			public View getView(int position, View convertView, ViewGroup parent1){
				ValueType item = getItem(position);
				return item.getViewOnce(ctx);
			}
		};

//		adapter.setNotifyOnChange(false);
//		listeners.add(new OnChangeAdaptor<ValueType>(){
//			@Override
//			public void onRemoveContent(ValueType valueType){
//				adapter.notifyDataSetChanged();
//			}
//
//			@Override
//			public void onOneValueAdded(ValueType valueType){
//				adapter.notifyDataSetChanged();
//			}
//		});

		dep_listView.setAdapter(adapter);
		return dep_listView;
	}

	@Deprecated
	private class ListEntryOnChangeAdaptor extends OnChangeAdaptor<ValueType>{
		private final Context ctx;

		private final TextView textView;

		public ListEntryOnChangeAdaptor(Context ctx, TextView textView){
			this.ctx = ctx;
			this.textView = textView;
		}

		@Override
		public void onSetContents(){
			final ListView listView = dep_getListView(ctx);
			listView.setPadding(listView.getPaddingLeft() +
							getNestLevels() * ctx.getResources().getDimensionPixelOffset(R.dimen.database_structure_left_padding_indent),
					listView.getPaddingTop(), listView.getPaddingRight(), listView.getPaddingBottom());
			dep_layout.removeView(textView);
			if(getName() != null){
				LinearLayout clicker = new LinearLayout(ctx);
				clicker.setLayoutParams(Main.MP_WC);
				clicker.setOrientation(LinearLayout.HORIZONTAL);
				textView.setText(getName());
				clicker.addView(textView);
				final Button button = new Button(ctx);
//				button.setText(R.string.Global_Hide);
				button.setText(R.string.Global_Show);
				listView.setVisibility(View.GONE);
				button.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v){
						Log.d("DatabaseStructure-Click", Structure.this.getClass().getSimpleName() + ":" + getName());
						if(listView.getVisibility() == View.GONE){
							listView.setVisibility(View.VISIBLE);
							button.setText(R.string.Global_Hide);
						}else{
							listView.setVisibility(View.GONE);
							button.setText(R.string.Global_Show);
						}
					}
				});
				clicker.addView(button);
				dep_layout.addView(clicker);
				Log.i("Structure", "OnClickListener to " + textView.getText());
			}
			dep_layout.addView(listView);
			dep_layout.invalidate();
//					listeners.remove(this);
		}

	}
}

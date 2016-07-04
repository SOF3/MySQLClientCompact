package io.github.chankyin.mysqlclientcompact.ui.home;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.ui.server.main.ServerMainActivity;
import lombok.Getter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
	public final static String PREF_SERVERS_JSON = "io.github.chankyin.mysqlclientcompact.PREF_SERVERS_JSON";
	@Getter private List<ServerObject> servers;
	@Getter private ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(R.string.Global_AppName);
		setContentView(R.layout.activity_home);

		list = (ListView) findViewById(R.id.Home_ListView);
		assert list != null;
		SharedPreferences prefs = getSharedPreferences(PREF_SERVERS_JSON, MODE_PRIVATE);
		String serversJson = prefs.getString("servers", "[]");
		JsonReader reader = new JsonReader(new StringReader(serversJson));
		servers = new ArrayList<>();
		try{
			reader.beginArray();
			while(reader.hasNext()){
				servers.add(ServerObject.fromJson(reader));
			}
			reader.endArray(); // needed?
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
			Log.e("servers.json", "corruption", e);
		}

		list.setAdapter(new ServerObjectArrayAdapter(servers));
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_home, menu);
		return true;
	}

	public void onAddButtonClick(MenuItem item){
		DialogFragment dialog = new AddServerDialogFragment();
		dialog.show(getSupportFragmentManager(), "addServer");
	}

	public void addServer(ServerObject server){
		servers.add(server);
		onServersChanged();
	}

	public void substituteServer(ServerObject server, int id){
		servers.set(id, server);
		onServersChanged();
	}

	public void deleteServer(int id){
		servers.remove(id);
		onServersChanged();
	}

	public void editServer(int id){
		DialogFragment dialog = new AddServerDialogFragment();
		Bundle args = new Bundle();
		args.putInt(AddServerDialogFragment.ARG_EDIT_SERVER_ID, id);
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "editServer");
	}

	public void onServersChanged(){
		StringWriter out = new StringWriter();
		JsonWriter writer = new JsonWriter(out);
		try{
			writer.beginArray();
			for(ServerObject serverObject : servers){
				serverObject.toJson(writer);
			}
			writer.endArray();
			writer.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
		String json = out.toString();
		SharedPreferences.Editor editor = getSharedPreferences(PREF_SERVERS_JSON, MODE_PRIVATE).edit();
		editor.putString("servers", json);
		editor.apply();

		((ServerObjectArrayAdapter) list.getAdapter()).notifyDataSetChanged();
//		list.setAdapter(new ServerObjectArrayAdapter(servers));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		ServerMainActivity.start(this, servers.get(position));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
//		DeleteServerDialogFragment dialog = new DeleteServerDialogFragment();
//		Bundle args = new Bundle();
//		args.putInt("serverId", position);
//		dialog.setArguments(args);
//		dialog.show(getSupportFragmentManager(), "deleteServer");
		LongClickServerListDialog dialog = new LongClickServerListDialog();
		Bundle args = new Bundle();
		args.putInt(LongClickServerListDialog.ARG_SERVER_ID, position);
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "serverActions");
		return true;
	}

	private class ServerObjectArrayAdapter extends ArrayAdapter<ServerObject>{
		public ServerObjectArrayAdapter(List<ServerObject> servers){
			super(HomeActivity.this, android.R.layout.simple_list_item_1, servers);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			ServerObject server = getItem(position);
			LinearLayout layout = new LinearLayout(HomeActivity.this);
			layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.VERTICAL);
			TextView title = new TextView(HomeActivity.this);
			title.setText(server.getServerName());
			title.setTextSize(title.getTextSize() * 0.67f);
			title.setTypeface(null, Typeface.BOLD);
			layout.addView(title);
			TextView info = new TextView(HomeActivity.this);
			String text = server.getUsername() + "@" + server.getAddress() + ":" + server.getPort() + "/" + server.getOptions().getDefaultSchema();
			info.setText(text);
			info.setTextSize(info.getTextSize() * 0.33f);
			layout.addView(info);
			return layout;
		}
	}
}

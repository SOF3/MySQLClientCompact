package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.objects.QueryLogEntry;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.objects.struct.DatabaseStructure;
import io.github.chankyin.mysqlclientcompact.view.LogTextView;
import lombok.Getter;

import java.io.IOException;
import java.io.StringReader;

public class ServerMainActivity extends AppCompatActivity{
	public final static String INTENT_EXTRA_BASE = "io.github.chankyin.mysqlclientcompact.serverui.ServerMainActivity.intentextra.";
	public final static String INTENT_EXTRA_USERNAME = INTENT_EXTRA_BASE + "username";
	public final static String INTENT_EXTRA_PASSWORD = INTENT_EXTRA_BASE + "password";
	/**
	 * A boolean-extra that is only checked if the password string-extra is empty or not set.
	 */
	public final static String INTENT_EXTRA_PASSWORD_SET = INTENT_EXTRA_BASE + "passwordSet";
	public final static String INTENT_EXTRA_SERVER_NAME = INTENT_EXTRA_BASE + "serverName";
	public final static String INTENT_EXTRA_OPTIONS = INTENT_EXTRA_BASE + "options.json";

	@Getter private ViewPager pager;
	@Getter private ServerObject server;
	@Getter private ScrollView scroll;
	@Getter private LogTextView<QueryLogEntry> queryLog;

	@Getter private ConnectionThread connectionThread = null;

	@Getter private MyFragmentPagerAdapter pagerAdapter;

	@Getter private boolean queryLogUpdateScheduled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Uri uri = intent.getData();
		String username = intent.getStringExtra(INTENT_EXTRA_USERNAME);
		if("".equals(username)){
			username = null;
		}

		String password = intent.getStringExtra(INTENT_EXTRA_PASSWORD);
		boolean isPasswordSet = intent.getBooleanExtra(INTENT_EXTRA_PASSWORD_SET, false);
		if("".equals(password) && !isPasswordSet){
			password = null;
		}

		String serverName = intent.getStringExtra(INTENT_EXTRA_SERVER_NAME);
		String optionsJson = intent.getStringExtra(INTENT_EXTRA_OPTIONS);
		ServerObject.Options options;
		try{
			options = ServerObject.Options.fromJson(new JsonReader(new StringReader(optionsJson)));
		}catch(IOException e){
			options = ServerObject.Options.builder().build();
		}

		server = ServerObject.fromUri(uri, username, password, serverName, options);

		if(username == null || password == null){
			new QueryCredentialsDialog().show(getSupportFragmentManager(), "queryCredentials");
		}else{
			init();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.server_main, menu);
		return true;
	}

	@UiThread
	public void init(){
		final Handler handler = new Handler();
		final long UPDATE_RATE = 100;
		Runnable doUpdateQueryLog = new Runnable(){
			@Override
			public void run(){
				doUpdateQueryLog();
				handler.postDelayed(this, UPDATE_RATE);
			}
		};
		handler.postDelayed(doUpdateQueryLog, UPDATE_RATE);
		queryLog = new LogTextView<>(this);

		connectionThread = new ConnectionThread(server, this);
		connectionThread.start();

		setTitle(server.getServerName());

		setContentView(R.layout.activity_server_main);
		pagerAdapter = new MyFragmentPagerAdapter();

		TabLayout tabs = (TabLayout) findViewById(R.id.ServerMain_TabLayout);
		assert tabs != null;
		for(int tabNumber = 0; tabNumber < pagerAdapter.getCount(); tabNumber++){
			tabs.addTab(tabs.newTab().setText(ServerMainPage.values()[tabNumber].getStringId()));
		}

		pager = (ViewPager) findViewById(R.id.ServerMain_ViewPager);
		assert pager != null;
		pager.setId(R.id.ServerMain_ViewPager);
		pager.setAdapter(pagerAdapter);
		pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
			@Override
			public void onPageSelected(int position){
				getPage(ServerMainPage.values()[position]).onSelected();
			}
		});

		tabs.setupWithViewPager(pager);

		final TextView queryLogTitle = (TextView) findViewById(R.id.ServerMain_QueryLog_Title);
		assert queryLogTitle != null;
		queryLogTitle.setOnClickListener(new View.OnClickListener(){
			private boolean hidden = false;

			@Override
			public void onClick(View v){
				if(!hidden){
					queryLog.setVisibility(View.GONE);
					queryLogTitle.setText(R.string.ServerMain_QueryLog_Title_Hidden);
					hidden = true;
				}else{
					queryLog.setVisibility(View.VISIBLE);
					queryLogTitle.setText(R.string.ServerMain_QueryLog_Title);
					hidden = false;
				}
			}
		});

		queryLog.setTextSize(getResources().getDimension(R.dimen.query_log_font));
		queryLog.setTypeface(Typeface.MONOSPACE);
		scroll = (ScrollView) findViewById(R.id.ServerMain_QueryLog_Scroll);
		assert scroll != null;
		scroll.addView(queryLog);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		connectionThread.disconnect();
	}

	@Override
	public void onBackPressed(){
		if(!((MFragment) pagerAdapter.getItem(pager.getCurrentItem())).onBackPressed()){
			super.onBackPressed();
		}
	}

	public void onRefreshStatus(MenuItem item){
		StatusFragment fragment = (StatusFragment) getPage(ServerMainPage.STATUS);
		fragment.refresh();
	}

	public void onRefreshStructure(MenuItem item){
		StructureFragment fragment = (StructureFragment) getPage(ServerMainPage.STRUCTURE);
		if(fragment.getDatabase() != null){
			DatabaseStructure database = new DatabaseStructure(server);
			fragment.setDatabase(database);
			database.doQuery(this);
		}
	}

	public void scheduleUpdateQueryLog(){
		queryLogUpdateScheduled = true;
	}

	@UiThread
	public void doUpdateQueryLog(){
		if(queryLogUpdateScheduled){
			queryLogUpdateScheduled = false;
			queryLog.updateText();
		}
	}

	public MFragment getPage(ServerMainPage page){
		for(int i = 0; i < pagerAdapter.getCount(); i++){
			Fragment fragment = pagerAdapter.getItem(i);
			if(fragment.getClass().equals(page.getFragmentClass())){
				return (MFragment) fragment;
			}
		}
		return null;
	}

	private class MyFragmentPagerAdapter extends FragmentPagerAdapter{
		@Getter private Fragment[] instances = new Fragment[ServerMainPage.values().length];

		private boolean hasStartedStruct = false;

		public MyFragmentPagerAdapter(){
			super(getSupportFragmentManager());
		}

		@Override
		@SuppressWarnings("TryWithIdenticalCatches")
		public Fragment getItem(int position){
			try{
				if(instances[position] != null){
					return instances[position];
				}
				return instances[position] = ServerMainPage.values()[position].getFragmentClass().newInstance();
			}catch(InstantiationException e){
				Log.wtf("ServerMain", e);
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				Log.wtf("ServerMain", e);
				throw new RuntimeException(e);
			}
		}

		@Override
		public CharSequence getPageTitle(int position){
			return getResources().getString(ServerMainPage.values()[position].getStringId());
		}

		@Override
		public int getCount(){
			return ServerMainPage.values().length;
		}
	}

	public static void start(Context ctx, ServerObject server){
		Intent intent = new Intent(ctx, ServerMainActivity.class);
		intent.setData(Uri.fromParts("jdbc",
				server.getAddress() + ":" + server.getPort() +
						"/" + server.getOptions().getDefaultSchema(""), ""));
		intent.putExtra(INTENT_EXTRA_USERNAME, server.getUsername());
		intent.putExtra(INTENT_EXTRA_PASSWORD, server.getPassword());
		intent.putExtra(INTENT_EXTRA_SERVER_NAME, server.getServerName());
		intent.putExtra(INTENT_EXTRA_OPTIONS, server.getOptions().toJson());
		ctx.startActivity(intent);
	}
}

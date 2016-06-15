package io.github.chankyin.mysqlclientcompact.serverui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.mysql.ConnectionThread;
import io.github.chankyin.mysqlclientcompact.mysql.result.ProcessedResult;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
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

	@Getter private ConnectionThread thread = null;

	@Getter private MyFragmentPagerAdapter pagerAdapter;

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

	public void init(){
		thread = new ConnectionThread(server, this);
		thread.start();

		setTitle(server.getServerName());

		LinearLayout layout = new LinearLayout(this);
		pagerAdapter = new MyFragmentPagerAdapter();
		layout.setOrientation(LinearLayout.VERTICAL);

		TabLayout tabs = new TabLayout(this);
		for(int tabNumber = 0; tabNumber < pagerAdapter.getCount(); tabNumber++){
			tabs.addTab(tabs.newTab().setText(ServerMainPage.values()[tabNumber].getStringId()));
		}
		layout.addView(tabs);

		pager = new ViewPager(this);
		pager.setId(R.id.ServerMain_ViewPager);
		pager.setAdapter(pagerAdapter);
		layout.addView(pager);

		tabs.setupWithViewPager(pager);

		setContentView(layout);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		thread.disconnect();
	}

	public void handleQueryResult(ProcessedResult result){

	}

	public Fragment getPage(ServerMainPage page){
		for(Fragment fragment : pagerAdapter.getInstances()){
			if(fragment.getClass().equals(page.getFragmentClass())){
				return fragment;
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
				return instances[position]
						= ServerMainPage.values()[position].getFragmentClass().newInstance();
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

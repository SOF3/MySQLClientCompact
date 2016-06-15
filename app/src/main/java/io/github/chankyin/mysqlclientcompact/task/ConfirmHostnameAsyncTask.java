package io.github.chankyin.mysqlclientcompact.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import io.github.chankyin.mysqlclientcompact.R;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConfirmHostnameAsyncTask extends AsyncTask<Void, Void, InetAddress>{
	private final Context ctx;
	private final String hostname;
	private Executor executor = null;

	public ConfirmHostnameAsyncTask(Context ctx, String hostname){
		this(ctx, hostname, null);
	}

	public ConfirmHostnameAsyncTask(Context ctx, String hostname, Executor executor){
		this.ctx = ctx;
		this.hostname = hostname;
		this.executor = executor;
	}

	@Override
	protected InetAddress doInBackground(Void... params){
		try{
			return InetAddress.getByName(hostname);
		}catch(UnknownHostException e){
			return null;
		}
	}

	@Override
	protected void onPostExecute(InetAddress address){
		if(executor == null){
			if(address == null){
				Toast.makeText(ctx, ctx.getString(R.string.AddServer_AddressNotResolved)
						.replace("${1}", hostname), Toast.LENGTH_LONG).show();
			}
		}else{
			executor.run(address);
		}
	}

	public static interface Executor{
		public void run(InetAddress address);
	}
}

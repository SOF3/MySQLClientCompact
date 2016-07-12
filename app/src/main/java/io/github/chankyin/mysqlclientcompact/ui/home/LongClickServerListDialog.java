package io.github.chankyin.mysqlclientcompact.ui.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;

public class LongClickServerListDialog extends DialogFragment{
	public final static String ARG_SERVER_ID = "serverId";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Bundle args = getArguments();
		final int serverId = args.getInt(ARG_SERVER_ID);
		HomeActivity activity = (HomeActivity) getActivity();
		ServerObject server = activity.getServers().get(serverId);
		return new AlertDialog.Builder(getActivity())
				.setTitle(getResources().getString(R.string.Home_Menu_Actions_Title, server.getServerName()))
				.setItems(R.array.Home_Menu_Actions, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						dismiss();
						switch(which){
							case 0:
								((HomeActivity) getActivity()).editServer(serverId);
								break;
							case 1:
							((HomeActivity) getActivity()).deleteServer(serverId);
							break;
							default:
								Log.wtf("MCC", "Unknown dialog item "+which);
						}
					}
				})
				.create();
	}
}

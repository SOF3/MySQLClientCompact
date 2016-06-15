package io.github.chankyin.mysqlclientcompact;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class DeleteServerDialogFragment extends DialogFragment{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
				.setTitle("Delete server")
				.setMessage("Do you really want to delete this server?")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						((HomeActivity) getActivity()).deleteServer(getArguments().getInt("serverId"));
						dismiss();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						dismiss();
					}
				})
				.create();
	}
}

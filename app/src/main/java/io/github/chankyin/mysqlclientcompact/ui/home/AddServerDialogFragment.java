package io.github.chankyin.mysqlclientcompact.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.task.ConfirmHostnameAsyncTask;

public class AddServerDialogFragment extends DialogFragment{
	public final static String ARG_EDIT_SERVER_ID = "editId";
	private int serverId;
	private boolean isEdit;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Bundle args = getArguments();
		serverId = args.getInt(ARG_EDIT_SERVER_ID, -1);
		isEdit = serverId != -1;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams") final LinearLayout layout
				= (LinearLayout) inflater.inflate(R.layout.layout_dialog_main_add_server, null);

		if(isEdit){
			populateLayout(layout);
		}

		layout.findViewById(R.id.AddServer_Address).setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus){
				if(!hasFocus){ // when focus is lost
					String hostname = ((EditText) v).getText().toString();
					new ConfirmHostnameAsyncTask(getActivity(), hostname).execute();
				}
			}
		});
		layout.findViewById(R.id.AddServer_ShowPassword).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				EditText pass = (EditText) layout.findViewById(R.id.AddServer_Password);
				boolean checked = ((CheckBox) v).isChecked();
				pass.setInputType(InputType.TYPE_CLASS_TEXT | (checked ?
						InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
						InputType.TYPE_TEXT_VARIATION_PASSWORD));
			}
		});
		CheckBox queryPass = (CheckBox) layout.findViewById(R.id.AddServer_QueryPassword);
		queryPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				EditText pass = (EditText) layout.findViewById(R.id.AddServer_Password);
				pass.setEnabled(!isChecked);
			}
		});

		builder.setView(layout)
				.setTitle(isEdit ? R.string.Home_Menu_Edit : R.string.Home_Menu_Add)
				.setPositiveButton(isEdit ? android.R.string.ok : R.string.AddServer_Add,
						new PositiveOnClickListener(layout))
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						Toast.makeText(getActivity(),
								isEdit ? R.string.EditServer_Aborted : R.string.AddServer_Aborted,
								Toast.LENGTH_LONG).show();
						dismiss();
					}
				});
		return builder.create();
	}

	private void populateLayout(LinearLayout layout){
		ServerObject server = ((HomeActivity) getActivity()).getServers().get(serverId);
		setText(layout, R.id.AddServer_ServerName, server.getServerName());
		setText(layout, R.id.AddServer_Address, server.getAddress());
		setText(layout, R.id.AddServer_Port, Integer.toString(server.getPort()));
		setText(layout, R.id.AddServer_Username, server.getUsername());
		setText(layout, R.id.AddServer_Password, server.getPassword());
		if(!server.isPasswordSet()){
			CheckBox checkBox = (CheckBox) layout.findViewById(R.id.AddServer_QueryPassword);
			checkBox.setChecked(true);
		}
		setText(layout, R.id.AddServer_DefaultSchema, server.getOptions().getDefaultSchema());
	}

	private void setText(LinearLayout layout, @IdRes int resId, String text){
		((EditText) layout.findViewById(resId)).setText(text);
	}

	private class PositiveOnClickListener implements DialogInterface.OnClickListener{
		private final View layout;

		public PositiveOnClickListener(View layout){
			this.layout = layout;
		}

		@Override
		public void onClick(DialogInterface dialog, int which){
			String portName = getEditedTextById(R.id.AddServer_Port);
			int port;
			try{
				port = Integer.parseInt(portName);
			}catch(NumberFormatException e){
				port = 3306;
				Toast.makeText(getActivity(), R.string.AddServer_PortInvalid, Toast.LENGTH_LONG).show();
			}
			String hostname = getEditedTextById(R.id.AddServer_Address);
			String serverName = getEditedTextById(R.id.AddServer_ServerName);
			if(serverName.isEmpty()){
				serverName = hostname;
			}
			ServerObject server = ServerObject.builder()
					.serverName(serverName)
					.address(hostname)
					.port(port)
					.username(getEditedTextById(R.id.AddServer_Username))
					.password(getEditedTextById(R.id.AddServer_Password))
					.isPasswordSet(!
							((CheckBox) layout.findViewById(R.id.AddServer_QueryPassword))
									.isChecked())
					.options(ServerObject.Options.builder()
							.defaultSchema(getEditedTextById(R.id.AddServer_DefaultSchema))
							.build())
					.build();
			if(isEdit){
				((HomeActivity) getActivity()).substituteServer(server, serverId);
			}else{
				((HomeActivity) getActivity()).addServer(server);
			}
			dismiss();
		}

		public String getEditedTextById(int id){
			return ((EditText) layout.findViewById(id)).getText().toString();
		}
	}
}

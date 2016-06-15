package io.github.chankyin.mysqlclientcompact;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;
import io.github.chankyin.mysqlclientcompact.task.ConfirmHostnameAsyncTask;

public class AddServerDialogFragment extends DialogFragment{
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams") final View layout = inflater.inflate(R.layout.layout_dialog_main_add_server, null);
		builder.setView(layout)
				.setTitle(R.string.Home_Menu_Add)
				.setPositiveButton(R.string.AddServer_Add, new DialogInterface.OnClickListener(){
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
						ServerObject server = ServerObject.builder()
								.serverName(getEditedTextById(R.id.AddServer_ServerName))
								.address(getEditedTextById(R.id.AddServer_Address))
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
						((HomeActivity) getActivity()).addServer(server);
						dismiss();
					}

					public String getEditedTextById(int id){
						return ((EditText) layout.findViewById(id)).getText().toString();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						Toast.makeText(getActivity(), R.string.AddServer_Aborted, Toast.LENGTH_LONG).show();
						dismiss();
					}
				});
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
		layout.findViewById(R.id.AddServer_QueryPassword).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				EditText pass = (EditText) layout.findViewById(R.id.AddServer_Password);
				pass.setEnabled(!((CheckBox) v).isChecked());
			}
		});
		return builder.create();
	}
}

package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.*;
import io.github.chankyin.mysqlclientcompact.Main;
import io.github.chankyin.mysqlclientcompact.R;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

public class QueryCredentialsDialog extends DialogFragment{
	private ServerObject server;
	private EditText usernameEdit, passwordEdit;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LinearLayout layout, line;
		TextView textView;
		server = ((ServerMainActivity) getActivity()).getServer();

		layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);

		line = new LinearLayout(getActivity());
		line.setLayoutParams(Main.MP_WC);
		line.setOrientation(LinearLayout.HORIZONTAL);
		textView = new TextView(getActivity());
		textView.setLayoutParams(Main.WC_WC);
		textView.setText(R.string.QueryCred_Username);
		line.addView(textView);
		usernameEdit = new EditText(getActivity());
		usernameEdit.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		usernameEdit.setLayoutParams(Main.WC_WC);
		if(server.getUsername() != null){
			usernameEdit.setText(server.getUsername());
		}
		line.addView(usernameEdit);
		layout.addView(line);

		line = new LinearLayout(getActivity());
		line.setLayoutParams(Main.MP_WC);
		line.setOrientation(LinearLayout.HORIZONTAL);
		textView = new TextView(getActivity());
		textView.setLayoutParams(Main.WC_WC);
		textView.setText(R.string.QueryCred_Password);
		line.addView(textView);
		passwordEdit = new EditText(getActivity());
		passwordEdit.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
		passwordEdit.setLayoutParams(Main.WC_WC);
		line.addView(passwordEdit);
		layout.addView(line);

		final CheckBox box = new CheckBox(getActivity());
		box.setLayoutParams(Main.MP_WC);
		box.setText(R.string.Global_ShowPassword);
		box.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				passwordEdit.setInputType(TYPE_CLASS_TEXT | (box.isChecked() ? TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : TYPE_TEXT_VARIATION_PASSWORD));
			}
		});
		layout.addView(box);

		return builder.setView(layout)
				.setTitle(getActivity().getString(R.string.QueryCred_Title))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						server.setUsername(usernameEdit.getText().toString());
						server.setPassword(passwordEdit.getText().toString());
						dismiss();
						((ServerMainActivity) getActivity()).init();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which){
						Toast.makeText(getActivity(), R.string.ServerMain_AbortMissingCred, Toast.LENGTH_LONG).show();
						dismiss();
						getActivity().finish();
					}
				})
				.create();
	}
}

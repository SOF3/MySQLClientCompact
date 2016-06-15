package io.github.chankyin.mysqlclientcompact.serverui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import io.github.chankyin.mysqlclientcompact.objects.ServerObject;

public class StatusFragment extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
		ServerObject server = ((ServerMainActivity) getActivity()).getServer();
		LinearLayout layout = new LinearLayout(getActivity());
		return layout;
	}
}

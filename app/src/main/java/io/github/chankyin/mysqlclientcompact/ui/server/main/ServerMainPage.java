package io.github.chankyin.mysqlclientcompact.ui.server.main;

import android.support.annotation.StringRes;
import io.github.chankyin.mysqlclientcompact.R;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServerMainPage{
	STATUS(StatusFragment.class, R.string.ServerMain_TabTitles_Status),
	QUERY(QueryFragment.class, R.string.ServerMain_TabTitles_Query),
	STRUCTURE(StructureFragment.class, R.string.ServerMain_TabTitles_Structure);

	@Getter private final Class<? extends MFragment> fragmentClass;
	@Getter @StringRes private final int stringId;
}

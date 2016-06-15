package io.github.chankyin.mysqlclientcompact.mysql;

import android.support.annotation.StringRes;
import io.github.chankyin.mysqlclientcompact.R;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CriticalError{
	UNKNOWN_HOST(R.string.ConnError_UnknownHost),
	ESTABLISH_ERROR(R.string.ConnError_Establish),
	CLOSE_ERROR(R.string.ConnError_Close),
	UNKNOWN_ACCESS_ERROR(R.string.ConnError_Access);

	@Getter @StringRes private final int messageId;
}

package fr.redmoon.tictac.bus.export.calendar;

import android.net.Uri;

public interface ICalendarAPIInfos {
	String getColumnNameID();
	String getColumnNameAccountName();
	String getColumnNameCalendarDisplayName();
	String getColumnNameOwnerAccount();
	
	Uri getCalendarUri();
	Uri getEventsContentUri();
}

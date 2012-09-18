package fr.redmoon.tictac.bus.export.calendar;

import android.net.Uri;


public class CalendarAPIInfosSDKlt8 implements ICalendarAPIInfos {
	private static final Uri CALENDAR_URI = Uri.parse("content://calendar/calendars");
	private static final Uri EVENTS_CONTENT_URI = Uri.parse("content://com.android.calendar/events");
	
	@Override
	public String getColumnNameID() {
		return "_id";
	}

	@Override
	public String getColumnNameAccountName() {
		return "account_name";
	}

	@Override
	public String getColumnNameCalendarDisplayName() {
		return "displayName";
	}

	@Override
	public String getColumnNameOwnerAccount() {
		return "ownerAccount";
	}

	@Override
	public Uri getCalendarUri() {
		return CALENDAR_URI;
	}

	@Override
	public Uri getEventsContentUri() {
		return EVENTS_CONTENT_URI;
	}
	
	
}

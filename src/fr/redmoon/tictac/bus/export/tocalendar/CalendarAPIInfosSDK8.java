package fr.redmoon.tictac.bus.export.tocalendar;

import android.net.Uri;

public class CalendarAPIInfosSDK8 extends CalendarAPIInfosSDKlt8 {
	private static final Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/calendars");
	
	@Override
	public Uri getCalendarUri() {
		return CALENDAR_URI;
	}
}

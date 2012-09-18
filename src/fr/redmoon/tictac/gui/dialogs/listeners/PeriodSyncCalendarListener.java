package fr.redmoon.tictac.gui.dialogs.listeners;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.DatePicker;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;

public class PeriodSyncCalendarListener extends AbsPeriodChooserListener {
	public PeriodSyncCalendarListener(final Activity _activity, final DbAdapter _db, final DatePicker _date1, final DatePicker _date2) {
		super(_activity, _db, _date1, _date2);
	}
	
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		// Identifiants des jours saisis
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		
		// Récupération des jours à synchroniser
		final List<DayBean> days = new ArrayList<DayBean>();
		mDb.fetchDays(firstDay, lastDay, days);
		
		// Synchronisation des jours
		for (DayBean day : days) {
			CalendarAccess.getInstance().createEvents(day);
		}
	}
}

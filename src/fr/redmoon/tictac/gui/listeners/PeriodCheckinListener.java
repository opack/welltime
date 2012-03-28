package fr.redmoon.tictac.gui.listeners;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.db.DbAdapter;

public class PeriodCheckinListener implements DialogInterface.OnClickListener {
	private final Activity mActivity;
	private final DbAdapter mDb;
	private final DatePicker mDate1;
	private final DatePicker mDate2;
	private final Spinner mDayType;
	
	public PeriodCheckinListener(final Activity _Activity, final DbAdapter _db, final DatePicker _date1, final DatePicker _date2, final Spinner _dayType) {
		mActivity = _Activity;
		mDb = _db;
		mDate1 = _date1;
		mDate2 = _date2;
		mDayType = _dayType;
	}
	
	public void onClick(final DialogInterface dialog, final int which) {
		int nbDaysCreated = 0;
		int nbDaysUpdated = 0;
		
		// Récupération des dates et du type de jour.
		final Calendar calendar = new GregorianCalendar(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		final int dayType = mDayType.getSelectedItemPosition();
		
		// Parcours des jours
		long curDate = DateUtils.getDayId(calendar);
		final DayBean dayToCreate = new DayBean();
		while (curDate <= lastDay) {
			// On vérifie s'il s'agit d'un jour travaillé dans la semaine
			if (DateUtils.isWorkingWeekDay(calendar)) {
				// On crée ce jour en base s'il n'existe pas
				if (mDb.isDayExisting(curDate)) {
					if (mDb.updateDayType(curDate, dayType)) {
						nbDaysUpdated++;
					}
				} else {
					dayToCreate.date = curDate;
					dayToCreate.type = dayType;
					mDb.createDay(dayToCreate);
					if (dayToCreate.isValid) {
						nbDaysCreated++;
					}
				}
			}
			
			// On passe au lendemain
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			curDate = DateUtils.getDayId(calendar);
		}
		
		// Affichage des résultats
		Toast.makeText(
				mActivity,
				mActivity.getString(
					R.string.period_checkin_results,
					nbDaysCreated, 
					nbDaysUpdated),
				Toast.LENGTH_LONG)
			.show();
	}
}

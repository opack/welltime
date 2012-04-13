package fr.redmoon.tictac.gui.dialogs.listeners;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.db.DbAdapter;

public class PeriodCheckinListener extends AbsPeriodChooserListener {
	private final Spinner mDayType;
	private final EditText mNote;
	
	public PeriodCheckinListener(final Activity _activity, final DbAdapter _db, final DatePicker _date1, final DatePicker _date2, final Spinner _dayType, final EditText _note) {
		super(_activity, _db, _date1, _date2);
		mDayType = _dayType;
		mNote = _note;
	}
	
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		int nbDaysCreated = 0;
		int nbDaysUpdated = 0;
		
		// R�cup�ration des dates et du type de jour.
		final Calendar calendar = new GregorianCalendar(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		final int dayType = mDayType.getSelectedItemPosition();
		final String note = mNote.getText().toString();
		
		// Parcours des jours
		final long firstDay = DateUtils.getDayId(calendar);
		long curDate = firstDay;
		final DayBean dayData = new DayBean();
		while (curDate <= lastDay) {
			// On v�rifie s'il s'agit d'un jour travaill� dans la semaine
			if (DateUtils.isWorkingWeekDay(calendar)) {
				// On pr�pare le jour � cr�er ou mettre � jour
				mDb.fetchDay(curDate, dayData);
				dayData.type = dayType;
				if (note != null) {
					dayData.note = note;
				}
				
				// On cr�e ce jour en base s'il n'existe pas, sinon on le
				// met � jour
				if (dayData.isValid) {
					mDb.updateDay(dayData);
					if (dayData.isValid) {
						nbDaysUpdated++;
					}
				} else {
					mDb.createDay(dayData);
					if (dayData.isValid) {
						nbDaysCreated++;
					}
				}
			}
			
			// On passe au lendemain
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			curDate = DateUtils.getDayId(calendar);
		}
		
		// On met � jour le temps HV depuis le 
    	// dernier enregistrement avant cette date jusqu'au dernier
    	// jour en base.
    	final FlexUtils flexUtils = new FlexUtils(mDb);
    	flexUtils.updateFlexIfNeeded(firstDay);
		
		// Affichage des r�sultats
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

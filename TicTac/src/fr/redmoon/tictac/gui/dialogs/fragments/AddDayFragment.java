package fr.redmoon.tictac.gui.dialogs.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.StandardDayTypes;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;

public class AddDayFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	public final static String TAG = AddDayFragment.class.getName();
	
	private long mDate;
	
	@Override
	public void setArguments(Bundle args) {
		mDate = args.getLong(DialogArgs.DATE.name());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int year = DateUtils.extractYear(mDate);
		final int monthOfYear = DateUtils.extractMonth(mDate);
		final int dayOfMonth = DateUtils.extractDayOfMonth(mDate);
		return new DatePickerDialog(getActivity(), this, year, monthOfYear, dayOfMonth);
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		final long selectedDate = DateUtils.getDayId(year, month, dayOfMonth);
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final DbAdapter db = activity.getDbAdapter();
		if (db == null || selectedDate == 0) {
			return;
		}
		boolean dbUpdated = false;
		
		// Mise � jour de la base de donn�es
		if (!db.isDayExisting(selectedDate)) {
			final DayBean day = new DayBean();
			day.date = selectedDate;
			day.typeMorning = StandardDayTypes.normal.name();
			day.typeAfternoon = StandardDayTypes.normal.name();
			db.createDay(day);
			dbUpdated = day.isValid;
			
			// Mise � jour de l'HV.
	    	final FlexUtils flexUtils = new FlexUtils(db);
	    	flexUtils.updateFlex(day.date);
	    	
	    	// Ajout des �v�nements dans le calendrier
			if (dbUpdated && PreferencesBean.instance.syncCalendar) {
				CalendarAccess.getInstance().createDayTypeEvent(day.date, day.typeMorning, day.typeAfternoon);
			}
		} else {
			// Si le jour existe d�j�, tant pis : on l'affiche.
			dbUpdated = true;
		}
		
		// Mise � jour de l'affichage
		if (dbUpdated) {
			activity.populateView(selectedDate);
		}
	}
}
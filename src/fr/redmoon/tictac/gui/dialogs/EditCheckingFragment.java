package fr.redmoon.tictac.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class EditCheckingFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	public final static String TAG = EditCheckingFragment.class.getName();
	
	private long mDate;
	private int mOldCheckingValue;
	
	@Override
	public void setArguments(Bundle args) {
		mDate = args.getLong(DialogArgs.DATE.name());
		mOldCheckingValue = args.getInt(DialogArgs.TIME.name());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int hour = TimeUtils.extractHour(mOldCheckingValue);
		final int minute = TimeUtils.extractMinutes(mOldCheckingValue);
		return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		final int newTime = hourOfDay * 100 + minute;
		
		if (newTime == 0) {
			return;
		}
		boolean dbUpdated = false;

		// Si on a choisit un pointage valide et qu'il n'existe
		// pas encore, on le cr�e et on supprime l'ancien.
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final DbAdapter db = DbAdapter.getInstance(activity);
		db.openDatabase();
		if (newTime != 0
		&& !db.isCheckingExisting(mDate, newTime)) {
			dbUpdated = db.createChecking(mDate, newTime);
		}
		
		// On souhaite supprimer l'ancien pointage s'il est diff�rent du nouveau
		if (mOldCheckingValue != TimeUtils.UNKNOWN_TIME && mOldCheckingValue != newTime) {
			dbUpdated = db.deleteChecking(mDate, mOldCheckingValue);
		}
		
		// Mise � jour de l'HV.
		final FlexUtils flexUtils = new FlexUtils(db);
		flexUtils.updateFlex(mDate);
		
		// Ajout du pointage dans le calendrier
		if (dbUpdated && PreferencesBean.instance.syncCalendar) {
			final List<Integer> checkings = new ArrayList<Integer>();
			db.fetchCheckings(mDate, checkings);
			CalendarAccess.getInstance().createWorkEvents(mDate, checkings);
		}
		db.closeDatabase();
		
		if (dbUpdated) {
			// Mise � jour des widgets
			if (mDate == DateUtils.getCurrentDayId()) {
				WidgetProvider.updateWidgets(activity);
			}
			
			// Mise � jour de l'affichage
			activity.populateView(mDate);
		}
	}
	
}
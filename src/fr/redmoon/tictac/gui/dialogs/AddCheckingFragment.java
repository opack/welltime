package fr.redmoon.tictac.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.StandardDayTypes;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class AddCheckingFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	public final static String TAG = AddCheckingFragment.class.getName();
	
	private long mDate;
	private int mInitialChecking;
	private boolean mFinishActivityOnDismiss;
	
	@Override
	public void setArguments(Bundle args) {
		mDate = args.getLong(DialogArgs.DATE.name());
		mInitialChecking = args.getInt(DialogArgs.TIME.name(), 0);
		mFinishActivityOnDismiss = args.getBoolean(DialogArgs.FINISH_ACTIVITY_ON_DISMISS.name(), false);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int hour = TimeUtils.extractHour(mInitialChecking);
		final int minute = TimeUtils.extractMinutes(mInitialChecking);
		return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		final int selectedTime = hourOfDay * 100 + minute;
		final TicTacActivity activity = (TicTacActivity)getActivity();
		if (selectedTime == 0) {
			return;
		}
		boolean dbUpdated = false;
		final DbAdapter db = DbAdapter.getInstance(getActivity());
		db.openDatabase();
		
		// Mise � jour de la base de donn�es
		if (db.isCheckingExisting(mDate, selectedTime)) {
			// Le pointage existe d�j� : affichage d'un message
			Toast.makeText(
				activity,
				activity.getString(R.string.error_checking_already_exists),
				Toast.LENGTH_LONG)
			.show();
		} else {
			// Cr�ation du nouveau pointage
			if (!db.isDayExisting(mDate)) {
				// Le jour n'existe pas : on le cr�e.
				final DayBean day = new DayBean();
				day.date = mDate;
				day.typeMorning = StandardDayTypes.normal.name();
				day.typeAfternoon = StandardDayTypes.normal.name();
				day.checkings.add(selectedTime);
				db.createDay(day);
				dbUpdated = day.isValid;
				
				// Ajout des �v�nements dans le calendrier
				if (dbUpdated && PreferencesBean.instance.syncCalendar) {
					CalendarAccess.getInstance().createEvents(day);
				}
			} else {
				// Le jour existe : on ajoute simplement le pointage
				dbUpdated = db.createChecking(mDate, selectedTime);
				
				if (dbUpdated && PreferencesBean.instance.syncCalendar) {
					// Ajout du pointage dans le calendrier
					final List<Integer> checkings = new ArrayList<Integer>();
					db.fetchCheckings(mDate, checkings);
					CalendarAccess.getInstance().createWorkEvents(mDate, checkings);
				}
			}
			
			// Mise � jour de l'HV.
			final FlexUtils flexUtils = new FlexUtils(db);
			flexUtils.updateFlex(mDate);
		}
		db.closeDatabase();
		
		// Mise � jour de l'affichage
		if (dbUpdated) {
			// Mise � jour des widgets
			if (mDate == DateUtils.getCurrentDayId()) {
				WidgetProvider.updateWidgets(activity);
			}
			
			// Mise � jour de l'affichage
			if (!mFinishActivityOnDismiss) {
				activity.populateView(mDate);
			} else {
				Toast.makeText(getActivity(), "Pointage � " + TimeUtils.formatTime(selectedTime) + " enregistr� !", Toast.LENGTH_LONG).show();
				activity.finish();
			}
		}
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mFinishActivityOnDismiss) {
			final Activity activity = getActivity();
			if (activity != null) {
				activity.finish();
			}
		}
	}
}
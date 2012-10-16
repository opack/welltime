package fr.redmoon.tictac.gui.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class EditExtraFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	public final static String TAG = EditExtraFragment.class.getName();
	
	private long mDate;
	private int mOldValue;
	
	@Override
	public void setArguments(Bundle args) {
		mDate = args.getLong(DialogArgs.DATE.name());
		mOldValue = args.getInt(DialogArgs.TIME.name());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int hour = TimeUtils.extractHour(mOldValue);
		final int minute = TimeUtils.extractMinutes(mOldValue);
		return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		final int newTime = hourOfDay * 100 + minute;
		final TicTacActivity activity = (TicTacActivity)getActivity();
		if (newTime == mOldValue) {
			return;
		}
		// Mise à jour de la base de données
		if (DbAdapter.getInstance().updateDayExtra(mDate, newTime)) {
			// Mise à jour de l'affichage
			activity.populateView(mDate);
		}
	}
}
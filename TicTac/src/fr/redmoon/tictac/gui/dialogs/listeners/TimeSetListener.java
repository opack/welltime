package fr.redmoon.tictac.gui.dialogs.listeners;

import android.app.TimePickerDialog;
import android.widget.TimePicker;
import fr.redmoon.tictac.TicTacActivity;

public abstract class TimeSetListener extends TicTacListener implements TimePickerDialog.OnTimeSetListener {
	
	public TimeSetListener(final TicTacActivity activity) {
		super(activity);
	}
	
	protected abstract void onTimeSet(int newTime);
	
	@Override
	public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
		if (mDb != null) {
			final int newTime = hourOfDay * 100 + minute;
			onTimeSet(newTime);
		}
	}
}
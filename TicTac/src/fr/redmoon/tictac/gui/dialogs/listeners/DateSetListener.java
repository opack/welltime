package fr.redmoon.tictac.gui.dialogs.listeners;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;

public abstract class DateSetListener extends TicTacListener implements DatePickerDialog.OnDateSetListener {
	
	public DateSetListener(final TicTacActivity activity) {
		super(activity);
	}
	
	@Override
	public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
		if (mDb != null) {
			final long newDate = DateUtils.getDayId(year, monthOfYear, dayOfMonth);
			onDateSet(newDate);
		}
	}
	
	protected abstract void onDateSet(long newDate);
}
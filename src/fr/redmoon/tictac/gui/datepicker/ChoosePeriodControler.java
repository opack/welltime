package fr.redmoon.tictac.gui.datepicker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.format.Time;
import android.widget.DatePicker;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;

public class ChoosePeriodControler implements DatePickerDialog.OnDateSetListener {

	public interface OnPeriodChoosedListener {
		void onPeriodSet(long start, long end);
	}
	
	protected DatePickerDialog startDateDialog;
	protected DatePickerDialog endDateDialog;
	protected long startDate;
	protected OnPeriodChoosedListener listener;
	
	public ChoosePeriodControler(final Context context, final OnPeriodChoosedListener listener) {
		this.listener = listener;
		
		// Création des deux boîtes de dialogue
		final Time now = new Time();
		now.setToNow();
		startDateDialog = DatePickerDialogHelper.createPeriodChooserDialog(context, this);
		DatePickerDialogHelper.prepare(startDateDialog, now.year, now.month, now.monthDay, context.getString(R.string.export_dlg_start));
		
		endDateDialog = DatePickerDialogHelper.createPeriodChooserDialog(context, this);
		DatePickerDialogHelper.prepare(endDateDialog, now.year, now.month, now.monthDay, context.getString(R.string.export_dlg_end));
	}
	
	@Override
	public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
		final long newDate = DateUtils.getDayId(year, monthOfYear, dayOfMonth);
		if (startDate == 0) {
			startDate = newDate;
			endDateDialog.show();
		} else {
			listener.onPeriodSet(startDate, newDate);
		}
	}

	public void prompt() {
		// Affichage de la première boîte de dialogue
		startDateDialog.show();
	}
}
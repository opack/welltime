package fr.redmoon.tictac.gui.dialogs.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.gui.ViewSynchronizer;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;

public class ShowDayFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	public final static String TAG = ShowDayFragment.class.getName();
	
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
		if (selectedDate == 0) {
			return;
		}
		activity.populateView(selectedDate);
		
		// Sauvegarde du jour courant dans le synchroniseur de vues pour accorder
    	// toutes les vues sur le même jour
    	ViewSynchronizer.getInstance().setCurrentDay(selectedDate);
	}
}
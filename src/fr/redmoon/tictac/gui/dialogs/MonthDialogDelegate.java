package fr.redmoon.tictac.gui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.gui.dialogs.listeners.AddDayListener;
import fr.redmoon.tictac.gui.dialogs.listeners.ShowDayListener;

public class MonthDialogDelegate extends AbsDialogDelegate {
	
	/**
	 * Listeners pour les boîtes de dialogue
	 */
	private AddDayListener mAddDayListener;
	private ShowDayListener mShowDayListener;
	
	public MonthDialogDelegate(final TicTacActivity activity) {
		super(activity);
		
		// Préparation des listeners pour les boîtes de dialogue
        mAddDayListener = new AddDayListener(mActivity);
        mShowDayListener = new ShowDayListener(mActivity);
	}
	
	@Override
	public Dialog createDialog(final int id) {
		final long today = mActivity.getToday();
		switch (id) {
		case DialogTypes.DATEPICKER_ADD_DAY:
			return DatePickerDialogHelper.createDialog(mActivity, mAddDayListener, DateUtils.extractYear(today), DateUtils.extractMonth(today), DateUtils.extractDayOfMonth(today));
		case DialogTypes.DATEPICKER_SHOW_DAY:
			return DatePickerDialogHelper.createDialog(mActivity, mShowDayListener, DateUtils.extractYear(today), DateUtils.extractMonth(today), DateUtils.extractDayOfMonth(today));
		}
		return null;
	}
	
	@Override
	public void prepareDialog(final int id, final Dialog dialog, final Bundle args) {
		final long date = args.getLong(DialogArgs.DATE);
		switch (id) {
			case DialogTypes.DATEPICKER_ADD_DAY:
			case DialogTypes.DATEPICKER_SHOW_DAY:
				// C'est un peu inutile car on passe toujours le jour courant.
				DatePickerDialogHelper.prepare((DatePickerDialog) dialog, DateUtils.extractYear(date), DateUtils.extractMonth(date), DateUtils.extractDayOfMonth(date));
				break;
		}
	}
}

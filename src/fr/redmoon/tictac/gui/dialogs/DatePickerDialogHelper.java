package fr.redmoon.tictac.gui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;

public class DatePickerDialogHelper {
	public static Dialog createDialog(final Context context, final DatePickerDialog.OnDateSetListener listener, final int year, final int monthOfYear, final int dayOfMonth) {
		return new DatePickerDialog(context, listener, year, monthOfYear, dayOfMonth);
	}
	
	public static void prepare(final DatePickerDialog dialog, final int year, final int monthOfYear, final int dayOfMonth) {
		dialog.updateDate(year, monthOfYear, dayOfMonth);
	}
	
	public static void prepare(final DatePickerDialog dialog, final int year, final int monthOfYear, final int dayOfMonth, final String title) {
		prepare(dialog, year, monthOfYear, dayOfMonth);
		if (title != null) {
			dialog.setTitle(title);
		}
	}
}

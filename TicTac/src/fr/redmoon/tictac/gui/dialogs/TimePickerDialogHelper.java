package fr.redmoon.tictac.gui.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import fr.redmoon.tictac.bus.TimeUtils;

public class TimePickerDialogHelper {
	public static Dialog createDialog(final Context context, final TimePickerDialog.OnTimeSetListener listener) {
		return new TimePickerDialog(
				context,
				listener,
				0,
				0,
				true);
	}
	
	public static void prepare(final TimePickerDialog dialog, final  int time) {
		int hours = 0;
		int minutes = 0;
		if (time != TimeUtils.UNKNOWN_TIME) {
			hours = (int) (time / 100);
			minutes = (int) (time % 100);
		}
		dialog.updateTime(hours, minutes);
	}
	
	public static void prepare(final TimePickerDialog dialog, final int time, final String title) {
		prepare(dialog, time);
		if (title != null) {
			dialog.setTitle(title);
		}
	}
}
